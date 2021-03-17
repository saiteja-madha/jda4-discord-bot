package bot.utils;

import bot.Config;
import bot.Constants;
import bot.command.CommandContext;
import bot.data.GreetingType;
import bot.data.ImageType;
import bot.database.DataSource;
import bot.database.objects.Greeting;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class ImageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);
    private static final String BASE_URL = Config.get("IMAGE_API_HOST");

    public static void sendImage(@NotNull TextChannel channel, byte[] bytes, String fileName) {
        channel.sendFile(bytes, fileName).queue((s) -> {
        }, (e) -> {
        });
    }

    public static String getFilter(String filterName, String image) {
        String endpoint = BASE_URL + "/filters/" + filterName;
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(endpoint))
                .newBuilder()
                .addQueryParameter("image", image);
        return urlBuilder.build().toString();
    }

    public static String getGenerator(String genName, String image) {
        String endpoint = BASE_URL + "/generators/" + genName;
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(endpoint))
                .newBuilder()
                .addQueryParameter("image", image);
        return urlBuilder.build().toString();
    }

    public static String getGenerator(String genName, String image1, String image2) {
        String endpoint = BASE_URL + "/generators/" + genName;
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(endpoint))
                .newBuilder()
                .addQueryParameter("image1", image1)
                .addQueryParameter("image2", image2);
        return urlBuilder.build().toString();
    }

    public static String getPodiumGen(String[] avatarArr, String[] nameArr) {
        String endpoint = BASE_URL + "/generators/podium";
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(endpoint))
                .newBuilder()
                .addQueryParameter("avatar1", avatarArr[0])
                .addQueryParameter("avatar2", avatarArr[1])
                .addQueryParameter("avatar3", avatarArr[2])
                .addQueryParameter("name1", nameArr[0])
                .addQueryParameter("name2", nameArr[1])
                .addQueryParameter("name3", nameArr[2]);
        return urlBuilder.build().toString();
    }

    public static String getTextGenerator(String genName, String text) {
        String endpoint = BASE_URL + "/generators/" + genName;
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(endpoint))
                .newBuilder()
                .addQueryParameter("text", text);
        return urlBuilder.build().toString();
    }

    public static void sendImageWithMessage(@NotNull TextChannel channel, byte[] bytes, String fileName, String message) {
        if (message == null) {
            sendImage(channel, bytes, fileName);
            return;
        }

        channel.sendMessage(message).addFile(bytes, fileName).queue((s) -> {
        }, (e) -> {
        });
    }

    public static void embedImage(TextChannel channel, EmbedBuilder embed, byte[] bytes, ImageType type) {
        embedImage(channel, bytes, embed, type);
    }

    public static void embedImage(TextChannel channel, byte[] bytes, ImageType type) {
        embedImage(channel, bytes, null, type);
    }

    @NotNull
    public static String getImageFromCommand(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        String url = null;

        if (!ctx.getMessage().getAttachments().isEmpty())
            url = tryGetAttachment(ctx);

        if (url == null && args.isEmpty())
            url = getAvatarUrl(ctx.getAuthor());

        if (url == null && MiscUtils.isURL(args.get(0)))
            url = tryGetUrl(ctx, args.get(0));

        if (url == null && !ctx.getMessage().getMentionedUsers().isEmpty())
            url = getAvatarUrl(ctx.getMessage().getMentionedUsers().get(0));

        if (url == null) {
            final List<Member> memberMentions = FinderUtil.findMembers(ctx.getArgsJoined(), ctx.getGuild());

            if (!memberMentions.isEmpty())
                url = getAvatarUrl(memberMentions.get(0).getUser());
        }

        if (url == null)
            url = getAvatarUrl(ctx.getAuthor());

        return url;
    }

    @Nullable
    private static String tryGetAttachment(CommandContext ctx) {
        final Message.Attachment attachment = ctx.getMessage().getAttachments().get(0);
        final File file = new File(attachment.getFileName());
        String mimetype = null;

        try {
            mimetype = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mimetype == null || !mimetype.split("/")[0].equals("image")) {
            ctx.reply("That file does not look like an image");
            return null;
        }

        return attachment.getUrl();
    }

    @Nullable
    private static String tryGetUrl(CommandContext ctx, String s) {
        try {
            return new URL(s).toString();
        } catch (MalformedURLException ignored) {
            ctx.reply("That does not look like a valid url");
            return null;
        }
    }

    @NotNull
    private static String getAvatarUrl(User user) {
        return user.getEffectiveAvatarUrl() + "?size=256";
    }

    private static void embedImage(TextChannel channel, byte[] bytes, EmbedBuilder eb, ImageType type) {
        EmbedBuilder embed = (eb != null) ? eb : EmbedUtils.getDefaultEmbed()
                .setColor(Constants.TRANSPARENT_EMBED);

        if (bytes == null) {
            BotUtils.sendErrorEmbed(channel, Constants.API_ERROR);
            return;
        }

        if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS)) {
            BotUtils.sendMsg(channel, "I need permission to `attach files` and `embed links` in order for this command to work.");
            return;
        }

        String attachName = type.getFileName();
        embed.setImage("attachment://" + attachName);

        channel.sendMessage(embed.build()).addFile(bytes, attachName).queue((__) -> {
        }, (e) -> LOGGER.error("Sending image failed: " + e.getMessage()));

    }

}
