package bot.handlers;

import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import bot.utils.BotUtils;
import bot.utils.MiscUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class XPHandler {

    public void handle(GuildMessageReceivedEvent event) {
        final Member member = event.getMember();

        GuildSettings settings = DataSource.INS.getSettings(event.getGuild().getId());
        if (!settings.isRankingEnabled || member == null)
            return;

        int xpToAdd = getRandomXP();
        int[] data = DataSource.INS.updateXp(member, xpToAdd, true);
        int currentLevel = data[0];
        int previousXp = data[1];
        int xpNeeded = getXPNeeded(currentLevel + 1);
        int newXp = previousXp + xpToAdd;

        if (newXp > xpNeeded) {
            int newLevel = currentLevel + 1;
            DataSource.INS.setLevel(member, newLevel);
            String lvlUpMsg = settings.levelUpMessage
                    .replace("{l}", String.valueOf(newLevel))
                    .replace("{m}", member.getAsMention());

            TextChannel targetChannel = event.getChannel();
            if (settings.levelUpChannel != null) {
                TextChannel tcById = event.getGuild().getTextChannelById(settings.levelUpChannel);
                if (tcById != null)
                    targetChannel = tcById;
            }

            BotUtils.sendMsg(targetChannel, lvlUpMsg);
        }
    }

    private int getRandomXP() {
        return MiscUtils.getRandInt(0, 9) + 1;
    }

    public int getXPNeeded(int level) {
        return level * level * 100;
    }

}
