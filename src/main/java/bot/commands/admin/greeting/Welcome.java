package bot.commands.admin.greeting;

import bot.data.GreetingType;

public class Welcome extends GreetingBase {

    public Welcome() {
        super(GreetingType.WELCOME);
        this.name = "welcome";
    }

}
