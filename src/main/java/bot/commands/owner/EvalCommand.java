package bot.commands.owner;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import groovy.lang.GroovyShell;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;

public class EvalCommand extends ICommand {

    private final GroovyShell engine;
    private final String imports;

    public EvalCommand() {
        this.name = "eval";
        this.help = "Takes groovy code and evaluates it";
        this.usage = "<groovy-script>";
        this.category = CommandCategory.OWNER;
        this.minArgsCount = 1;
        this.engine = new GroovyShell();
        this.imports = "import java.io.*\n" +
                "import java.lang.*\n" +
                "import java.util.*\n" +
                "import java.util.concurrent.*\n" +
                "import net.dv8tion.jda.core.*\n" +
                "import net.dv8tion.jda.api.entities.*\n" +
                "import net.dv8tion.jda.api.entities.impl.*\n" +
                "import net.dv8tion.jda.api.managers.*\n" +
                "import net.dv8tion.jda.api.managers.impl.*\n" +
                "import net.dv8tion.jda.api.utils.*\n";
    }

    @Override
    public void handle(@Nonnull CommandContext ctx) {
        try {
            engine.setProperty("args", ctx.getArgs());
            engine.setProperty("event", ctx.getEvent());
            engine.setProperty("message", ctx.getMessage());
            engine.setProperty("channel", ctx.getChannel());
            engine.setProperty("api", ctx.getJDA());
            engine.setProperty("guild", ctx.getGuild());
            engine.setProperty("member", ctx.getMember());

            String script = imports + ctx.getMessage().getContentRaw().split("\\s+", 2)[1];
            Object out = engine.evaluate(script);

            ctx.reply(out == null ? "Executed without error" : out.toString());

        } catch (Exception ex) {
            EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                    .setDescription("Script Execution failed: ```" + ex.getMessage() + "```");
            ctx.reply(embed.build());
        }

    }

}
