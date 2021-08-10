package bot.handlers;

import bot.data.GreetingType;
import bot.data.InviteType;
import bot.data.objects.CachedInvite;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static java.time.temporal.ChronoUnit.DAYS;

public class InviteHandler extends ListenerAdapter {

    private final Logger LOGGER = LoggerFactory.getLogger(InviteHandler.class);
    private final Map<String, Map<String, CachedInvite>> inviteCache = new ConcurrentHashMap<>();
    private static final long FAKE_INVITE_DAYS_OFFSET = 1;

    @Override
    public void onGuildReady(final GuildReadyEvent event) {
        this.cacheGuildInvites(event.getGuild());
    }

    @Override
    public void onGuildInviteCreate(@Nonnull GuildInviteCreateEvent event) {
        this.cacheGuildInvites(event.getGuild());
    }

    @Override
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {
        this.cacheGuildInvites(event.getGuild());
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        final User user = event.getUser();
        if (user.isBot()) {
            return;
        }
        this.handleWelcome(event.getGuild(), user);
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        final User user = event.getUser();
        if (user.isBot()) {
            return;
        }
        this.handleFarewell(event.getGuild(), user);
    }

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

    public static int getTotalInvites(int[] invites) {
        if (invites.length == 4)
            return invites[0] + invites[1] + invites[3]; // Tracked + Fake + Added
        else
            return -1;
    }

    public static int getEffectiveInvites(int[] invites) {
        if (invites.length == 4)
            return invites[0] + invites[3] - invites[1] - invites[2]; // Tracked + Added - Fake - Left
        else
            return -1;
    }

