package bot.command.commands.admin;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class RemoveReactionRoleCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {

        final TextChannel channel = ctx.getChannel();
        final Message message = ctx.getMessage();
        final List<String> args = ctx.getArgs();
        List<TextChannel> channels = message.getMentionedChannels();

        if (args.size() < 2 || channels.isEmpty()) {
            channel.sendMessage("Missing arguments").queue();
            return;
        }

        TextChannel tc = channels.get(0);
        String messageIdString = args.get(1);

        try {
            Long messageId = Long.parseLong(messageIdString);
            ctx.getMessage().clearReactions().queue();

            DataSource.INS.removeReactionRole(channel.getGuild().getIdLong(), channel.getIdLong(), messageId, null);
            channel.sendMessage("Removed reaction role!").queue();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public String getName() {
        return "removerr";
    }

    @Override
    public String getHelp() {
        return "Remove reaction role to the mentioned message\n" +
                "```Usage: [prefix]removerr <#channel> <messageid> <emote> <@role>```";
    }

}
