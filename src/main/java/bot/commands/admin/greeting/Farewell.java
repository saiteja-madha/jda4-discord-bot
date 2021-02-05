package bot.commands.admin.greeting;

import bot.data.GreetingType;

public class Farewell extends GreetingBase {

    public Farewell() {
        super(GreetingType.FAREWELL);
        this.name = "farewell";
    }

}
