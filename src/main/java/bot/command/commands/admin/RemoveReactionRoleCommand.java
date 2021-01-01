package bot.command.commands.admin;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveReactionRoleCommand extends ICommand {

    public RemoveReactionRoleCommand() {
        this.name = "removerr";
        this.help = "Remove reaction role to the mentioned message";
        this.usage = "<#channel> <messageid>";
        this.argsCount = 2;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Message message = ctx.getMessage();
        final List<String> args = ctx.getArgs();
        List<TextChannel> channels = message.getMentionedChannels();

        if (channels.isEmpty()) {
            ctx.reply("Incorrect usage! Please mention the channel where the message exists");
            return;
        }

        TextChannel tc = channels.get(0);
        String messageIdString = args.get(1);

        try {
            long messageId = Long.parseLong(messageIdString);
            tc.retrieveMessageById(messageId).queue((msg) -> msg.clearReactions().queue((__) -> {
                try {
                    DataSource.INS.removeReactionRole(ctx.getGuild().getIdLong(), tc.getIdLong(), messageId, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ctx.reply("Removed reaction role!");
            }), (err) -> ctx.reply("Did you provide a valid messageId?"));

        } catch (Exception e) {
            ctx.reply("Failed to remove reaction role! Did you provide valid arguments?");
        }


    }

}
