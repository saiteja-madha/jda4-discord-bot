package bot.handlers;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.commands.admin.*;
import bot.commands.admin.flag.FlagtrChannels;
import bot.commands.admin.flag.FlagtrCommand;
import bot.commands.admin.mod_config.MaxWarningsCommand;
import bot.commands.admin.reaction_role.AddReactionRoleCommand;
import bot.commands.admin.reaction_role.RemoveReactionRoleCommand;
import bot.commands.economy.BalanceCommand;
import bot.commands.economy.DailyCommand;
import bot.commands.economy.GambleCommand;
import bot.commands.economy.TransferCommand;
import bot.commands.fun.*;
import bot.commands.image.filters.*;
import bot.commands.image.generators.*;
import bot.commands.information.*;
import bot.commands.moderation.*;
import bot.commands.social.ReputationCommand;
import bot.commands.utility.*;
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

        // SOCIAL COMMANDS
        addCommand(new ReputationCommand());

        // ECONOMY COMMANDS
        addCommand(new BalanceCommand());
        addCommand(new DailyCommand());
        addCommand(new GambleCommand());
        addCommand(new TransferCommand());

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
        addCommand(new CovidCommand());
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

        // IMAGE COMMANDS
        addCommand(new Blur());
        addCommand(new Contrast());
        addCommand(new Gay());
        addCommand(new GreyScale());
        addCommand(new Invert());
        addCommand(new Sepia());
        addCommand(new Ad());
        addCommand(new Affect());
        addCommand(new Approved());
        addCommand(new BatSlap());
        addCommand(new Beautiful());
        addCommand(new Bed());
        addCommand(new Bobross());
        addCommand(new ConfusedStonk());
        addCommand(new Delete());
        addCommand(new DiscordBlack());
        addCommand(new DiscordBlue());
        addCommand(new DoubleStonk());
        addCommand(new FacePalm());
        addCommand(new Frame());
        addCommand(new Hitler());
        addCommand(new Jail());
        addCommand(new Karaba());
        addCommand(new Kiss());
        addCommand(new MMS());
        addCommand(new NotStonk());
        addCommand(new Poutine());
        addCommand(new Rejected());
        addCommand(new RIP());
        addCommand(new Spank());
        addCommand(new Stonk());
        addCommand(new Tatoo());
        addCommand(new Trash());
        addCommand(new Wanted());
        addCommand(new Podium());

        // MODERATION COMMANDS
        addCommand(new BanCommand());
        addCommand(new ClearWarnCommand());
        addCommand(new DeafenCommand());
        addCommand(new KickCommand());
        addCommand(new MuteCommand());
        addCommand(new PurgeAttachmentCommand());
        addCommand(new PurgeBotsCommand());
        addCommand(new PurgeCommand());
        addCommand(new PurgeLinksCommand());
        addCommand(new PurgeUserCommand());
        addCommand(new SetNickCommand());
        addCommand(new SoftBanCommand());
        addCommand(new UnDeafenCommand());
        addCommand(new UnmuteCommand());
        addCommand(new VMuteCommand());
        addCommand(new VUnMuteCommand());
        addCommand(new WarnCommand());
        addCommand(new WarningsCommand(waiter));

        // ADMIN COMMANDS
        addCommand(new SetPrefixCommand());
        addCommand(new ModLogChannel());
        addCommand(new MaxWarningsCommand());
        addCommand(new XPSystem());
        addCommand(new FlagtrCommand());
        addCommand(new FlagtrChannels());
        addCommand(new AddReactionRoleCommand());
        addCommand(new RemoveReactionRoleCommand());
        addCommand(new CounterSetup());
        addCommand(new TicketSetup(waiter));

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
