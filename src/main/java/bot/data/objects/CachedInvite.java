package bot.data.objects;

import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VanityInvite;
import org.jetbrains.annotations.Nullable;

public class CachedInvite {

    public final String code;
    public final int uses;
    public final boolean isVanity;
    @Nullable public User inviter;

    public CachedInvite(Invite invite){
        this.code = invite.getCode();
        this.uses = invite.getUses();
        this.isVanity = false;
        if (invite.isExpanded()) {
            this.inviter = invite.getInviter();
        }
        else {
            invite.expand().queue(inv -> this.inviter = inv.getInviter());
        }
    }

    public CachedInvite(String code, int uses) {
        this.code = code;
        this.uses = uses;
        this.isVanity = true;
        this.inviter = null;
    }

}
