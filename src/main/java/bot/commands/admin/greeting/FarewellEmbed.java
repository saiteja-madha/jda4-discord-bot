package bot.commands.admin.greeting;

import bot.data.GreetingType;

public class FarewellEmbed extends GreetingEmbedBase {

    public FarewellEmbed() {
        super(GreetingType.FAREWELL);
        this.name = "fe";
    }

}
