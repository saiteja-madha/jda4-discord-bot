package bot.commands.admin.greeting;

import bot.Bot;
import bot.data.GreetingType;

public class Welcome extends GreetingBase {

    public Welcome(Bot bot) {
        super(bot, GreetingType.WELCOME);
        this.name = "welcome";
    }

}
