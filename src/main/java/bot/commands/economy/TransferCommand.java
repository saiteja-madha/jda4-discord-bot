package bot.commands.economy;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

public class TransferCommand extends ICommand {

    public TransferCommand() {
        this.name = "transfer";
        this.help = "transfer coins to other user";
        this.usage = "<coins> <@user>";
        this.minArgsCount = 2;
        this.category = CommandCategory.ECONOMY;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        if (ctx.getMessage().getMentionedMembers().isEmpty()) {
            ctx.reply("Incorrect arguments passed! Please mention the user whom you want to transfer coins as well");
            return;
        }

        try {
            int coinsToTransfer = Integer.parseInt(ctx.getArgs().get(0));
            Member target = ctx.getMessage().getMentionedMembers().get(0);

            if (coinsToTransfer < 0) {
                ctx.reply("Oops! You cannot transfer negative value");
                return;
            }

            if (target.getUser().isBot()) {
                ctx.reply("You cannot transfer coins to bots!");
                return;
            }

            if (target.equals(ctx.getMember())) {
                ctx.reply("Err! You cannot transfer coins to self");
                return;
            }

            int coins = DataSource.INS.getEconomy(ctx.getMember()).coins;

            if (coinsToTransfer > coins) {
                ctx.reply("Insufficient coin balance! You only have " + coins + Constants.CURRENCY);
                return;
            }

            final int[] sourceBal = DataSource.INS.removeCoins(ctx.getMember(), coinsToTransfer);
            final int[] targetBal = DataSource.INS.addCoins(target, coinsToTransfer);

            EmbedBuilder embed = EmbedUtils.defaultEmbed()
                    .setAuthor("Coins Transferred")
                    .setDescription(String.format("**Updated Balance:**" +
                                    "\n %s %s: %s" +
                                    "\n %s %s: %s",
                            Constants.ARROW, ctx.getMember().getEffectiveName(), sourceBal[1] + Constants.CURRENCY,
                            Constants.ARROW, target.getEffectiveName(), targetBal[1] + Constants.CURRENCY));

            ctx.reply(embed.build());

        } catch (NumberFormatException ex) {
            ctx.reply("Please enter a valid amount of coins to transfer");
        }

    }

}
