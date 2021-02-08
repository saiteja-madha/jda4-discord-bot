package bot.commands.social;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReputationCommand extends ICommand {

    public ReputationCommand() {
        this.name = "rep";
        this.help = "give reputation to a user";
        this.usage = "<@user>";
        this.minArgsCount = 1;
        this.category = CommandCategory.SOCIAL;
        this.cooldown = 86400;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        List<Member> mentionedMembers = ctx.getMessage().getMentionedMembers();

        if (mentionedMembers.isEmpty()) {
            ctx.reply("You need to mention a member whom you want to rep!");
            return;
        }

        Member target = mentionedMembers.get(0);
        if (target.getUser().isBot()) {
            ctx.reply("You cannot give reputation to a bot!");
            return;
        }

        if (target.equals(ctx.getMember())) {
            ctx.reply("You cannot give reputation to yourself!");
            return;
        }

        DataSource.INS.setReputation(target, 1);

        EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                .setAuthor(ctx.getMember().getEffectiveName(), null, ctx.getAuthor().getEffectiveAvatarUrl())
                .setDescription(target.getAsMention() + " +1 Rep!");

        ctx.reply(embed.build());

    }

}
