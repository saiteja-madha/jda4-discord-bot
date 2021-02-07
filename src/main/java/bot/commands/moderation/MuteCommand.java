package bot.commands.moderation;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.utils.GuildUtils;
import bot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MuteCommand extends ICommand {

    public MuteCommand() {
        this.name = "mute";
        this.help = "mutes the specified member(s)";
        this.usage = "<@member(s)> [reason]";
        this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
        this.userPermissions = new Permission[]{Permission.KICK_MEMBERS};
        this.minArgsCount = 1;
        this.category = CommandCategory.MODERATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final Message message = ctx.getMessage();
        List<Member> targetMembers = message.getMentionedMembers();

        if (args.get(0).equalsIgnoreCase("setup")) {
            setupMute(ctx);
            return;
        }

        if (targetMembers.isEmpty()) {
            ctx.reply("Please @mention the member(s) you want to mute!");
            return;
        }

        Role mutedrole = GuildUtils.getMutedRole(ctx.getGuild());

        if (mutedrole == null) {
            ctx.reply("No \"Muted\" role exists! Please add and setup up a \"Muted\" role, or use `"
                    + ctx.getPrefix() + "mute setup` to have one made automatically.");
            return;
        }

        if (!PermissionUtil.canInteract(ctx.getSelfMember(), mutedrole)) {
            ctx.reply("I do not have permission to move members to `Muted` role. Is that role below my highest role? ");
            return;
        }

        // Split content at last member mention
        String[] split = message.getContentRaw().split(targetMembers.get(targetMembers.size() - 1).getId() + ">");
        final String reason = (split.length > 1)
                ? String.join(" ", split[1].split("\\s+")).trim()
                : "No reason provided";

        targetMembers
                .stream()
                // Filter out members with which bot and command author can interact
                .filter(target -> ModerationUtils.canInteract(ctx.getMember(), target, "mute", ctx.getChannel()))
                .forEach(member -> ModerationUtils.mute(message, member, reason, mutedrole));

    }

    private void setupMute(CommandContext ctx) {
        GuildMessageReceivedEvent event = ctx.getEvent();
        Role mutedRole = null;

        if (!ctx.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            ctx.reply("You must have the Manage Server permission to use his command");
            return;
        }

        for (Role role : event.getGuild().getRoles())
            if (role.getName().equalsIgnoreCase("muted")) {
                mutedRole = role;
                break;
            }

        if (mutedRole != null) {
            ctx.reply("Muted role already exists \"muted\", Delete it and run the setup again!");
            return;
        }
        mutedRole = ModerationUtils.createMutedRole(ctx.getGuild());

        if (mutedRole != null)
            ctx.reply("The muted role has been set up. Please make sure everything is in order "
                    + "by checking the position of the role, as well as the channel specific permissions.");
        else
            ctx.reply("Something went wrong while setting up. Please make sure "
                    + "I have permission to edit/create roles, and modify every channel. Alternatively, give me the "
                    + "`Administrator` permission for setting up.");
    }

}
