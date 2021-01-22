package bot.command.commands.fun;

import bot.command.CommandContext;
import bot.command.ICommand;
import org.jetbrains.annotations.NotNull;

public class FlipTextCommand extends ICommand {

    private static final String NORMAL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_,;.?!/\\'0123456789";
    private static final String FLIPPED = "∀qϽᗡƎℲƃHIſʞ˥WNOԀὉᴚS⊥∩ΛMXʎZɐqɔpǝɟbɥıظʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/\\,0ƖᄅƐㄣϛ9ㄥ86";

    public FlipTextCommand() {
        this.name = "fliptext";
        this.help = "reverses the given message";
        this.usage = "<message>";
        this.minArgsCount = 1;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final String text = ctx.getArgsJoined();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);

            int a = NORMAL.indexOf(letter);
            builder.append((a != -1) ? FLIPPED.charAt(a) : letter);
        }

        ctx.reply(builder.toString());

    }

}
