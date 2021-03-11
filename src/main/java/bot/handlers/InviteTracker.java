package bot.handlers;

import bot.data.GreetingType;
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

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InviteTracker extends ListenerAdapter {

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

        this.handleGreeting(event.getGuild(), event.getUser(), GreetingType.WELCOME);

    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        final User user = event.getUser();

        if (user.isBot()) {
            return;
        }

        this.handleGreeting(event.getGuild(), event.getUser(), GreetingType.FAREWELL);

    }

    public void handleGreeting(Guild guild, User user, GreetingType type) {
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

                this.sendGreeting(guild, user, inviteUsed, type);

            });

        } else {
            this.sendGreeting(guild, user, null, type);
        }

    }

    private void sendGreeting(Guild guild, User user, @Nullable Invite inviteUsed, GreetingType type) {
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

        EmbedBuilder embed = buildEmbed(guild, user, inviteUsed, config);
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

    public static EmbedBuilder buildEmbed(Guild guild, User user, @Nullable Invite inviteUsed, Greeting config) {
        EmbedBuilder embed = new EmbedBuilder();
        if (config.embedColor != null)
            embed.setColor(MiscUtils.hex2Rgb(config.embedColor));
        if (config.description != null)
            embed.setDescription(resolveGreeting(guild, user, inviteUsed, config.description));
        if (config.embedFooter != null)
            embed.setFooter(resolveGreeting(guild, user, inviteUsed, config.embedFooter));
        if (config.embedThumbnail)
            embed.setThumbnail(user.getEffectiveAvatarUrl());
        if (config.embedImage != null)
            embed.setImage(config.embedImage);

        return embed;
    }

    public static String resolveGreeting(Guild guild, User user, @Nullable Invite inviteUsed, String message) {
        return message.replace("{server}", guild.getName())
                .replace("{count}", String.valueOf(guild.getMemberCount()))
                .replace("{member}", user.getAsTag())
                .replace("{inviter}", ((inviteUsed != null)
                        ? (inviteUsed.getInviter() == null ? "NA" : inviteUsed.getInviter().getAsTag())
                        : "NA")
                );

    }

}
