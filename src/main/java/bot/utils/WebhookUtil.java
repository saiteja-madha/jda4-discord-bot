package bot.utils;

import bot.Constants;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.time.ZonedDateTime;

public class WebhookUtil {

    private final WebhookClient client;

    public WebhookUtil(String url) {
        client = new WebhookClientBuilder(url).build();
    }

    public void sendMsg(String avatar, String name, String message, WebhookEmbed embed) {

        WebhookMessage msg = new WebhookMessageBuilder()
                .setAvatarUrl(avatar)
                .setUsername(name)
                .setContent(message)
                .addEmbeds(embed)
                .build();

        client.send(msg);
    }

    public void sendMsg(String avatar, String name, WebhookEmbed embed) {
        sendMsg(avatar, name, null, embed);
    }

    public void sendWebhook(Member owner, Guild guild, Action action) {
        String title = null;
        WebhookEmbed embed = null;

        String name = owner == null ? "Unknown" : owner.getUser().getAsTag();
        String id = owner == null ? "?" : owner.getId();

        switch (action) {
            case JOIN:
                title = "Join";
                embed = new WebhookEmbedBuilder()
                        .setColor(Constants.SUCCESS_EMBED)
                        .setThumbnailUrl(guild.getIconUrl())
                        .setTitle(new WebhookEmbed.EmbedTitle("Guild Joined", null))
                        .addField(new WebhookEmbed.EmbedField(
                                false, "Name", guild.getName()
                        ))
                        .addField(new WebhookEmbed.EmbedField(
                                false, "ID", guild.getId()
                        ))
                        .addField(new WebhookEmbed.EmbedField(
                                false,
                                "Owner",
                                String.format(
                                        "%s | %s ",
                                        name,
                                        id
                                )
                        ))
                        .addField(new WebhookEmbed.EmbedField(
                                false,
                                "Members",
                                String.format(
                                        "```yaml\n" +
                                                "Total: %d\n" +
                                                "```",
                                        guild.getMemberCount()
                                )
                        ))
                        .setFooter(new WebhookEmbed.EmbedFooter(
                                String.format(
                                        "Guild #%d",
                                        guild.getJDA().getGuildCache().size()
                                ),
                                null
                        ))
                        .setTimestamp(ZonedDateTime.now())
                        .build();
                break;

            case LEAVE:
                title = "Leave";
                embed = new WebhookEmbedBuilder()
                        .setColor(Constants.ERROR_EMBED)
                        .setThumbnailUrl(guild.getIconUrl())
                        .setTitle(new WebhookEmbed.EmbedTitle("Guild Left", null))
                        .addField(new WebhookEmbed.EmbedField(
                                false, "Name", guild.getName()
                        ))
                        .addField(new WebhookEmbed.EmbedField(
                                false, "ID", guild.getId()
                        ))
                        .addField(new WebhookEmbed.EmbedField(
                                false, "Owner", String.format(
                                "%s | %s",
                                name,
                                id
                        )
                        ))
                        .addField(new WebhookEmbed.EmbedField(
                                false,
                                "Members",
                                String.format(
                                        "```yaml\n" +
                                                "Total: %d\n" +
                                                "```",
                                        guild.getMemberCount()
                                )
                        ))
                        .setFooter(new WebhookEmbed.EmbedFooter(
                                String.format(
                                        "Guild #%d",
                                        guild.getJDA().getGuildCache().size()
                                ),
                                null
                        ))
                        .setTimestamp(ZonedDateTime.now())
                        .build();
                break;

        }

        this.sendMsg(
                guild.getSelfMember().getUser().getEffectiveAvatarUrl(),
                title,
                null,
                embed
        );
    }

    public enum Action {
        JOIN,
        LEAVE,
        AUTO_LEAVE
    }

}
