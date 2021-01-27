package bot.commands.image.generators;

import bot.Constants;
import bot.command.CommandContext;
import bot.commands.image.GenBaseCommand;
import bot.utils.ImageUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class Podium extends GenBaseCommand {

    public Podium() {
        this.name = getClass().getSimpleName().toLowerCase();
        this.usage = "<@member1> <@member2> <@member3>";
        this.minArgsCount = 3;
        this.help = "generates a meme for the provided image";
    }

    @Override
    public void handle(@Nonnull @NotNull CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final User author = ctx.getAuthor();

        List<Member> mentionedMembers = ctx.getMessage().getMentionedMembers();
        if (mentionedMembers.size() < 3) {
            ctx.reply("Please mention 3 members");
            return;
        }

        String[] nameArr = new String[]{
                mentionedMembers.get(0).getEffectiveName(),
                mentionedMembers.get(1).getEffectiveName(),
                mentionedMembers.get(2).getEffectiveName()
        };

        String[] avatarArr = new String[]{
                mentionedMembers.get(0).getUser().getEffectiveAvatarUrl(),
                mentionedMembers.get(1).getUser().getEffectiveAvatarUrl(),
                mentionedMembers.get(2).getUser().getEffectiveAvatarUrl()
        };

        WebUtils.ins.getByteStream(ImageUtils.getPodiumGen(avatarArr, nameArr)).async(bytes -> {
            EmbedBuilder embed = EmbedUtils.defaultEmbed()
                    .setColor(Constants.TRANSPARENT_EMBED)
                    .setFooter("Requested by: " + author.getAsTag());

            ImageUtils.embedImage(channel, embed, bytes, this.getImageType());

        });


    }

}
