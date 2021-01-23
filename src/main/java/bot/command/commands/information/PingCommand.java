package bot.command.commands.information;

import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
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
                (ping) -> {
                    EmbedBuilder embed = EmbedUtils.defaultEmbed()
                            .setDescription(String.format("Reset ping: %sms\n" +
                                    "Websocket ping: %sms", ping, jda.getGatewayPing()));

                    ctx.reply(embed.build());

                }
        );
    }

}
