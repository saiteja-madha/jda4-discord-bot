package bot.commands.information;

import bot.Config;
import bot.Constants;
import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import bot.utils.MiscUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.util.Collections;

public class BotInfoCommand extends ICommand {

    private final DecimalFormat df = new DecimalFormat("#.##");
    private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    public BotInfoCommand() {
        this.name = "botinfo";
        this.help = "shows bot information";
        this.aliases = Collections.singletonList("binfo");
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.INFORMATION;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final JDA jda = ctx.getJDA();

        // GUILD STATS
        final String guilds = jda.getGuildCache().size() + "";
        final String tc = jda.getTextChannelCache().size() + "";
        final String vc = jda.getVoiceChannelCache().size() + "";
        final String roles = jda.getRoleCache().size() + "";
        final String responses = jda.getResponseTotal() + "";
        final String gatewayPing = jda.getGatewayPing() + " ms";
        final String inviteUrl = Config.get("BOT_INVITE");

        int members = 0;
        for (Guild g : jda.getGuilds()) {
            members += g.getMemberCount();
        }

        // RAM STATS
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        long totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        // CPU STATS
        int cores = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        String OS = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getName();
        String cpuUsage = getProcessCpuLoad() + "%";

        // UPTIME STATS
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeMXBean.getUptime();
        long uptimeInSeconds = uptime / 1000;

        // BUILD EMBED
        String str = "";
        str = str + Constants.CUBE_BULLET + " Total guilds: " + guilds + "\n";
        str = str + Constants.CUBE_BULLET + " Total users: " + members + "\n";
        str = str + Constants.CUBE_BULLET + " Total text channels: " + tc + "\n";
        str = str + Constants.CUBE_BULLET + " Total voice channels: " + vc + "\n";
        str = str + Constants.CUBE_BULLET + " Total roles: " + roles + "\n\n";
        str = str + Constants.ARROW_BULLET + " Responses Served: " + responses + "\n";
        str = str + Constants.ARROW_BULLET + " Websocket Ping: " + gatewayPing + "\n";
        str = str + "\n";

        EmbedBuilder eb = EmbedUtils.getDefaultEmbed()
                .setTitle("About " + jda.getSelfUser().getName())
                .setThumbnail(jda.getSelfUser().getEffectiveAvatarUrl())
                .setDescription(str)
                .addField("CPU:",
                        Constants.ARROW + " **OS:** " + OS + "\n"
                                + Constants.ARROW + " **Cores:** " + cores + "\n"
                                + Constants.ARROW + " **Threads:** " + Thread.activeCount() + "\n"
                                + Constants.ARROW + " **Usage:** " + cpuUsage + "\n"
                        , true)
                .addField("MEMORY:",
                        Constants.ARROW + " **Max:** " + maxMemory + " MB\n"
                                + Constants.ARROW + " **Total:** " + totalMemory + " MB\n"
                                + Constants.ARROW + " **Free:** " + freeMemory + " MB\n"
                                + Constants.ARROW + " **Used:** " + usedMemory + " MB\n"
                        , true)
                .addField("UPTIME:", "```" + MiscUtils.formatTime(uptimeInSeconds) + "```", false)
                .addField("LIBRARY:", "[JDA " + JDAInfo.VERSION + "](http://home.dv8tion.net:8080/job/JDA/" + JDAInfo.VERSION_BUILD + "/)", true)
                .addField("INVITE:", "[Add Me here!](" + inviteUrl + ")", true)
                .addField("SUPPORT:", "[Discord](" + Config.get("DISCORD_INVITE") + ")", true);

        ctx.reply(eb.build());

    }

    private String getProcessCpuLoad() {
        AttributeList list = null;

        try {
            list = mbs.getAttributes(ObjectName.getInstance("java.lang:type=OperatingSystem"),
                    new String[]{"ProcessCpuLoad"});
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert list != null;
        if (list.isEmpty())
            return "NaN";

        Attribute att = (Attribute) list.get(0);
        Double value = (Double) att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0)
            return "NaN";

        // returns a percentage value with 1 decimal point precision
        return df.format((value * 1000) / 10.0);
    }

}
