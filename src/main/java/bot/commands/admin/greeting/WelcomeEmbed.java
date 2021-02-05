package bot.commands.admin.greeting;

import bot.data.GreetingType;

public class WelcomeEmbed extends GreetingEmbedBase {

    public WelcomeEmbed() {
        super(GreetingType.WELCOME);
        this.name = "we";
    }

}
