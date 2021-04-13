package bot.commands.admin;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class FlagtrCommand extends ICommand {

    public FlagtrCommand() {
        this.name = "flagtr";
        this.help = "enable or disable translation by reaction";
        this.minArgsCount = 1;
        this.usage = "<#channel(s) | OFF>";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        String input = ctx.getArgs().get(0);

        if (input.equalsIgnoreCase("OFF") || input.equalsIgnoreCase("none")) {
            DataSource.INS.updateTranslationChannels(ctx.getGuild().getId(), null);
            ctx.replyWithSuccess("Success. Configuration saved!");
        } else {
            List<TextChannel> menChannels = ctx.getMessage().getMentionedChannels();

            if (menChannels.isEmpty()) {
                ctx.reply("Please mention the channels where translation by flags should be enabled");
                return;
            }

            List<String> collect = menChannels.stream().map(ISnowflake::getId).collect(Collectors.toList());
            DataSource.INS.updateTranslationChannels(ctx.getGuild().getId(), collect);

            ctx.replyWithSuccess("Successfully updated flag translation channels");
        }


    }

}