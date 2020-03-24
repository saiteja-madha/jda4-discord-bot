package bot.command.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Arrays;
import java.util.List;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.main.CommandManager;
import bot.main.Config;
import bot.main.MemoryMap;

public class HelpCommand implements ICommand {

    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(CommandContext ctx) {
        List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();

        if (args.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            String prefix = MemoryMap.PREFIXES.get(ctx.getGuild().getIdLong());

            builder.append("List of commands\n");

            manager.getCommands().stream().map(ICommand::getName).forEach(
                    (it) -> builder.append('`')
                    				.append(prefix)
                    				.append(it)
                    				.append("`\n")
            );

            channel.sendMessage(builder.toString()).queue();
            return;
        }

        String search = args.get(0);
        ICommand command = manager.getCommand(search);

        if (command == null) {
            channel.sendMessage("Nothing found for " + search).queue();
            return;
        }
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle((Config.get("prefix")) + command.getName())
        	.setColor(0x33FFBB)
        	.setDescription(command.getHelp().replace("[prefix]", MemoryMap.PREFIXES.get(ctx.getGuild().getIdLong())));
        
        channel.sendMessage(builder.build()).queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getHelp() {
        return "Shows the list with commands in the bot\n" +
                "```Usage: [prefix]help <command>```";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("commands", "cmds", "commandlist");
    }
}
