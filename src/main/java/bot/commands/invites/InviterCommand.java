package bot.commands.invites;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InviterCommand extends ICommand {

    public InviterCommand() {
        this.name = "inviter";
        this.help = "shows inviter information";
        this.usage = "[@member]";
        this.category = CommandCategory.INVITES;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<Member> menMembers = ctx.getMessage().getMentionedMembers();
        Member target = ctx.getMember();

        if (!menMembers.isEmpty()) {
            target = menMembers.get(0);
        }

        final String inviterId = DataSource.INS.getInviterId(ctx.getGuildId(), target.getId());

        if (inviterId == null) {
            ctx.reply("Oops! Cannot track who invited you");
            return;
        }

        final int[] invites = DataSource.INS.getInvites(ctx.getGuildId(), inviterId);
        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setDescription("**Inviter:** " + "<@" + inviterId + ">" + "\n" +
                        "**Inviter ID:** " + inviterId + "\n" +
                        "**Inviter Invites:** Total: `" + invites[0] + "` Fake: `" + invites[2] + "` Left: `" + invites[1] + "`"

                )
                .setFooter("");
        ctx.reply(embed.build());


    }
}
