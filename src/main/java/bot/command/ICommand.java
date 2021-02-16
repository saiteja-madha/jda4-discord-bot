package bot.command;

import bot.utils.BotUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ICommand {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ICommand.class);

    protected String name = "";
    protected List<String> aliases = Collections.emptyList();
    protected String help = "No help available";
    protected boolean multilineHelp = false;
    protected String usage = "";
    protected int minArgsCount = 0;
    protected int maxCharCount = 3000;
    protected Permission[] userPermissions = new Permission[0];
    protected Permission[] botPermissions = new Permission[0];
    protected int cooldown = 0;
    protected CommandCategory category = CommandCategory.UNLISTED;

    public void run(@Nonnull CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        if (!ctx.getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;

        if (this.userPermissions.length > 0 && !ctx.getMember().hasPermission(channel, this.userPermissions)) {
            final String permissionsWord = "permission" + (this.userPermissions.length > 1 ? "s" : "");

            ctx.reply("You need the `" + parsePerms(this.userPermissions) + "` " + permissionsWord
                    + " for this command\nPlease contact your server administrator if this is incorrect.");
            return;
        }

        if (this.botPermissions.length > 0 && !ctx.getSelfMember().hasPermission(channel, this.botPermissions)) {
            final String permissionsWord = "permission" + (this.botPermissions.length > 1 ? "s" : "");
            ctx.reply("I need the `" + parsePerms(this.botPermissions) + "` " + permissionsWord
                    + " for this command to work\nPlease contact your server administrator about this.");
            return;
        }

        if (this.minArgsCount > 0 && (ctx.getArgs().isEmpty() || ctx.getArgs().size() < this.minArgsCount)) {
            this.sendUsageEmbed(ctx, "Insufficient Arguments");
            return;
        }

        if (ctx.getArgsJoined().length() > this.maxCharCount) {
            ctx.reply("Maximum character count for arguments cannot exceed `" + this.maxCharCount
                    + "` characters");
            return;
        }

        if (this.cooldown > 0) {
            String key = getCooldownKey(ctx);
            int remaining = ctx.getCmdHandler().getRemainingCooldown(key);
            if (remaining > 0) {
                this.sendCoolDown(ctx.getEvent(), remaining);
                return;
            } else ctx.getCmdHandler().applyCooldown(key, cooldown);
        }

        ctx.getEvent().getChannel().sendTyping().queue();
        handle(ctx);

    }


    public abstract void handle(@Nonnull CommandContext ctx);

    protected String parsePerms(Permission[] perms) {
        return Arrays.stream(perms).map(Permission::getName).collect(Collectors.joining("`, `"));
    }

    public String getName() {
        return this.name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void sendUsageEmbed(TextChannel channel, String prefix, String invoke, String title) {
        final StringBuilder str = new StringBuilder();

        // Append Usage
        if (this.multilineHelp)
            str.append(this.usage.replace("{p}", prefix)
                    .replace("{i}", invoke));

        else
            str.append("**Usage:**\n")
                    .append("```css\n")
                    .append(prefix)
                    .append(invoke)
                    .append(" ")
                    .append(this.usage)
                    .append("```");

        // Append Help
        if (!this.help.equals(""))
            str.append("\n**Help**: ").append(this.help.replace("{p}", prefix)).append("\n");

        // Append cooldown
        if (this.cooldown > 0)
            str.append("**Cooldown**: ").append(this.cooldown).append(" seconds\n");

        EmbedBuilder eb = EmbedUtils.getDefaultEmbed()
                .setAuthor(title)
                .setDescription(str.toString());

        BotUtils.sendEmbed(channel, eb.build());

    }

    public void sendUsageEmbed(@Nonnull CommandContext ctx, String title) {
        this.sendUsageEmbed(ctx.getChannel(), ctx.getPrefix(), ctx.getInvoke(), title);
    }

    private void sendCoolDown(GuildMessageReceivedEvent event, int seconds) {
        BotUtils.sendEmbed(
                event.getChannel(),
                EmbedUtils.getDefaultEmbed()
                        .setAuthor("Kindly wait " + seconds + " seconds before using this command")
                        .build(),
                5
        );
    }

    private String getCooldownKey(CommandContext ctx) {
        return this.name + "|" + "U:" + ctx.getAuthor().getId();
    }

    public CommandCategory getCategory() {
        return this.category;
    }

}