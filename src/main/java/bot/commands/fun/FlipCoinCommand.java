package bot.commands.fun;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FlipCoinCommand extends ICommand {

    public FlipCoinCommand() {
        this.name = "flipcoin";
        this.usage = "flips a coin heads or tails";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.FUN;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        boolean isHeads = new Random().nextBoolean();

        String HEADS = "https://i.imgur.com/HavOS7J.png";
        String TAILS = "https://i.imgur.com/u1pmQMV.png";

        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setAuthor(ctx.getAuthor().getAsTag(), null, ctx.getAuthor().getEffectiveAvatarUrl())
                .setImage(isHeads ? HEADS : TAILS);

        ctx.reply(embed.build());

    }
}
