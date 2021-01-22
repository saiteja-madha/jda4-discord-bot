package bot.command.commands.utility;

import bot.Constants;
import bot.command.CommandContext;
import bot.command.ICommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class TranslateCodes extends ICommand {

    private final Paginator.Builder pBuilder;

    public TranslateCodes(EventWaiter waiter) {
        this.name = "trcodes";
        this.help = "displays a list of available translate codes";
        this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_HISTORY,
                Permission.MESSAGE_EMBED_LINKS};
        this.pBuilder = new Paginator.Builder()
                .setColumns(3)
                .setItemsPerPage(60)
                .setFinalAction(m -> {
                })
                .setEventWaiter(waiter);
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        pBuilder.clearItems();
        Constants.langCodes.forEach((k, v) ->
                pBuilder.addItems("\u276F" + " " + v + ": `" + k + "`")
        );

        Paginator p = pBuilder
                .addUsers(ctx.getAuthor())
                .setText("Language Codes")
                .setColor(Color.decode(String.valueOf(0x3883d9)))
                .build();

        p.paginate(ctx.getChannel(), 1);

    }
}
