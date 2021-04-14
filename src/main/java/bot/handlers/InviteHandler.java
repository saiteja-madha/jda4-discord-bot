package bot.handlers;

import bot.data.GreetingType;
import bot.data.InviteType;
import bot.data.objects.InviteData;
import bot.database.DataSource;
import bot.database.objects.Greeting;
import bot.utils.BotUtils;
import bot.utils.MiscUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InviteHandler extends ListenerAdapter {

    private final Logger LOGGER = LoggerFactory.getLogger(InviteHandler.class);
    private final Map<String, InviteData> inviteCache = new ConcurrentHashMap<>();

    @Nullable
    public static TextChannel getGreetingChannel(Guild guild, Greeting config) {
        TextChannel channel = null;
        if (config != null) {
            if (config.channel != null) {
                TextChannel tcById = guild.getTextChannelById(config.channel);
                if (tcById != null)
                    channel = tcById;
                else
                    DataSource.INS.setGreetingChannel(guild.getId(), null, config.type);
            }
        }

        return channel;
    }

    public static EmbedBuilder buildEmbed(Guild guild, User user, @Nullable User inviter, int[] inviterInvites, Greeting config) {
        EmbedBuilder embed = new EmbedBuilder();
        if (config.embedColor != null)
            embed.setColor(MiscUtils.hex2Rgb(config.embedColor));
        if (config.description != null)
            embed.setDescription(resolveGreeting(guild, user, inviter, inviterInvites, config.description));
        if (config.embedFooter != null)
            embed.setFooter(resolveGreeting(guild, user, inviter, inviterInvites, config.embedFooter));
        if (config.embedThumbnail)
            embed.setThumbnail(user.getEffectiveAvatarUrl());
        if (config.embedImage != null)
            embed.setImage(config.embedImage);

        return embed;
    }

    public static String resolveGreeting(Guild guild, User user, @Nullable User inviter, int[] inviterInvites, String message) {
        return message.replaceAll("\\\\n", "\n")
                .replace("{server}", guild.getName())
                .replace("{count}", String.valueOf(guild.getMemberCount()))
                .replace("{member}", user.getName())
                .replace("{@member}", user.getAsMention())
                .replace("{inviter}", (inviter == null ? "NA" : inviter.getName()))
                .replace("{@inviter}", (inviter == null ? "NA" : inviter.getAsMention()))
                .replace("{invites}", "Total: `" + inviterInvites[0] + "` Fake: `" + inviterInvites[1] + "` Left: `" + inviterInvites[2] + "`");
    }

    public static int getEffectiveInvites(int[] invites) {
        if (invites.length == 4)
            return invites[0] - invites[1] - invites[2] + invites[3];
        else
            return invites[0] - invites[1] - invites[2];
    }

    // Cache All invites when enabled and save existing invite count
    public void enableTracking(Guild guild) {
        if (!DataSource.INS.getSettings(guild.getId()).shouldTrackInvites) {

            guild.retrieveInvites().queue((invites) -> {
                for (Invite invite : invites) {
                    final User inviter = invite.getInviter();

                    if (inviter == null)
                        continue;

                    int uses = invite.getUses();

                    if (uses == 0)
                        continue;

                    DataSource.INS.incrementInvites(guild.getId(), inviter.getId(), uses, InviteType.TOTAL);

                }
                this.cacheGuildInvites(invites);
            });

            DataSource.INS.inviteTracking(guild.getId(), true);

        }
    }

    public boolean shouldInvitesByTracked(Guild guild) {
        return DataSource.INS.getSettings(guild.getId()).shouldTrackInvites
                && guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER);
    }

    private void cacheGuildInvites(Guild guild) {
        guild.retrieveInvites().queue(this::cacheGuildInvites);
    }

    private void cacheGuildInvites(List<Invite> invites) {
        for (Invite invite : invites) {
            inviteCache.put(invite.getCode(), new InviteData(invite));
        }
    }

    @Override
    public void onGuildReady(final GuildReadyEvent event) {
        final Guild guild = event.getGuild();

        if (this.shouldInvitesByTracked(guild)) {
            this.cacheGuildInvites(guild);
        }

    }

    @Override
    public void onGuildInviteCreate(@Nonnull GuildInviteCreateEvent event) {
        if (this.shouldInvitesByTracked(event.getGuild())) {
            final String code = event.getCode();
            final InviteData invite = new InviteData(event.getInvite());
            inviteCache.put(code, invite);
        }
    }

    @Override
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {
        if (this.shouldInvitesByTracked(event.getGuild())) {
            final String code = event.getCode();
            inviteCache.remove(code);
        }
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        final User user = event.getUser();

        if (user.isBot()) {
            return;
        }

        this.handleWelcome(event.getGuild(), event.getUser());

    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        final User user = event.getUser();

        if (user.isBot()) {
            return;
        }

        this.handleFarewell(event.getGuild(), event.getUser());

    }

    private void handleWelcome(Guild guild, User user) {
        // Invites tracking is disabled
        if (!this.shouldInvitesByTracked(guild)) {
            this.sendGreetingWithoutInviter(guild, user, GreetingType.WELCOME);
            return;
        }

        guild.retrieveInvites().queue((invites) -> {
            Invite inviteUsed = null;
            for (final Invite invite : invites) {

                // Skip checking other invites
                if (inviteUsed != null) {
                    break;
                }

                final String code = invite.getCode();
                final InviteData cachedInvite = inviteCache.get(code);

                // Skip if the invite wasn't cached earlier
                if (cachedInvite == null) {
                    continue;
                }

                // Uses are same, so this is not the invite used
                if (invite.getUses() == cachedInvite.getUses()) {
                    continue;
                }

                // Is this even possible?
                if (invite.getUses() < cachedInvite.getUses()) {
                    continue;
                }

                inviteUsed = invite;
                cachedInvite.incrementUses();
            }

            User inviter;
            if (inviteUsed != null) {
                inviter = inviteUsed.getInviter();
                if (inviter == null) {
                    LOGGER.error("GuildId: {} - No user found for invite {}", guild.getId(), inviteUsed.getCode());
                } else {
                    DataSource.INS.logInvite(guild.getId(), user.getId(), inviter.getId());
                    final int[] ints = DataSource.INS.incrementInvites(guild.getId(), inviter.getId(), 1, InviteType.TOTAL);

                    // Handle Invite Ranks
                    this.addInviteRole(guild, inviter, ints);
                    this.sendGreeting(guild, user, inviter, ints, GreetingType.WELCOME);

                    return;
                }
            } else {
                // Failed to track invite
                LOGGER.info("Failed to track in guild: {}", guild.getId());
            }
            this.sendGreetingWithoutInviter(guild, user, GreetingType.WELCOME);

        });


    }

    private void handleFarewell(Guild guild, User user) {
        if (this.shouldInvitesByTracked(guild)) {
            final String inviterId = DataSource.INS.getInviterId(guild.getId(), user.getId());

            // Inviter ID found in Database
            if (inviterId != null) {
                final int[] ints = DataSource.INS.incrementInvites(guild.getId(), inviterId, 1, InviteType.LEFT);

                // Handle Invite Ranks
                this.removeInviteRole(guild, inviterId, ints);

                // Retrieve Member from inviterId
                guild.retrieveMemberById(inviterId).queue(inviter -> {
                            if (inviter == null)
                                this.sendGreetingWithoutInviter(guild, user, GreetingType.FAREWELL);
                            else
                                this.sendGreeting(guild, user, inviter.getUser(), ints, GreetingType.FAREWELL);
                        }, (err) -> this.sendGreetingWithoutInviter(guild, user, GreetingType.FAREWELL)
                );
                return;
            }
        }
        this.sendGreetingWithoutInviter(guild, user, GreetingType.FAREWELL);
    }

    private void addInviteRole(Guild guild, User user, int[] ints) {
        final Map<Integer, String> inviteRanks = DataSource.INS.getSettings(guild.getId()).inviteRanks;
        final int effectiveInvites = getEffectiveInvites(ints);

        // Invites count is equal to required (i.e) Add rank
        if (inviteRanks.containsKey(effectiveInvites)) {
            final String roleId = inviteRanks.get(effectiveInvites);
            final Role roleById = guild.getRoleById(roleId);

            if (roleById != null) {
                guild.addRoleToMember(user.getId(), roleById).queue((__) -> {
                    String SERVER_LINK = "https://discord.com/channels/";
                    EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                            .setDescription("**Guild Name**: " + BotUtils.getEmbedHyperLink(guild.getName(), SERVER_LINK + guild.getId()) +
                                    "\n**Role Name**: " + roleById.getName() +
                                    "\n**Invites Count:** " + effectiveInvites
                            ).setAuthor("Invite Role Added")
                            .setColor(roleById.getColor());

                    BotUtils.sendDM(user, embed.build());

                });
            }
        }
    }

    private void removeInviteRole(Guild guild, String inviterId, int[] ints) {
        final Map<Integer, String> inviteRanks = DataSource.INS.getSettings(guild.getId()).inviteRanks;
        final int effectiveInvites = getEffectiveInvites(ints);

        // Invites count is less than required (i.e) Remove rank
        if (inviteRanks.containsKey(effectiveInvites + 1)) {
            final String roleId = inviteRanks.get(effectiveInvites + 1);
            final Role roleById = guild.getRoleById(roleId);

            if (roleById != null) {
                guild.removeRoleFromMember(inviterId, roleById).queue((__) -> {
                    String SERVER_LINK = "https://discord.com/channels/";
                    EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                            .setDescription("**Guild Name**: " + BotUtils.getEmbedHyperLink(guild.getName(), SERVER_LINK + guild.getId()) +
                                    "\n**Role Name**: " + roleById.getName() +
                                    "\n**Invites**: " + effectiveInvites +
                                    "\n**Required Invites**: " + (effectiveInvites + 1)
                            ).setAuthor("Invite Role Removed")
                            .setColor(roleById.getColor());

                    guild.getJDA().retrieveUserById(inviterId).queue(inviter -> BotUtils.sendDM(inviter, embed.build()));

                });
            }
        }

    }

    private void sendGreetingWithoutInviter(Guild guild, User user, GreetingType type) {
        this.sendGreeting(guild, user, null, new int[]{0, 0, 0}, type);
    }

    private void sendGreeting(Guild guild, User user, User inviter, int[] inviterInvites, GreetingType type) {
        Greeting config;
        if (type == GreetingType.WELCOME)
            config = DataSource.INS.getWelcomeConfig(guild.getId());
        else
            config = DataSource.INS.getFarewellConfig(guild.getId());

        if (config == null) {
            return;
        }

        final TextChannel greetChannel = getGreetingChannel(guild, config);
        if (greetChannel == null)
            return;

        EmbedBuilder embed = buildEmbed(guild, user, inviter, inviterInvites, config);
        BotUtils.sendEmbed(greetChannel, embed.build());

    }

}
