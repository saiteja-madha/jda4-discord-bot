package bot.data.objects;

import net.dv8tion.jda.api.entities.Invite;

import java.util.Objects;

public class InviteData {

    private final String guildId;
    private final int maxAge;
    private int uses;

    public InviteData(Invite invite) {
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
