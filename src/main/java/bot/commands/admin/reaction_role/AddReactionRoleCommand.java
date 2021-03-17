package bot.commands.admin.reaction_role;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddReactionRoleCommand extends ICommand {

    private final Permission[] channelPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
            Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_EXT_EMOJI};

    public AddReactionRoleCommand() {
        this.name = "addrr";
        this.help = "Reacts with an emoji to the mentioned message";
        this.usage = "<#channel> <messageid> <emote> <@role>";
        this.minArgsCount = 4;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Message message = ctx.getMessage();
        final List<String> args = ctx.getArgs();
        final List<TextChannel> menChannels = message.getMentionedChannels();

        if (menChannels.isEmpty()) {
            ctx.reply("Incorrect usage! Missing arguments");
            return;
        }

        final TextChannel channel = menChannels.get(0);

        if (!ctx.getSelfMember().hasPermission(channel, this.channelPermissions)) {
            ctx.replyError("I need the following permissions in " + channel.getAsMention() + "\n" +
                    "`" + parsePerms(channelPermissions) + "`");
            return;
        }

        try {
            long messageId = Long.parseLong(args.get(1));
            Role role = null;

            if (!message.getMentionedRoles().isEmpty()) {
                role = message.getMentionedRoles().get(0);
            } else {
                final List<Role> rolesByName = ctx.getGuild().getRolesByName(args.get(3), true);
                if (!rolesByName.isEmpty()) {
                    role = rolesByName.get(0);
                }
            }

            if (role == null) {
                ctx.reply("Oops! I did not find any role matching `" + args.get(3) + "`");
                return;
            }

            if (!ctx.getSelfMember().canInteract(role)) {
                ctx.replyError("Oops! I cannot add/remove members to that role. Is that role higher than mine?");
                return;
            }

            final Role finalRole = role;
            String emoji;
            if (!message.getEmotes().isEmpty()) {
                Emote emote = message.getEmotes().get(0);
                emoji = emote.getName() + ":" + emote.getId();
            } else {
                emoji = args.get(2);
            }

            channel.addReactionById(messageId, emoji).queue((__) -> {
                DataSource.INS.addReactionRole(ctx.getGuild().getId(), channel.getId(), Long.toString(messageId), finalRole.getId(), emoji);
                ctx.replyWithSuccess("Successfully added reaction role!");
            }, (e) -> {
                if (e.getMessage().contains("Unknown Message"))
                    ctx.replyError("Failed to setup reaction role!\nDid you provide a valid messageId?");

                else if (e.getMessage().contains("Unknown Emoji"))
                    ctx.replyError("Failed to setup reaction role!\nDid you provide a valid emoji/emote?");

                ctx.replyError("Oops! Failed to setup reaction role!");
                LOGGER.error(e.getMessage());
            });

        } catch (NumberFormatException e) {
            ctx.reply("Did you provide a valid messageId?");
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            ctx.reply("Failed to react! Did you provide valid arguments?");
        }

    }

}
