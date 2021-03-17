package bot.commands.invites;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.data.InviteType;
import bot.database.DataSource;
import bot.handlers.InviteHandler;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddInvitesCommand extends ICommand {

    public AddInvitesCommand() {
        this.name = "addinvites";
        this.help = "add invites to a member [Can be negative]";
        this.usage = "<@member> <amount>";
        this.minArgsCount = 2;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.INVITES;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<Member> mentionedMembers = ctx.getMessage().getMentionedMembers();
        if (mentionedMembers.isEmpty()) {
            ctx.reply("Incorrect usage! You need to mention a member");
            return;
        }

        final Member target = mentionedMembers.get(0);
        final String input = ctx.getArgs().get(1);

        try {
            final int amount = Integer.parseInt(input);
            final int[] invites = DataSource.INS.incrementInvites(ctx.getGuildId(), target.getId(), amount, InviteType.ADDED);

            EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                    .setAuthor("Successfully added invites to " + target.getEffectiveName())
                    .setThumbnail(target.getUser().getEffectiveAvatarUrl())
                    .setColor(Constants.SUCCESS_EMBED)
                    .setFooter("")
                    .setDescription(target.getAsMention() + " now has " + InviteHandler.getEffectiveInvites(invites) + " invites")
                    .addField("Total Invites", "**" + InviteHandler.getEffectiveInvites(invites) + "**", true)
                    .addField("Fake Invites", "**" + invites[1] + "**", true)
                    .addField("Left Invites", "**" + invites[2] + "**", true);

            ctx.reply(embed.build());

        } catch (NumberFormatException ex) {
            ctx.reply("Err! Did you provide a valid number input?");
        }

    }

}
