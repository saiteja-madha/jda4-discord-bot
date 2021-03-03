package bot.data.objects;

import java.util.Objects;

public class InviteData {

    private final String guildId;
    private final int maxAge;
    private int uses;

    public InviteData(net.dv8tion.jda.api.entities.Invite invite) {
        this.guildId = Objects.requireNonNull(invite.getGuild()).getId();
        this.uses = invite.getUses();
        this.maxAge = invite.getMaxAge();
    }

    public String getGuildId() {
        return this.guildId;
    }

    public int getUses() {
        return this.uses;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public void incrementUses() {
        this.uses++;
    }

}
