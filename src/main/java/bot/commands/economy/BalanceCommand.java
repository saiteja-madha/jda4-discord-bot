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

import java.util.Collections;
import java.util.List;

public class BalanceCommand extends ICommand {

    public BalanceCommand() {
        this.name = "balance";
        this.help = "shows your current coin balance";
        this.aliases = Collections.singletonList("bal");
        this.usage = "[@user]";
        this.category = CommandCategory.ECONOMY;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        Member target = ctx.getMember();
        List<Member> mentionedMembers = ctx.getMessage().getMentionedMembers();

        if (!mentionedMembers.isEmpty()) {
            target = mentionedMembers.get(0);
            if (target.getUser().isBot()) {
                ctx.reply("You cannot use this command on bots!");
                return;
            }
        }

        final int points = DataSource.INS.getEconomy(target).coins;

        final EmbedBuilder embed = EmbedUtils.defaultEmbed()
                .setAuthor(target.getEffectiveName(), null, target.getUser().getEffectiveAvatarUrl())
                .setDescription("**Coin Balance:** " + points + Constants.CURRENCY);

        if (!target.equals(ctx.getMember()))
            embed.setFooter("Requested By: " + ctx.getAuthor().getAsTag());

        ctx.reply(embed.build());

    }
}
