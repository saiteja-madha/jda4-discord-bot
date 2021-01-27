package bot.commands.utility;

import bot.command.CommandContext;
import bot.command.ICommand;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class HelpCommand extends ICommand {

    public HelpCommand() {
        this.name = "help";
        this.help = "Shows the list with commands in the bot";
        this.usage = "<command>";
        this.aliases = Arrays.asList("commands", "cmds", "commandlist");
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            StringBuilder builder = new StringBuilder()
                    .append("List of commands\n");

            ctx.getCmdHandler().getCommands().stream().map(ICommand::getName).forEach(
                    (it) -> builder.append('`')
                            .append(ctx.getPrefix())
                            .append(it)
                            .append("`\n")
            );

            ctx.reply(builder.toString());
            return;
        }

        String search = args.get(0);
        ICommand command = ctx.getCmdHandler().getCommand(search);

        if (command == null) {
            ctx.reply("Nothing found for " + search);
            return;
        }

        command.sendUsage(ctx);

    }

}
