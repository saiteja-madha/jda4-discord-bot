package bot.commands.admin.greeting;

import bot.data.GreetingType;

public class WelcomeImage extends GreetingImageBase {

    public WelcomeImage() {
        super(GreetingType.WELCOME);
        this.name = "wi";
    }

}
