package bot.commands.utility;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.duncte123.botcommons.web.ContentType;
import me.duncte123.botcommons.web.WebParserUtils;
import me.duncte123.botcommons.web.WebUtils;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class HasteCommand extends ICommand {

    private static final String HASTE_SERVER = "https://hastebin.com/";

    public HasteCommand() {
        this.name = "haste";
        this.help = "Posts some text to hastebin";
        this.usage = "<text>";
        this.minArgsCount = 1;
        this.category = CommandCategory.UTILS;
        this.cooldown = 5;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String invoke = this.getName();
        final String contentRaw = ctx.getMessage().getContentRaw();
        final int index = contentRaw.indexOf(invoke) + invoke.length();
        final String body = contentRaw.substring(index).trim();

        this.createPaste(body, (message) -> {
            if (message == null)
                ctx.reply("Error connecting to hastebin server");
            else
                ctx.reply(message);
        });
    }

    private void createPaste(String text, Consumer<String> callback) {
        Request request = WebUtils.defaultRequest()
                .post(RequestBody.create(null, text.getBytes()))
                .addHeader("Content-Type", ContentType.TEXT_PLAIN.getType())
                .url(HASTE_SERVER + "documents")
                .build();

        WebUtils.ins.prepareRaw(request, (r) -> WebParserUtils.toJSONObject(r, new ObjectMapper())).async(
                (json) -> {
                    String key = json.get("key").asText();
                    callback.accept(HASTE_SERVER + key);
                },
                (e) -> {
                    LOGGER.error("Hastebin error: " + e.getMessage());
                    callback.accept(null);
                }
        );
    }

}
