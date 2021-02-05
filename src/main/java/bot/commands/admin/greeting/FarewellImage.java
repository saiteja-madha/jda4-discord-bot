package bot.commands.admin.greeting;

import bot.data.GreetingType;

public class FarewellImage extends GreetingImageBase {

    public FarewellImage() {
        super(GreetingType.FAREWELL);
        this.name = "fi";
    }

}