    private boolean shouldInvitesByTracked(Guild guild) {
        return DataSource.INS.getSettings(guild.getId()).shouldTrackInvites
                && guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER);
    }

    private void cacheGuildInvites(Guild guild) {
        if (this.shouldInvitesByTracked(guild)) {
            guild.retrieveInvites().queue(invites -> cacheGuildInvites(guild, invites));
        }
    }

    private void cacheGuildInvites(Guild guild, List<Invite> invites) {
        Map<String, CachedInvite> cache = new ConcurrentHashMap<>();
        for (Invite invite : invites) {
            cache.put(invite.getCode(), new CachedInvite(invite));
        }
        if (guild.getVanityCode() != null) {
            cache.put(guild.getVanityCode(), new CachedInvite(guild.getVanityCode(), this.getVanityInviteUses(guild)));
        }
        inviteCache.put(guild.getId(), cache);
    }

    public void enableTracking(Guild guild) {
        if (!DataSource.INS.getSettings(guild.getId()).shouldTrackInvites) {
            this.cacheGuildInvites(guild);
            DataSource.INS.inviteTracking(guild.getId(), true);
        }
    }

    private int getVanityInviteUses(Guild guild) {
        int vanityUses = 0;
        try {
            return guild.retrieveVanityInvite().submit().get().getUses();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Failed to fetch vanity url for guild: {} | Error: {}", guild.getId(), e.getMessage());
        }
        return vanityUses;
    }

    private boolean isFakeInvite(User user) {
        final String avatar = user.getAvatarUrl();
        final OffsetDateTime timeCreated = user.getTimeCreated();
        final long days = DAYS.between(timeCreated, OffsetDateTime.now());
        return days > FAKE_INVITE_DAYS_OFFSET && avatar == null;
    }

    private void handleWelcome(Guild guild, User user) {
        // Invites tracking is disabled
        if (!this.shouldInvitesByTracked(guild)) {
            this.sendGreetingWithoutInvite(guild, user, GreetingType.WELCOME);
            return;
        }

        guild.retrieveInvites().queue((invites) -> {
            final Map<String, CachedInvite> cachedInvites = inviteCache.get(guild.getId());
            this.cacheGuildInvites(guild, invites);
            final Map<String, CachedInvite> newInvites = inviteCache.get(guild.getId());

            CachedInvite inviteUsed = null;

            for (Map.Entry<String,CachedInvite> entry : newInvites.entrySet()) {
                String code = entry.getKey();
                CachedInvite invite = entry.getValue();

                if (!cachedInvites.containsKey(code)) {
                    if (invite.uses == 1) {
                        inviteUsed = invite;
                        break;
                    }
                } else {
                    if (invite.uses > cachedInvites.get(code).uses) {
                        inviteUsed = invite;
                        break;
                    }
                }
            }

            if (inviteUsed == null) {
                LOGGER.info("Failed to track in guild: {}", guild.getId());
                this.sendGreetingWithoutInvite(guild, user, GreetingType.WELCOME);
                return;
            }

            if (inviteUsed.isVanity) {
                DataSource.INS.logInvite(guild.getId(), user.getId(), "VANITY_URL", guild.getVanityCode());
                final int[] ints = DataSource.INS.incrementInvites(guild.getId(), "VANITY_URL", 1, InviteType.TRACKED);
                this.sendVanityGreeting(guild, user, ints, GreetingType.WELCOME);
                return;
            }

            if (inviteUsed.inviter != null) {
                User inviter = inviteUsed.inviter;
                DataSource.INS.logInvite(guild.getId(), user.getId(), inviter.getId(), inviteUsed.code);

                int[] ints;
                // check possible fake invite
                if (isFakeInvite(user)) {
                    ints = DataSource.INS.incrementInvites(guild.getId(), inviter.getId(), 1, InviteType.FAKE);
                } else {
                    ints = DataSource.INS.incrementInvites(guild.getId(), inviter.getId(), 1, InviteType.TRACKED);
                }

                // Handle Invite Ranks
                this.addInviteRole(guild, inviter, ints);
                this.sendInviterGreeting(guild, user, inviter, ints, GreetingType.WELCOME);
            } else {
                LOGGER.error("GuildId: {} - No user found for invite {}", guild.getId(), inviteUsed.code);
            }
        });
    }

    private void handleFarewell(Guild guild, User user) {
        if (this.shouldInvitesByTracked(guild)) {
            final String inviterId = DataSource.INS.getInviterId(guild.getId(), user.getId());

            // Inviter ID found in Database
            if (inviterId != null) {
                final int[] ints = DataSource.INS.incrementInvites(guild.getId(), inviterId, 1, InviteType.LEFT);
                if (inviterId.equals("VANITY_URL")) {
                    this.sendVanityGreeting(guild, user, ints, GreetingType.FAREWELL);
                    return;
                }

                // Handle Invite Ranks
                this.removeInviteRole(guild, inviterId, ints);

                // Retrieve Member from inviterId
                guild.retrieveMemberById(inviterId).queue(inviter -> {
                            if (inviter == null)
                                this.sendGreetingWithoutInvite(guild, user, GreetingType.FAREWELL);
                            else
                                this.sendInviterGreeting(guild, user, inviter.getUser(), ints, GreetingType.FAREWELL);
                        }, (err) -> this.sendGreetingWithoutInvite(guild, user, GreetingType.FAREWELL)
                );

                // Delete inviter log
                DataSource.INS.removeInviterId(guild.getId(), user.getId());
                return;
            }
        }
        this.sendGreetingWithoutInvite(guild, user, GreetingType.FAREWELL);
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

    private void sendGreetingWithoutInvite(Guild guild, User user, GreetingType type) {
        this.sendGreeting(guild, user, null, new int[]{0, 0, 0, 0}, type, false);
    }

    private void sendInviterGreeting(Guild guild, User user, User inviter, int[] invites, GreetingType type) {
        this.sendGreeting(guild, user, inviter, invites, type, false);
    }

    private void sendVanityGreeting(Guild guild, User user, int[] ints, GreetingType type) {
        this.sendGreeting(guild, user, null, ints, type, true);
    }

    private void sendGreeting(Guild guild, User user, @Nullable User inviter, int[] invites, GreetingType type, boolean isVanity) {
        Greeting config;
        if (type == GreetingType.WELCOME)
            config = DataSource.INS.getWelcomeConfig(guild.getId());
        else
            config = DataSource.INS.getFarewellConfig(guild.getId());

        if (config == null)
            return;


        final TextChannel greetChannel = getGreetingChannel(guild, config);
        if (greetChannel == null)
            return;

        EmbedBuilder embed = buildEmbed(guild, user, inviter, invites, config, isVanity);
        BotUtils.sendEmbed(greetChannel, embed.build());
    }

    public static EmbedBuilder buildEmbed(Guild guild, User user, @Nullable User inviter, int[] invites, Greeting config, boolean isVanity) {
        EmbedBuilder embed = new EmbedBuilder();
        if (config.embedColor != null)
            embed.setColor(MiscUtils.hex2Rgb(config.embedColor));
        if (config.description != null)
            embed.setDescription(replace(guild, user, inviter, invites, config.description, isVanity));
        if (config.embedFooter != null)
            embed.setFooter(replace(guild, user, inviter, invites, config.embedFooter, isVanity));
        if (config.embedThumbnail)
            embed.setThumbnail(user.getEffectiveAvatarUrl());
        if (config.embedImage != null)
            embed.setImage(config.embedImage);

        return embed;
    }

    private static String replace(Guild guild, User user, @Nullable User inviter, int[] invites, String message, boolean isVanity) {
        return message.replaceAll("\\\\n", "\n")
                .replace("{server}", guild.getName())
                .replace("{count}", String.valueOf(guild.getMemberCount()))
                .replace("{member:name}", user.getName())
                .replace("{member:mention}", user.getAsMention())
                .replace("{member:tag}", user.getAsTag())
                .replace("{inviter:name}", isVanity ? "Vanity Url" : (inviter == null ? "NA" : inviter.getName()))
                .replace("{inviter:mention}", isVanity ? "Vanity Url" : (inviter == null ? "NA" : inviter.getAsMention()))
                .replace("{inviter:tag}", isVanity ? "Vanity Url" : (inviter == null ? "NA" : inviter.getAsTag()))
                .replace("{invites}", getEffectiveInvites(invites) + "")
                .replace("{invites:total}", getTotalInvites(invites) + "")
                .replace("{invites:fake}", invites[1] + "")
                .replace("{invites:left}", invites[2] + "");
    }

}
