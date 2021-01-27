package bot.commands.information;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.utils.MiscUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class UptimeCommand extends ICommand {

    public UptimeCommand() {
        this.name = "uptime";
        this.help = "shows bot's uptime";
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeMXBean.getUptime();
        long uptimeInSeconds = uptime / 1000;
        final String uptimeString = MiscUtils.formatTime(uptimeInSeconds);

        EmbedBuilder embed = EmbedUtils.defaultEmbed()
                .setTitle("I am online since")
                .setDescription("```" + uptimeString + "```");

        ctx.reply(embed.build());

    }

}
