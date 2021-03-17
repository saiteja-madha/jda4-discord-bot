package bot.commands.invites;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import bot.handlers.InviteHandler;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InvitesCommand extends ICommand {

    public InvitesCommand() {
        this.name = "invites";
        this.help = "shows number of invites in this server";
        this.usage = "[@member]";
        this.category = CommandCategory.INVITE;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<Member> menMembers = ctx.getMessage().getMentionedMembers();
        Member target = ctx.getMember();

        if (!menMembers.isEmpty()) {
            target = menMembers.get(0);
        }

        final int[] invites = DataSource.INS.getInvites(ctx.getGuildId(), target.getId());
        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setAuthor("Invites for " + target.getEffectiveName())
                .setThumbnail(target.getUser().getEffectiveAvatarUrl())
                .setFooter("")
                .setDescription(target.getAsMention() + " has " + InviteHandler.getEffectiveInvites(invites) + " invites")
                .addField("Total Invites", "**" + InviteHandler.getEffectiveInvites(invites) + "**", true)
                .addField("Fake Invites", "**" + invites[1] + "**", true)
                .addField("Left Invites", "**" + invites[2] + "**", true);

        ctx.reply(embed.build());

    }

}
