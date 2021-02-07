package bot.commands.economy;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.utils.MiscUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class GambleCommand extends ICommand {

    public GambleCommand() {
        this.name = "gamble";
        this.usage = "<amount>";
        this.help = "try your luck by gambling";
        this.aliases = Collections.singletonList("slot");
        this.minArgsCount = 1;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.ECONOMY;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        int betAmount;
        try {
            betAmount = Integer.parseInt(ctx.getArgs().get(0));
            if (betAmount < 10) {
                ctx.reply("Bet amount cannot be less than 10");
                return;
            }
        } catch (NumberFormatException ex) {
            ctx.reply("Please provide a valid number input for bet-amount");
            return;
        }

        final int coins = DataSource.INS.getEconomy(ctx.getMember()).coins;

        if (betAmount > coins) {
            ctx.reply("You do not have enough coins to gamble!\n" +
                    "**Coin Balance:** " + coins + Constants.CURRENCY);
            return;
        }

        String slot1 = getEmoji(), slot2 = getEmoji(), slot3 = getEmoji();
        String str = "**Gamble Amount:** " + betAmount + "₪\n" +
                "**Multiplier:** 1.5x\n" +
                "╔═════════╗\n" +
                String.format("║ %s ║ %s ║ %s \n", getEmoji(), getEmoji(), getEmoji()) +
                "╠═════════╣\n" +
                String.format("║ %s ║ %s ║ %s ⟸\n", slot1, slot2, slot3) +
                "╠═════════╣\n" +
                String.format("║ %s ║ %s ║ %s \n", getEmoji(), getEmoji(), getEmoji()) +
                "╚═════════╝";

        final int reward = calculateReward(betAmount, slot1, slot2, slot3);
        String result = ((reward > 0) ? ("You won " + reward) : ("You lost " + betAmount)) + Constants.CURRENCY;
        int balance = coins + (reward - betAmount);

        DataSource.INS.addCoins(ctx.getMember(), (reward - betAmount));

        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setAuthor(ctx.getAuthor().getName(), null, ctx.getAuthor().getEffectiveAvatarUrl())
                .setDescription(str)
                .setFooter(result + "\nUpdated Balance: " + balance + Constants.CURRENCY);

        ctx.reply(embed.build());

    }

    private int calculateReward(int amount, String var1, String var2, String var3) {
        if (var1.equals(var2) && var2.equals(var3))
            return (int) (2.25 * amount);
        if (var1.equals(var2) || var2.equals(var3) || var1.equals(var3))
            return (int) (1.5 * amount);
        else
            return 0;
    }

    private String getEmoji() {
        int ran = MiscUtils.getRandInt(1, 9);
        switch (ran) {
            case 1:
                return "\uD83C\uDF52";
            case 2:
                return "\uD83C\uDF4C";
            case 3:
                return "\uD83C\uDF51";
            case 4:
                return "\uD83C\uDF45";
            case 5:
                return "\uD83C\uDF49";
            case 6:
                return "\uD83C\uDF47";
            case 7:
                return "\uD83C\uDF53";
            case 8:
                return "\uD83C\uDF50";
            case 9:
                return "\uD83C\uDF4D";
            default:
                return "";
        }
    }

}
