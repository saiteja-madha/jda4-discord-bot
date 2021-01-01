package bot.command.commands;

import bot.command.CommandContext;
import bot.command.ICommand;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

public class PingCommand extends ICommand {

    public PingCommand() {
        this.name = "ping";
        this.help = "Shows the current ping from the bot to the discord servers";
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        JDA jda = ctx.getJDA();

        jda.getRestPing().queue(
                (ping) -> ctx.reply(String.format("Reset ping: %sms\nWS ping: %sms", ping, jda.getGatewayPing()))
        );
    }

}
