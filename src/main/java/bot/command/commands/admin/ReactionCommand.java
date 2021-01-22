package bot.command.commands.admin;

import bot.command.CommandContext;
import bot.command.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReactionCommand extends ICommand {

    public ReactionCommand() {
        this.name = "react";
        this.help = "Reacts with an emoji to the mentioned message";
        this.usage = "<#channel> <messageid> <emote>";
        this.minArgsCount = 3;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Message message = ctx.getMessage();
        final List<String> args = ctx.getArgs();
        List<TextChannel> channels = message.getMentionedChannels();

        if (channels.isEmpty()) {
            ctx.reply("Please mention the channel where the messageId exists");
            return;
        }

        TextChannel tc = channels.get(0);
        String messageIdString = args.get(1);

        try {
            long messageId = Long.parseLong(messageIdString);
            String emote = args.get(2);
            tc.addReactionById(messageId, emote).queue();
        } catch (NumberFormatException e) {
            ctx.reply("Did you provide a valid messageId?");
        } catch (Exception ex) {
            ctx.reply("Failed to react! Did you provide valid arguments?");
        }
    }

}
