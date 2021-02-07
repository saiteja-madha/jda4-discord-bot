package bot.commands.admin.counter;

import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.data.CounterType;
import bot.database.DataSource;
import bot.database.objects.CounterConfig;
import bot.utils.GuildUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.NotNull;

public class CounterSetup extends ICommand {

    public CounterSetup() {
        this.name = "counter";
        this.usage = "`{p}counter all <name>` : enable a channel to count members & bots\n" +
                "`{p}counter members <name>` : enable a channel to count members\n" +
                "`{p}counter bots <name>` : enable a channel to count bots\n" +
                "`{p}counter status` : check status on currently configured counters\n";
        this.minArgsCount = 1;
        this.botPermissions = new Permission[]{Permission.MANAGE_CHANNEL};
        this.category = CommandCategory.ADMINISTRATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final CounterType type = CounterType.fromSearch(ctx.getArgs().get(0));
        if (type == null) {
            if (ctx.getArgs().get(0).equalsIgnoreCase("status")) {
                this.sendStatus(ctx);
                return;
            }

            ctx.reply("Incorrect arguments are passed! Counter types: `all/members/bots`");
            return;
        }

        if (ctx.getArgs().size() < 2) {
            ctx.reply("Incorrect Usage!");
            return;
        }

        final Guild guild = ctx.getGuild();

        GuildUtils.getMemberStats(guild, count -> {

            if (count == null) {
                ctx.reply("Unexpected error occurred while retrieving members on this server. " +
                        "Try again later or contact support server");
                return;
            }

            String counterName = String.join(" ", ctx.getArgs().subList(1, ctx.getArgs().size()));
            String channelName = " ";
            if (type == CounterType.MEMBERS)
                channelName = counterName + " : " + count[2];
            else if (type == CounterType.BOTS)
                channelName = counterName + " : " + count[1];
            else if (type == CounterType.ALL)
                channelName = counterName + " : " + count[0];

            try {
                guild.createVoiceChannel(channelName).queue((vc) -> {
                    vc.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VOICE_CONNECT).queue();
                    DataSource.INS.setCounter(type, guild, vc, counterName);
                    DataSource.INS.updateBotCount(guild.getId(), false, (int) count[1]);

                    ctx.reply("Configuration saved! Counter channel created");
                }, err -> ctx.reply("Setup failed! Unexpected error"));

            } catch (Exception e) {
                ctx.reply("Setup failed! Unexpected error");
            }
        });

    }

    private void sendStatus(CommandContext ctx) {
        CounterConfig config = DataSource.INS.getCounterConfig(ctx.getGuild().getId());

        if (config == null) {
            ctx.reply("No counter channel has been configured on this guild");
            return;
        }

        StringBuilder str = new StringBuilder();

        VoiceChannel tvc = null, mvc = null, bvc = null;
        if (config.tCountChannel != null)
            tvc = ctx.getGuild().getVoiceChannelById(config.tCountChannel);
        if (config.mCountChannel != null)
            mvc = ctx.getGuild().getVoiceChannelById(config.mCountChannel);
        if (config.bCountChannel != null)
            bvc = ctx.getGuild().getVoiceChannelById(config.bCountChannel);

        str.append("TotalCount Channel: ").append(tvc == null ? Constants.X_MARK : Constants.TICK).append("\n")
                .append("MemberCount Channel: ").append(mvc == null ? Constants.X_MARK : Constants.TICK).append("\n")
                .append("BotCount Channel: ").append(bvc == null ? Constants.X_MARK : Constants.TICK).append("\n");

        EmbedBuilder embed = EmbedUtils.defaultEmbed()
                .setAuthor("Counter Configuration")
                .setDescription(str.toString());

        ctx.reply(embed.build());

    }

}
