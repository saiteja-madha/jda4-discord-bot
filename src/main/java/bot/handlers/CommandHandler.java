package bot.handlers;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.command.commands.admin.ReactionCommand;
import bot.command.commands.admin.SetPrefixCommand;
import bot.command.commands.admin.flag.FlagtrChannels;
import bot.command.commands.admin.flag.FlagtrCommand;
import bot.command.commands.admin.reaction_role.AddReactionRoleCommand;
import bot.command.commands.admin.reaction_role.RemoveReactionRoleCommand;
import bot.command.commands.fun.*;
import bot.command.commands.information.*;
import bot.command.commands.moderation.*;
import bot.command.commands.utility.*;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class CommandHandler {

    private final ArrayList<ICommand> commands = new ArrayList<>();
    private final HashMap<String, Integer> commandIndex = new HashMap<>();

    public CommandHandler(EventWaiter waiter) {

        addCommand(new ReactionCommand());

        // INFORMATION COMMANDS
        addCommand(new AvatarCommand());
        addCommand(new BotInfoCommand());
        addCommand(new ChannelInfoCommand());
        addCommand(new GuildInfoCommand());
        addCommand(new InviteCommand());
        addCommand(new PingCommand());
        addCommand(new RoleInfoCommand());
        addCommand(new UptimeCommand());
        addCommand(new UserInfoCommand());

        // UTILITY COMMANDS
        addCommand(new GithubCommand());
        addCommand(new HasteCommand());
        addCommand(new HelpCommand());
        addCommand(new TranslateCodes(waiter));
        addCommand(new TranslateCommand());
        addCommand(new UrbanCommand());

        // FUN COMMANDS
        addCommand(new FlipCoinCommand());
        addCommand(new FlipTextCommand());
        addCommand(new CatCommand());
        addCommand(new DogCommand());
        addCommand(new AnimalCommand());
        addCommand(new JokeCommand());
        addCommand(new MemeCommand());

        // MODERATION COMMANDS
        addCommand(new KickCommand());
        addCommand(new SoftBanCommand());
        addCommand(new BanCommand());
        addCommand(new PurgeAttachmentCommand());
        addCommand(new PurgeBotsCommand());
        addCommand(new PurgeCommand());
        addCommand(new PurgeLinksCommand());
        addCommand(new PurgeUserCommand());

        // ADMIN COMMANDS
        addCommand(new SetPrefixCommand());
        addCommand(new FlagtrCommand());
        addCommand(new FlagtrChannels());
        addCommand(new AddReactionRoleCommand());
        addCommand(new RemoveReactionRoleCommand());

    }

    private void addCommand(ICommand cmd) {
        int index = this.commands.size();

        if (this.commandIndex.containsKey(cmd.getName())) {
            throw new IllegalArgumentException(String.format("Command name \"%s\" is already in use", cmd.getName()));
        }

        for (String alias : cmd.getAliases()) {
            if (this.commandIndex.containsKey(alias)) {
                throw new IllegalArgumentException(String.format("Alias: %s in Command: \"%s\" is already used!", alias, cmd.getName()));
            }
            this.commandIndex.put(alias, index);
        }

        this.commandIndex.put(cmd.getName(), index);
        this.commands.add(index, cmd);

    }

    public ArrayList<ICommand> getCommands() {
        return commands;
    }

    @Nullable
    public ICommand getCommand(String search) {
        int i = this.commandIndex.getOrDefault(search.toLowerCase(), -1);
        return i != -1 ? this.commands.get(i) : null;
    }

    public void handle(GuildMessageReceivedEvent event, String prefix) {
        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(prefix), "")
                .split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null) {
            List<String> args = Arrays.asList(split).subList(1, split.length);
            CommandContext ctx = new CommandContext(event, args, invoke, prefix, this);
            cmd.run(ctx);
        }

    }

}
