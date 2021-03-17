package bot.commands.admin.greeting;

import bot.Bot;
import bot.data.GreetingType;

public class Farewell extends GreetingBase {

    public Farewell(Bot bot) {
        super(bot, GreetingType.FAREWELL);
        this.name = "farewell";
    }

}
