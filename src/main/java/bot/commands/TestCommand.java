package bot.commands;

import bot.command.CommandContext;
import bot.command.ICommand;
import bot.database.DataSource;
import org.jetbrains.annotations.NotNull;

public class TestCommand extends ICommand {

    public TestCommand(){
        this.name = "test";
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        DataSource.INS.updateXp(ctx.getMember(), 10, true);
    }

}
