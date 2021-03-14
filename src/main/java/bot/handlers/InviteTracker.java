package bot.handlers;

import bot.data.GreetingType;
import bot.data.InviteType;
import bot.data.objects.InviteData;
import bot.database.DataSource;
import bot.database.objects.Greeting;
import bot.utils.BotUtils;
import bot.utils.MiscUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InviteTracker extends ListenerAdapter {

    private final Logger LOGGER = LoggerFactory.getLogger(InviteTracker.class);
    private final Map<String, InviteData> inviteCache = new ConcurrentHashMap<>();

    public boolean shouldInvitesByTracked(Guild guild) {
        return DataSource.INS.getSettings(guild.getId()).shouldTrackInvites
                && guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER);
    }

    public void cacheGuildInvites(Guild guild) {
        guild.retrieveInvites().queue((invites) -> {
                    for (Invite invite : invites) {
                        inviteCache.put(invite.getCode(), new InviteData(invite));
                    }
                }
        );
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

    public void handleWelcome(Guild guild, User user) {
        // Invites tracking
        if (this.shouldInvitesByTracked(guild)) {

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

                User inviter = null;
                if (inviteUsed != null) {
                    inviter = inviteUsed.getInviter();
                    if (inviter == null) {
                        LOGGER.error("GuildId: {} - No user found for invite {}", guild.getId(), inviteUsed);
                    } else {
                        DataSource.INS.logInvite(guild.getId(), user.getId(), inviter.getId());
                        DataSource.INS.incrementInvites(guild.getId(), inviter.getId(), InviteType.TOTAL);
                    }
                }
                this.sendGreeting(guild, user, inviter, GreetingType.WELCOME);

            });

        } else {
            this.sendGreeting(guild, user, null, GreetingType.WELCOME);
        }

    }

    public void handleFarewell(Guild guild, @Nullable User user) {
        if (this.shouldInvitesByTracked(guild)) {
            if (user != null) {
                final String inviterId = DataSource.INS.getInviterId(guild.getId(), user.getId());
                DataSource.INS.incrementInvites(guild.getId(), inviterId, InviteType.LEFT);

                // Retrieve Member from inviterId
                if (inviterId != null) {
                    guild.retrieveMemberById(inviterId).queue(member ->
                            this.sendGreeting(guild, user, member.getUser(), GreetingType.FAREWELL)
                    );
                }
            }
            return;
        }
        this.sendGreeting(guild, user, null, GreetingType.FAREWELL);
    }

    private void sendGreeting(Guild guild, User user, User inviter, GreetingType type) {
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

        EmbedBuilder embed = buildEmbed(guild, user, inviter, config);
        BotUtils.sendEmbed(greetChannel, embed.build());

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

    public static EmbedBuilder buildEmbed(Guild guild, User user, @Nullable User inviter, Greeting config) {
        EmbedBuilder embed = new EmbedBuilder();
        if (config.embedColor != null)
            embed.setColor(MiscUtils.hex2Rgb(config.embedColor));
        if (config.description != null)
            embed.setDescription(resolveGreeting(guild, user, inviter, config.description));
        if (config.embedFooter != null)
            embed.setFooter(resolveGreeting(guild, user, inviter, config.embedFooter));
        if (config.embedThumbnail)
            embed.setThumbnail(user.getEffectiveAvatarUrl());
        if (config.embedImage != null)
            embed.setImage(config.embedImage);

        return embed;
    }

    public static String resolveGreeting(Guild guild, User user, @Nullable User inviter, String message) {
        int[] inviterInvites = {0, 0, 0};
        if (inviter != null)
            inviterInvites = DataSource.INS.getInvites(guild.getId(), inviter.getId());
        return message.replaceAll("\\\\n", "\n")
                .replace("{server}", guild.getName())
                .replace("{count}", String.valueOf(guild.getMemberCount()))
                .replace("{member}", user.getAsTag())
                .replace("{inviter}", (inviter == null ? "NA" : inviter.getAsTag()))
                .replace("{invites}", "Total: `" + inviterInvites[0] + "` Fake: `" + inviterInvites[1] + "` Left: `" + inviterInvites[2] + "`");
    }

}
