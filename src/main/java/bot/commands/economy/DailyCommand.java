package bot.commands.economy;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.database.objects.Economy;
import bot.utils.MiscUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DailyCommand extends ICommand {

    public DailyCommand() {
        this.name = "daily";
        this.help = "receive a daily bonus";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.ECONOMY;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Economy economy = DataSource.INS.getEconomy(ctx.getMember());

        EmbedBuilder embed = EmbedUtils.defaultEmbed()
                .setAuthor(ctx.getMember().getEffectiveName(), null, ctx.getAuthor().getEffectiveAvatarUrl());

        final Instant current = Instant.now();

        int streakCheck = 0;
        if (economy.dailyTimestamp != null) {
            int dailyCheck = economy.dailyTimestamp.plus(1, ChronoUnit.DAYS).compareTo(current);
            if (dailyCheck > 0) {
                final String remainingTime = MiscUtils.getRemainingTime(economy.dailyTimestamp);
                embed.setDescription("You can again run this command in `" + remainingTime + "`");
                ctx.reply(embed.build());
                return;
            }
            streakCheck = economy.dailyTimestamp.plus(2, ChronoUnit.DAYS).compareTo(current);
        }

        int streakUpdate = economy.dailyStreak;
        if (streakCheck > 0)
            streakUpdate += 1;
        else
            streakUpdate = 1;

        int[] points = DataSource.INS.updateDailyStreak(ctx.getMember(), 100, streakUpdate);

        embed.setDescription(String.format("You got %s%s as your daily reward\n" +
                "**Updated Balance:** %s%s", 100, Constants.CURRENCY, points[1], Constants.CURRENCY));

        ctx.reply(embed.build());

    }

}
