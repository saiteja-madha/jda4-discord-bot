package bot.handlers;

import bot.database.DataSource;
import bot.database.objects.GuildSettings;
import bot.utils.BotUtils;
import bot.utils.MiscUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class XPHandler {

    private final Map<String, OffsetDateTime> xpCooldown;

    public XPHandler() {
        this.xpCooldown = new HashMap<>();
    }

    public void handle(GuildMessageReceivedEvent event, GuildSettings settings) {
        final Member member = event.getMember();

        // Member is null in case of Webhook
        assert member != null;

        // Cooldown of 2 minutes to prevent spamming
        String key = event.getGuild().getId() + "|" + member.getId();
        if (isOnCooldown(key)) return;
        else this.applyCooldown(key);

        int xpToAdd = getRandomXP();
        int[] data = DataSource.INS.incrementXp(member, xpToAdd, true);
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

    public boolean isOnCooldown(String name) {
        if (xpCooldown.containsKey(name)) {
            int time = (int) Math.ceil(OffsetDateTime.now().until(xpCooldown.get(name), ChronoUnit.MILLIS) / 1000D);
            if (time <= 0) {
                xpCooldown.remove(name);
                return false;
            }
            return true;
        }
        return false;
    }

    public void applyCooldown(String name) {
        xpCooldown.put(name, OffsetDateTime.now().plusMinutes(2));
    }

    public void cleanCooldowns() {
        OffsetDateTime now = OffsetDateTime.now();
        xpCooldown.keySet().stream().filter((str) -> (xpCooldown.get(str).isBefore(now))).collect(Collectors.toList())
                .forEach(xpCooldown::remove);
    }

}
