package bot.commands.utility;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.web.WebUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Arrays;

public class ProxiesCommand extends ICommand {

    private static final String PROXIES_URL =
            "https://api.proxyscrape.com/?request=displayproxies&proxytype=%s&timeout=10000&country=all&anonymity=all&ssl=all";

    public ProxiesCommand() {
        this.name = "proxies";
        this.help = "fetch proxies";
        this.aliases = Arrays.asList("socks5", "socks4", "http");
        this.category = CommandCategory.UTILS;
        this.cooldown = 10;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String invoke = ctx.getInvoke();
        String proxyType;

        if (invoke.equalsIgnoreCase("http"))
            proxyType = "http";
        else if (invoke.equalsIgnoreCase("socks4"))
            proxyType = "socks4";
        else if (invoke.equalsIgnoreCase("socks5"))
            proxyType = "socks5";
        else
            proxyType = "all";

        WebUtils.ins.getByteStream(String.format(PROXIES_URL, proxyType)).async((bytes) -> {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            ctx.getChannel().sendMessage(proxyType.toUpperCase() + " Proxies fetched")
                    .addFile(bytes, proxyType + "_proxies" + timestamp.getTime() + ".txt")
                    .queue();
        });

    }

}
