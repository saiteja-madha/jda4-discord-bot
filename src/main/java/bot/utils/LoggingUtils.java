package bot.utils;

import bot.data.ModAction;
import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

public class LoggingUtils {

    public static void onSetNick(Message msg, Member target, @Nullable String oldName, String newName) {
        logModeration(msg, msg.getMember(), target, null, new String[]{oldName, newName}, ModAction.SETNICK);
    }

    public static void onDeafen(Message msg, Member target, String reason) {
        logModeration(msg, msg.getMember(), target, reason, null, ModAction.DEAFEN);
    }

    public static void onUnDeafen(Message msg, Member target, String reason) {
        logModeration(msg, msg.getMember(), target, reason, null, ModAction.UNDEAFEN);
    }

    public static void onVMute(Message msg, Member target, String reason) {
        logModeration(msg, msg.getMember(), target, reason, null, ModAction.VMUTE);
    }

    public static void onVUnmute(Message msg, Member target, String reason) {
        logModeration(msg, msg.getMember(), target, reason, null, ModAction.VUNMUTE);
    }

    public static void onWarn(Message msg, Member target, String reason, int receivedWarnings, int maxWarnings) {
        logModeration(msg, msg.getMember(), target, reason, new String[]{String.valueOf(receivedWarnings), String.valueOf(maxWarnings)},
                ModAction.WARN);
    }

    public static void onTempMute(Message msg, Member target, String reason, String unmuteTime) {
        logModeration(msg, msg.getMember(), target, reason, new String[]{unmuteTime}, ModAction.TEMPMUTE);
    }

    public static void onMute(Message msg, Member target, String reason) {
        logModeration(msg, msg.getMember(), target, reason, null, ModAction.MUTE);
    }

    public static void onUnmute(Message msg, Member target, String reason) {
        logModeration(msg, msg.getMember(), target, reason, null, ModAction.UNMUTE);
    }

    public static void onKick(Message msg, Member target, String reason) {
        logModeration(msg, msg.getMember(), target, reason, null, ModAction.KICK);
    }

    public static void onSoftBan(Message msg, Member target, String reason) {
        logModeration(msg, msg.getMember(), target, reason, null, ModAction.SOFTBAN);
    }

    public static void onTempBan(Message msg, Member target, String reason, String unbanTime) {
        logModeration(msg, msg.getMember(), target, reason, new String[]{unbanTime}, ModAction.TEMPMUTE);
    }

    public static void onBan(Message msg, Member target, String reason) {
        logModeration(msg, msg.getMember(), target, reason, null, ModAction.BAN);
    }

    private static void logModeration(Message message, Member mod, Member target, String reason, @Nullable String[] args, ModAction action) {
        final Guild guild = message.getGuild();
        GuildSettings settings = DataSource.INS.getSettings(guild.getId());

        TextChannel logChannel = null;
        if (settings.modlogChannel != null) logChannel = guild.getTextChannelById(settings.modlogChannel);
        if (logChannel == null) return;

        StringBuilder str = new StringBuilder();

        final String reasonString = (reason == null) || reason.equals("") ? "Not specified" : reason;
        final String modString = (mod == null) ? "NA" : mod.getUser().getAsTag() + " [`" + mod.getId() + "`]";
        final String targetString = (target == null) ? "NA" : target.getUser().getAsTag() + " [`" + target.getId() + "`]";

        switch (action) {
            case SETNICK:
                str.append("**Moderator:** ").append(modString).append("\n")
                        .append("**Target User:** ").append(targetString).append("\n")
                        .append("**Old Name:** ").append(args[0] == null ? "-" : args[0]).append("\n")
                        .append("**New Name:** ").append(args[1]).append("\n");
                break;

            case DEAFEN:
                str.append("**Deafened User:** ").append(targetString).append("\n")
                        .append("**Deafened By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
                break;

            case UNDEAFEN:
                str.append("**UnDeafened User:** ").append(targetString).append("\n")
                        .append("**UnDeafened By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
                break;

            case VMUTE:
                str.append("**VoiceMuted User:** ").append(targetString).append("\n")
                        .append("**VoiceMuted By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
                break;

            case VUNMUTE:
                str.append("**Voice UnMuted User:** ").append(targetString).append("\n")
                        .append("**Voice UnMuted By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
                break;

            case PURGE:
                str.append("**Purged By:** ").append(modString).append("\n")
                        .append("**Purge Type:** ").append(args[0]).append("\n")
                        .append("**Purge Channel:** ").append(message.getTextChannel().getAsMention()).append("\n")
                        .append("**Messages Purged:** ").append(args[1]).append("\n");
                break;

            case WARN:
                str.append("**Warned User:** ").append(targetString).append("\n")
                        .append("**Warned By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n")
                        .append("**Warnings Received:** ").append(args[0]).append("\n")
                        .append("**Max Warnings:** ").append(args[1]).append("\n");
                break;

            case MUTE:
                str.append("**Muted User:** ").append(targetString).append("\n")
                        .append("**Muted By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
                break;

            case TEMPMUTE:
                str.append("**TempMuted User:** ").append(targetString).append("\n")
                        .append("**TempMuted By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n")
                        .append("**Muted upto:** `").append(args[0]).append("`\n");
                break;

            case UNMUTE:
                str.append("**UnMuted User:** ").append(targetString).append("\n")
                        .append("**UnMuted By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
                break;

            case KICK:
                str.append("**Kicked User:** ").append(targetString).append("\n")
                        .append("**Kicked By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
                break;

            case BAN:
                str.append("**Banned User:** ").append(targetString).append("\n")
                        .append("**Banned By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
                break;

            case TEMPBAN:
                str.append("**Temp-Banned User:** ").append(targetString).append("\n")
                        .append("**Temp-Banned By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n")
                        .append("**Banned upto:** `").append(args[0]).append("`\n");
                break;

            case SOFTBAN:
                str.append("**Softbanned User:** ").append(targetString).append("\n")
                        .append("**Softbanned By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
                break;

            case UNBAN:
                str.append("**Unbanned User:** ").append(targetString).append("\n")
                        .append("**Unbanned By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
                break;

            default: {
                str.append("**Punished User:** ").append(targetString).append("\n")
                        .append("**Punished By:** ").append(modString).append("\n")
                        .append("**Reason:** ").append(reasonString).append("\n");
            }
        }

        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setColor(0xfe5442)
                .setDescription(str.toString());
        if (target != null)
            embed.setAuthor("Moderation - " + action.getText(), null, target.getUser().getEffectiveAvatarUrl());
        else
            embed.setAuthor("Moderation - " + action.getText());

        BotUtils.sendMsg(logChannel, embed.build());

    }

}
