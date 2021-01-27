package bot.commands.image;

import bot.Constants;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.data.ImageType;
import bot.utils.ImageUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public abstract class FilterBaseCommand extends ICommand {

    public FilterBaseCommand() {
        this.name = getClass().getSimpleName().toLowerCase();
        this.usage = "<imageurl>/<@mention>/<attachment>";
        this.help = "generates filter for the provided image";
    }

    @Override
    public void handle(@Nonnull @NotNull CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final User author = ctx.getAuthor();
        final String imageUrl = ImageUtils.getImageFromCommand(ctx);

        WebUtils.ins.getByteStream(ImageUtils.getFilter(this.getFilterName(), imageUrl)).async(bytes -> {
            EmbedBuilder embed = EmbedUtils.defaultEmbed()
                    .setColor(Constants.TRANSPARENT_EMBED)
                    .setFooter("Requested by: " + author.getAsTag());

            ImageUtils.sendImage(channel, bytes, this.getImageType().getFileName());
            ImageUtils.embedImage(channel, embed, bytes, this.getImageType());

        });

    }

    public String getFilterName() {
        return this.name;
    }

    public ImageType getImageType() {
        return ImageType.IMAGE;
    }

}
