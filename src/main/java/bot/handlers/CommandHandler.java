package bot.handlers;

import bot.Bot;
import bot.Config;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.commands.admin.FlagtrCommand;
import bot.commands.admin.SetPrefixCommand;
import bot.commands.admin.XPSystem;
import bot.commands.admin.CounterSetup;
import bot.commands.admin.greeting.Farewell;
import bot.commands.admin.greeting.Welcome;
import bot.commands.admin.mod_config.MaxWarningsCommand;
import bot.commands.admin.mod_config.ModLogChannel;
import bot.commands.admin.reaction_role.AddReactionRoleCommand;
import bot.commands.admin.reaction_role.RemoveReactionRoleCommand;
import bot.commands.admin.TicketSetup;
import bot.commands.automod.*;
import bot.commands.economy.BalanceCommand;
import bot.commands.economy.DailyCommand;
import bot.commands.economy.GambleCommand;
import bot.commands.economy.TransferCommand;
import bot.commands.fun.*;
import bot.commands.image.filters.*;
import bot.commands.image.generators.*;
import bot.commands.image.text_generators.Achievement;
import bot.commands.image.text_generators.BeLikeBill;
import bot.commands.image.text_generators.Presentation;
import bot.commands.information.*;
import bot.commands.invites.*;
import bot.commands.moderation.*;
import bot.commands.owner.EvalCommand;
import bot.commands.owner.ShutDownCommand;
import bot.commands.owner.UsageCommand;
import bot.commands.social.ReputationCommand;
import bot.commands.utility.*;
import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandHandler extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    private final ArrayList<ICommand> commands = new ArrayList<>();
    private final HashMap<String, Integer> commandIndex = new HashMap<>();
    private final HashMap<String, OffsetDateTime> cooldowns = new HashMap<>();
    private final HashMap<String, Integer> uses = new HashMap<>();

    public CommandHandler(Bot bot) {

        // INVITE COMMANDS
        addCommand(new InvitesCommand());
        addCommand(new InviterCommand());
        addCommand(new InviteTracker(bot));
        addCommand(new InviteCodesCommand(bot.getWaiter()));
        addCommand(new AddInvitesRankCommand());
        addCommand(new InviteRanksCommand(bot.getWaiter()));
        addCommand(new RemInvitesRankCommand());
        addCommand(new AddInvitesCommand());
        addCommand(new ClearInvitesCommand());
        addCommand(new InviteImportCommand(bot.getWaiter()));

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
        addCommand(new BotInvite());
        addCommand(new PingCommand());
        addCommand(new RoleInfoCommand());
        addCommand(new UptimeCommand());
        addCommand(new UserInfoCommand());

        // UTILITY COMMANDS
        addCommand(new CovidCommand());
        addCommand(new GithubCommand());
        addCommand(new HelpCommand(bot.getWaiter()));
        addCommand(new TranslateCodes(bot.getWaiter()));
        addCommand(new TranslateCommand());
        addCommand(new UrbanCommand());
        addCommand(new ProxiesCommand());

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
        addCommand(new Achievement());
        addCommand(new BeLikeBill());
        addCommand(new Presentation());

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
        addCommand(new TempBanCommand());
        addCommand(new TempMuteCommand());
        addCommand(new UnDeafenCommand());
        addCommand(new UnmuteCommand());
        addCommand(new VMuteCommand());
        addCommand(new VUnMuteCommand());
        addCommand(new WarnCommand());
        addCommand(new WarningsCommand(bot.getWaiter()));

        // ADMIN COMMANDS
        addCommand(new SetPrefixCommand());
        addCommand(new ModLogChannel());
        addCommand(new MaxWarningsCommand());
        addCommand(new XPSystem());
        addCommand(new FlagtrCommand());
        addCommand(new AddReactionRoleCommand());
        addCommand(new RemoveReactionRoleCommand());
        addCommand(new CounterSetup());
        addCommand(new TicketSetup(bot.getWaiter()));
        addCommand(new Welcome(bot));
        addCommand(new Farewell(bot));

        // AUTOMOD COMMANDS
        addCommand(new AntiLinksCommand());
        addCommand(new AntiInvitesCommand());
        addCommand(new AutoModLog());
        addCommand(new AutoModStatus());
        addCommand(new MaxLinesCommand());
        addCommand(new MaxMentionsCommand());
        addCommand(new MaxRoleMentionsCommand());
        addCommand(new AntiGhostPingCommand());

        // OWNER COMMANDS
        addCommand(new ShutDownCommand(bot));
        addCommand(new UsageCommand(bot.getWaiter()));
        addCommand(new EvalCommand());

        LOGGER.info("{} commands loaded!", commands.size());

    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }

        GuildSettings settings = DataSource.INS.getSettings(event.getGuild().getId());
        String raw = event.getMessage().getContentRaw();

        if (raw.startsWith(settings.prefix)) {
            this.handle(event, settings.prefix);
        }

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

    private void handle(GuildMessageReceivedEvent event, String prefix) {
        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(prefix), "")
                .split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null) {
            if(cmd.getCategory() == CommandCategory.OWNER) {
                if (!event.getMessage().getAuthor().getId().equals(Config.get("OWNER_ID"))) return;
            }

            List<String> args = Arrays.asList(split).subList(1, split.length);
            CommandContext ctx = new CommandContext(event, args, invoke, prefix, this);
            uses.put(cmd.getName(), uses.getOrDefault(cmd.getName(), 0) + 1);
            cmd.run(ctx);
        }

    }

    public int getRemainingCooldown(String name) {
        if (cooldowns.containsKey(name)) {
            int time = (int) Math.ceil(OffsetDateTime.now().until(cooldowns.get(name), ChronoUnit.MILLIS) / 1000D);
            if (time <= 0) {
                cooldowns.remove(name);
                return 0;
            }
            return time;
        }
        return 0;
    }

    public void applyCooldown(String name, int seconds) {
        cooldowns.put(name, OffsetDateTime.now().plusSeconds(seconds));
    }

    public void cleanCooldowns() {
        OffsetDateTime now = OffsetDateTime.now();
        final int size = cooldowns.size();
        cooldowns.keySet().stream().filter((str) -> (cooldowns.get(str).isBefore(now))).collect(Collectors.toList())
                .forEach(cooldowns::remove);
        LOGGER.debug("Command cooldown cache cleared - {} keys released", size);
    }

    public int getCommandUses(String name) {
        return uses.getOrDefault(name, 0);
    }

    public void invalidateCooldown(String key) {
        cooldowns.remove(key);
    }

}
