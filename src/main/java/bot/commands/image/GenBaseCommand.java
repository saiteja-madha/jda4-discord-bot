package bot.commands.image;

import bot.Constants;
import bot.command.CommandCategory;
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

public abstract class GenBaseCommand extends ICommand {

    public GenBaseCommand() {
        this.name = getClass().getSimpleName().toLowerCase();
        this.usage = "<imageurl>/<@mention>/<attachment>";
        this.help = "generates a meme for the provided image";
        this.category = CommandCategory.IMAGE;
        this.cooldown = 5;
    }

    @Override
    public void handle(@Nonnull @NotNull CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final User author = ctx.getAuthor();
        final String imageUrl = ImageUtils.getImageFromCommand(ctx);

        if (this.getImageCount() == 1) {
            WebUtils.ins.getByteStream(ImageUtils.getGenerator(this.getGenName(), imageUrl)).async(bytes -> {
                EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                        .setColor(Constants.TRANSPARENT_EMBED)
                        .setFooter("Requested by: " + author.getAsTag());

                ImageUtils.embedImage(channel, embed, bytes, this.getImageType());

            });
        } else if (this.getImageCount() == 2) {
            WebUtils.ins.getByteStream(ImageUtils.getGenerator(this.getGenName(), author.getEffectiveAvatarUrl(), imageUrl)).async(bytes -> {
                EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                        .setColor(Constants.TRANSPARENT_EMBED)
                        .setFooter("Requested by: " + author.getAsTag());

                ImageUtils.embedImage(channel, embed, bytes, this.getImageType());

            }, err -> System.out.println(err.getMessage()));
        }

    }

    public int getImageCount() {
        return 1;
    }

    public String getGenName() {
        return this.name;
    }

    public ImageType getImageType() {
        return ImageType.IMAGE;
    }

}
