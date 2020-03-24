package bot.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.command.commands.*;
import bot.command.commands.AddReactionRoleCommand;
import bot.command.commands.admin.*;
import bot.command.commands.admin.ReactionCommand;
import bot.command.commands.admin.RemoveReactionRoleCommand;
import bot.command.commands.fun.*;
import bot.command.commands.moderation.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandManager {
    private final List<ICommand> commands = new ArrayList<>();

    public CommandManager() {
        addCommand(new PingCommand());
        addCommand(new HelpCommand(this));
        addCommand(new HasteCommand());
        addCommand(new MemeCommand());
        addCommand(new CatCommand());
        addCommand(new DogCommand());
        addCommand(new AnimalCommand());
        addCommand(new JokeCommand());
        addCommand(new KickCommand());
        addCommand(new InstagramCommand());
        addCommand(new GithubCommand());
        addCommand(new WebhookCommand());
        addCommand(new SetPrefixCommand());
        addCommand(new ReactionCommand());
        addCommand(new AddReactionRoleCommand());
        addCommand(new RemoveReactionRoleCommand());
    }

    private void addCommand(ICommand cmd) {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));

        if (nameFound) {
            throw new IllegalArgumentException("A command with this name is already present");
        }

        commands.add(cmd);
    }

    public List<ICommand> getCommands() {
        return commands;
    }

    @Nullable
    public ICommand getCommand(String search) {
        String searchLower = search.toLowerCase();

        for (ICommand cmd : this.commands) {
            if (cmd.getName().equals(searchLower) || cmd.getAliases().contains(searchLower)) {
                return cmd;
            }
        }

        return null;
    }

    public void handle(GuildMessageReceivedEvent event, String prefix) {
        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(prefix), "")
                .split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null) {
            event.getChannel().sendTyping().queue();
            List<String> args = Arrays.asList(split).subList(1, split.length);

            CommandContext ctx = new CommandContext(event, args);

            cmd.handle(ctx);
        }
    }

}
