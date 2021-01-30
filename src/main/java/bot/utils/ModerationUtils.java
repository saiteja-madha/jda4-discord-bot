package bot.utils;

import bot.command.CommandContext;
import bot.data.PurgeType;
import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import bot.database.objects.WarnLogs;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class ModerationUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(ModerationUtils.class);
    private final static Pattern linkCheck = Pattern.compile("^(?:https?|ftp):\\/\\/[^\\s/$.?#].[^\\s]*$");

    public static boolean canInteract(Member mod, Member target, String action, TextChannel channel) {
        if (!mod.canInteract(target)) {
            BotUtils.sendMsg(channel, "Oops! You cannot `" + action + "` " + target.getEffectiveName());
            return false;
        }

        final Member self = mod.getGuild().getSelfMember();

        if (!self.canInteract(target)) {
            BotUtils.sendMsg(channel, "Ugh! I cannot `" + action + "` " + target.getEffectiveName() + ", Are their roles above mine?");
            return false;
        }
        return true;
    }

    public static boolean canSendLogs(TextChannel cmdChannel, TextChannel channel) {
        final Member self = channel.getGuild().getSelfMember();

        if (!self.hasPermission(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
                Permission.MESSAGE_EMBED_LINKS)) {
            BotUtils.sendMsg(cmdChannel, "Ugh! I cannot send logs to that channel? I need the following permissions in " +
                    "" + channel.getAsMention() + "\n`Read Messages`, `Write Messages` and `Embed Links`");
            return false;
        }
        return true;
    }

    @Nullable
    public static Role createMutedRole(@NotNull Guild guild) {
        Role mutedrole;
        try {
            mutedrole = guild.createRole().setColor(11).setName("Muted").submit().get();
            int pos = guild.getSelfMember().getRoles().get(0).getPosition();
            RoleOrderAction modifyRolePositions = guild.modifyRolePositions();
            modifyRolePositions.selectPosition(mutedrole).moveTo(pos - 1).queue();
            for (TextChannel tc : guild.getTextChannels()) {
                if (guild.getSelfMember().hasPermission(tc, Permission.MANAGE_PERMISSIONS, Permission.VIEW_CHANNEL))
                    tc.createPermissionOverride(mutedrole).deny(Permission.MESSAGE_WRITE).queue();
            }
            for (VoiceChannel vc : guild.getVoiceChannels()) {
                if (guild.getSelfMember().hasPermission(vc, Permission.MANAGE_PERMISSIONS, Permission.VIEW_CHANNEL))
                    vc.createPermissionOverride(mutedrole).deny(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK).queue();

            }
            return mutedrole;
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Role create error | Guild: " + guild.getId() + " Error: " + e.getMessage());
        }

        return null;
    }

    public static int checkPurgeAmount(@NotNull CommandContext ctx, int input) {
        final List<String> args = ctx.getArgs();
        int amount;

        String arg = args.get(input);

        try {
            amount = Integer.parseInt(arg);
        } catch (NumberFormatException ignored) {
            ctx.reply("`" + arg + "` is not a valid number");
            return 0;
        }

        if (amount < 2 || amount > 100) {
            ctx.reply("Amount must be at least 2 and at most 100");
            return 0;
        }
        return amount;
    }

    public static void purge(CommandContext ctx, PurgeType type, int amount, String argument) {
        final TextChannel channel = ctx.getChannel();
        List<Message> messageList = new ArrayList<>();

        for (Message message : channel.getIterableHistory()) {
            if (messageList.size() == amount)
                break;

            if (message.getTimeCreated().isBefore(OffsetDateTime.now().minusWeeks(2)))
                break;

            if (message.isPinned())
                continue;

            switch (type) {
                case ATTACHMENT:
                    if (!message.getAttachments().isEmpty()) {
                        messageList.add(message);
                    }
                    break;
                case BOT:
                    if (message.getAuthor().isBot()) {
                        messageList.add(message);
                    }
                    break;
                case LINK:
                    if (linkCheck.matcher(message.getContentRaw()).matches()) {
                        messageList.add(message);
                    }
                    break;
                case TOKEN:
                    if (message.getContentRaw().toLowerCase().contains(argument.toLowerCase())) {
                        messageList.add(message);
                    }
                    break;
                case USER:
                    if (message.getAuthor().getId().equals(argument)) {
                        messageList.add(message);
                    }
                    break;
                case ALL:
                    messageList.add(message);
                    break;
            }
        }

        final int size = messageList.size();

        if (size <= 1) {
            ctx.reply("Not found messages that can be purged!");
            return;
        }

        if (size > 100)
            messageList.remove(100);

        ctx.getMessage().delete().queue(null, e -> {/* Ignore */});
        ctx.getChannel().deleteMessages(messageList).queue((__) ->
                        BotUtils.sendMsg(channel, "Successfully purged `" + size + "` messages!", 7)
                , e -> {
                    ctx.reply("Purge Failed!");
                    LOGGER.error("Purge Failed: " + e.getMessage());
                });
    }

    public static void setNick(Message message, Member target, String newName) {
        final TextChannel channel = message.getTextChannel();
        final String oldName = target.getNickname();

        target.modifyNickname(newName).queue((__) -> {
            if (oldName == null)
                BotUtils.sendMsg(channel, "Changed nick of `"
                        + target.getUser().getAsTag() + "` to `" + newName + "`");
            else
                BotUtils.sendMsg(channel, "Changed nick of `"
                        + target.getUser().getAsTag() + "` from `"
                        + oldName + "` to `" + newName + "`");

            LoggingUtils.onSetNick(message, target, oldName, newName);

        }, e -> LOGGER.error("Nick change error: " + e.getMessage()));
    }

    public static void deafen(Message msg, Member target, String reason) {
        final TextChannel channel = msg.getTextChannel();
        final GuildVoiceState voiceState = target.getVoiceState();

        if (voiceState != null && !voiceState.inVoiceChannel()) {
            BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` not connected to any voice channel");
            return;
        }

        if (voiceState != null && voiceState.isGuildDeafened()) {
            BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` is already deafened on this server");
            return;
        }

        target.deafen(true).reason(reason).queue(
                (__) -> {
                    BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` is now deafened");
                    LoggingUtils.onDeafen(msg, target, reason);
                },
                e -> {
                    LOGGER.error("Deafen error: " + e.getMessage());
                    BotUtils.sendMsg(channel, "Deafen was not successful");
                });
    }

    public static void unDeafen(Message msg, Member target, String reason) {
        final TextChannel channel = msg.getTextChannel();
        final GuildVoiceState voiceState = target.getVoiceState();

        if (voiceState != null && !voiceState.inVoiceChannel()) {
            BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` not connected to any voice channel");
            return;
        }

        if (voiceState != null && !voiceState.isGuildDeafened()) {
            BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` is not deafened on this server");
            return;
        }

        target.deafen(false).reason(reason).queue(
                (__) -> {
                    BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` is now un-deafened");
                    LoggingUtils.onUnDeafen(msg, target, reason);
                },
                e -> {
                    LOGGER.error("Undeafen error: " + e.getMessage());
                    BotUtils.sendMsg(channel, "Deafen was not successful");
                });
    }

    public static void vmute(Message msg, Member target, String reason) {
        final TextChannel channel = msg.getTextChannel();
        final GuildVoiceState voiceState = target.getVoiceState();

        if (voiceState != null && !voiceState.inVoiceChannel()) {
            BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` not connected to any voice channel");
            return;
        }

        if (voiceState != null && voiceState.isGuildMuted()) {
            BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` is already voice muted on this server");
            return;
        }

        target.mute(true).reason(reason).queue(
                (__) -> {
                    BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` is now voice muted");
                    LoggingUtils.onVMute(msg, target, reason);
                },
                e -> {
                    LOGGER.error("VMute error: " + e.getMessage());
                    BotUtils.sendMsg(channel, "Voice Mute was not successful");
                });
    }

    public static void vunmute(Message msg, Member target, String reason) {
        final TextChannel channel = msg.getTextChannel();
        final GuildVoiceState voiceState = target.getVoiceState();

        if (voiceState != null && !voiceState.inVoiceChannel()) {
            BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` not connected to any voice channel");
            return;
        }

        if (voiceState != null && !voiceState.isGuildMuted()) {
            BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` is not voice muted on this server");
            return;
        }

        target.mute(false).reason(reason).queue(
                (__) -> {
                    BotUtils.sendMsg(channel, "`" + target.getEffectiveName() + "` is now voice un-muted");
                    LoggingUtils.onVUnmute(msg, target, reason);
                },
                e -> {
                    LOGGER.error("VUnMute error: " + e.getMessage());
                    BotUtils.sendMsg(channel, "Voice UnMute was not successful");
                });
    }

    public static void warn(Message msg, Member target, String reason) {
        final TextChannel channel = msg.getTextChannel();
        final String guildId = msg.getGuild().getId();
        final GuildSettings settings = DataSource.INS.getSettings(guildId);

        int maxWarnings = settings.maxWarnings;

        List<WarnLogs> warnLogs = DataSource.INS.getWarnLogs(target);
        int received = warnLogs.size();

        if ((received + 1) >= maxWarnings) {
            if (msg.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS))
                kick(msg, target, "Autokick - Max warnings reached");
            else
                BotUtils.sendMsg(channel, "Max warnings reached for `" + target.getEffectiveName()
                        + "` but unable to kick him due to missing `Kick Permission`");
            return;
        }

        DataSource.INS.warnUser(msg.getMember(), target, reason);
        LoggingUtils.onWarn(msg, target, reason, (received + 1), maxWarnings);
        BotUtils.sendMsg(channel, "`" + target.getUser().getAsTag() + "` has received a warning! `" + (received + 1) + "/" + maxWarnings + "`");

    }

    public static void tempMute(Message msg, Member target, String reason, Role mutedRole, int seconds) {
        final TextChannel tc = msg.getTextChannel();
        final Guild guild = msg.getGuild();

        if (target.getRoles().contains(mutedRole)) {
            BotUtils.sendMsg(tc, "`" + target.getUser().getAsTag() + "` is already muted!");
            return;
        }

        try {
            guild.addRoleToMember(target, mutedRole).queue((__) -> {
                long unmuteTime = Instant.now().plus(seconds, ChronoUnit.SECONDS).getEpochSecond();

                DataSource.INS.tempMute(guild.getId(), target.getId(), unmuteTime);
                String unmute = LocalDateTime.ofInstant(Instant.now().plusSeconds(seconds), ZoneOffset.UTC).toString();
                BotUtils.sendSuccessWithMessage(msg, "`" + target.getUser().getAsTag() + "` is muted until `" + unmute + "`");
                LoggingUtils.onTempMute(msg, target, reason, unmute);

            }, (e) -> {
                LOGGER.error("TempMute Error - " + e.getMessage());
                BotUtils.sendMsg(tc, "Temp Mute was not successful");
            });
        } catch (Exception e) {
            LOGGER.error("TempMute Error - " + e.getMessage());
            BotUtils.sendMsg(tc, "Temp Mute was not successful");
        }

    }

    public static void mute(Message msg, Member target, String reason, Role mutedrole) {
        final TextChannel channel = msg.getTextChannel();
        final Guild guild = msg.getGuild();

        if (target.getRoles().contains(mutedrole)) {
            BotUtils.sendMsg(channel, "`" + target.getUser().getAsTag() + "` is already muted!");
            return;
        }

        try {
            guild.addRoleToMember(target, mutedrole).queue(
                    (__) -> {
                        BotUtils.sendMsg(channel, "`" + target.getUser().getAsTag() + "` is now muted on this server!");
                        LoggingUtils.onMute(msg, target, reason);
                    },
                    (e) -> {
                        LOGGER.error("Mute Error - " + e.getMessage());
                        BotUtils.sendMsg(channel, "Mute was not successful");
                    });
        } catch (Exception e) {
            LOGGER.error("Mute Error - " + e.getMessage());
            BotUtils.sendMsg(channel, "Mute was not successful");
        }

    }

    public static void unmute(Message msg, Member target, String reason) {
        Role mutedrole = null;
        final TextChannel channel = msg.getTextChannel();
        final Guild guild = msg.getGuild();

        for (Role role : target.getRoles())
            if (role.getName().equalsIgnoreCase("muted")) {
                mutedrole = role;
                break;
            }

        if (mutedrole == null) {
            BotUtils.sendMsg(channel, "`" + target.getUser().getAsTag() + "`" + " is not muted!");
            return;
        }

        try {
            guild.removeRoleFromMember(target, mutedrole).queue((__) -> {
                        BotUtils.sendMsg(channel, "`" + target.getUser().getAsTag() + "` is now unmuted on this server!");
                        LoggingUtils.onUnmute(msg, target, reason);
                    },
                    (e) -> {
                        LOGGER.error("UnMute Error - " + e.getMessage());
                        BotUtils.sendMsg(channel, "UnMute was not successful");
                    });
        } catch (Exception e) {
            LOGGER.error("UnMute Error - " + e.getMessage());
            BotUtils.sendMsg(channel, "UnMute was not successful");
        }

    }

    public static void kick(Message msg, Member target, String reason) {
        final TextChannel tc = msg.getTextChannel();
        final Guild guild = msg.getGuild();

        try {
            guild.kick(target, reason)
                    .reason(reason)
                    .queue((__) -> BotUtils.sendMsg(tc, "`" + target.getUser().getAsTag() + "` is kicked from this server!"),
                            (error) -> {
                                BotUtils.sendMsg(tc, "Could not kick " + target.getAsMention());
                                LoggingUtils.onKick(msg, target, reason);
                                LOGGER.error("Kick Error - " + error.getMessage());
                            }
                    );
        } catch (Exception e) {
            LOGGER.error("Kick Error - " + e.getMessage());
            BotUtils.sendMsg(tc, "Kick was not successful");
        }
    }

    public static void softBan(Message msg, Member target, String reason) {
        final TextChannel tc = msg.getTextChannel();
        final Guild guild = msg.getGuild();

        try {
            guild.ban(target, 1, reason)
                    .reason(reason)
                    .queue((__) -> guild.unban(target.getUser()).reason("Softban")
                            .queue((___) -> {
                                BotUtils.sendMsg(tc, "`" + target.getUser().getAsTag() + "` is softbanned from this server!");
                                LoggingUtils.onSoftBan(msg, target, reason);
                            }, (e) -> {
                                BotUtils.sendMsg(tc, "Could not softban " + target.getAsMention());
                                LOGGER.error("Softban Error - " + e.getMessage());
                            }), (e) -> {
                        BotUtils.sendMsg(tc, "Could not softban " + target.getAsMention());
                        LOGGER.error("Softban Error - " + e.getMessage());
                    });

        } catch (Exception e) {
            LOGGER.error("Softban Error - " + e.getMessage());
            BotUtils.sendMsg(tc, "SoftBan was not successful");
        }
    }

    public static void tempBan(Message msg, Member target, String reason, int seconds) {
        final TextChannel tc = msg.getTextChannel();
        final Guild guild = msg.getGuild();

        try {
            guild.ban(target, 0, reason)
                    .reason(reason)
                    .queue((__) -> {
                                long unmuteTime = Instant.now().plus(seconds, ChronoUnit.SECONDS).getEpochSecond();
                                DataSource.INS.tempBan(guild.getId(), target.getId(), unmuteTime);
                                String unban = LocalDateTime.ofInstant(Instant.now().plusSeconds(seconds), ZoneOffset.UTC).toString();
                                BotUtils.sendSuccessWithMessage(msg, "`" + target.getUser().getAsTag() + "` is banned from this server until `" + unban + "`");
                                LoggingUtils.onTempBan(msg, target, reason, unban);

                            },
                            (e) -> {
                                BotUtils.sendMsg(msg.getTextChannel(), "Could not tempban " + target.getAsMention());
                                LOGGER.error("SoftBan Error - " + e.getMessage());
                            }
                    );
        } catch (Exception e) {
            LOGGER.error("Ban Error - " + e.getMessage());
            BotUtils.sendMsg(tc, "TempBan was not successful");
        }

    }

    public static void ban(Message msg, Member target, String reason) {
        final TextChannel tc = msg.getTextChannel();
        final Guild guild = msg.getGuild();

        try {
            guild.ban(target, 0, reason)
                    .reason(reason)
                    .queue((__) -> {
                                BotUtils.sendMsg(tc, "`" + target.getUser().getAsTag() + "` is banned from this server!");
                                LoggingUtils.onBan(msg, target, reason);
                            },
                            (error) -> {
                                BotUtils.sendMsg(tc, "Could not ban " + target.getAsMention());
                                LOGGER.error("Ban Error - " + error.getMessage());
                            }
                    );
            guild.kick(target, reason).reason(reason)
                    .queue((__) -> BotUtils.sendMsg(tc, "`" + target.getUser().getAsTag() + "` is kicked from this server!"),
                            (error) -> BotUtils.sendMsg(tc, "Could not kick " + target.getAsMention())
                    );
        } catch (Exception e) {
            LOGGER.error("Ban Error - " + e.getMessage());
            BotUtils.sendMsg(tc, "Ban was not successful");
        }
    }

}
