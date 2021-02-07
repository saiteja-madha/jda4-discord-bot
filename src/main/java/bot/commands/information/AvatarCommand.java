package bot.commands.information;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class AvatarCommand extends ICommand {

    public AvatarCommand() {
        this.name = "avatar";
        this.help = "displays avatar information about the user";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.INFORMATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        User user = ctx.getAuthor();

        if (!ctx.getMessage().getMentionedUsers().isEmpty())
            user = ctx.getMessage().getMentionedUsers().get(0);

        String avatar = user.getEffectiveAvatarUrl();
        String x64 = avatar + "?size=64";
        String x128 = avatar + "?size=128";
        String x256 = avatar + "?size=256";
        String x512 = avatar + "?size=512";
        String x1024 = avatar + "?size=1024";
        String x2048 = avatar + "?size=2048";

        final EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setAuthor("Avatar of " + user.getAsTag())
                .setDescription("Links: "
                        + Constants.CIRCLE_BULLET + " [x64](" + x64 + ") "
                        + Constants.CIRCLE_BULLET + " [x128](" + x128 + ") "
                        + Constants.CIRCLE_BULLET + " [x256](" + x256 + ") "
                        + Constants.CIRCLE_BULLET + " [x512](" + x512 + ") "
                        + Constants.CIRCLE_BULLET + " [x1024](" + x1024 + ") "
                        + Constants.CIRCLE_BULLET + " [x2048](" + x2048 + ") "
                )
                .setImage(x256);

        if (!user.equals(ctx.getAuthor()))
            embed.setFooter("Requested By: " + ctx.getAuthor().getAsTag());

        ctx.reply(embed.build());

    }

}
