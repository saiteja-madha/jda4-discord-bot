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
        final String code = event.getCode();
        final InviteData invite = new InviteData(event.getInvite());
        inviteCache.put(code, invite);
    }

    @Override
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {
        final String code = event.getCode();
        inviteCache.remove(code);
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        final User user = event.getUser();

        if (user.isBot()) {
            return;
        }

        this.sendGreeting(event.getGuild(), event.getUser(), GreetingType.WELCOME);

    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        final User user = event.getUser();

        if (user.isBot()) {
            return;
        }

        this.sendGreeting(event.getGuild(), event.getUser(), GreetingType.FAREWELL);

    }

    public void sendGreeting(Guild guild, User user, GreetingType type) {
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

        if (config.description != null && config.description.contains("{inviter}") ||
                config.embedFooter != null && config.embedFooter.contains("{inviter}")) {

            guild.retrieveInvites().queue((invites) -> {
                boolean inviteFound = false;
                for (final Invite invite : invites) {
                    if (inviteFound) {
                        break;
                    }
                    final String code = invite.getCode();
                    final InviteData cachedInvite = inviteCache.get(code);

                    if (cachedInvite == null) {
                        continue;
                    }

                    if (invite.getUses() == cachedInvite.getUses()) {
                        continue;
                    }

                    inviteFound = true;
                    cachedInvite.incrementUses();

                    EmbedBuilder embed = buildEmbed(guild, user, invite.getInviter(), config);
                    BotUtils.sendEmbed(greetChannel, embed.build());

                }
            });
        } else {
            EmbedBuilder embed = buildEmbed(guild, user, null, config);
            BotUtils.sendEmbed(greetChannel, embed.build());
        }

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

    public static EmbedBuilder buildEmbed(Guild guild, User user, User inviter, Greeting config) {
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
        return message.replace("{server}", guild.getName())
                .replace("{count}", String.valueOf(guild.getMemberCount()))
                .replace("{member}", user.getAsTag())
                .replace("{inviter}", ((inviter != null) ? inviter.getAsTag() : "NA"));

    }

}
