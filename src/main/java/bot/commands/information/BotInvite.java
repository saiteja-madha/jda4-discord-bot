package bot.commands.information;

import bot.Config;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.utils.BotUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class BotInvite extends ICommand {

    public BotInvite() {
        this.name = "botinvite";
        this.help = "get the bot's invite";
        this.category = CommandCategory.INFORMATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        if (!ctx.getArgs().isEmpty())
            return;

        final String inviteUrl = Config.get("BOT_INVITE");

        String desc = "";
        desc += "Support Server: [Join here](" + Config.get("DISCORD_INVITE") + ")" + "\n";
        desc += "Invite Link: [Add me here](" + inviteUrl + ")" + "\n";

        final EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setAuthor("Wew! I made it threw the ~waves~")
                .setDescription(desc);

        BotUtils.sendDM(ctx.getAuthor(), embed,
                (s) -> ctx.reply("My invite link has been sent in DM!"),
                e -> ctx.reply("Err.. I could not reach you! Is your DM blocked?"));

    }

}
