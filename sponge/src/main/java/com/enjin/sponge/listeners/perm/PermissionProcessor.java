package com.enjin.sponge.listeners.perm;

import com.enjin.core.Enjin;
import com.enjin.sponge.tasks.DelayedPlayerPermsUpdate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.command.SendCommandEvent;

import java.util.Optional;

public abstract class PermissionProcessor {
    public abstract void processCommand(CommandSource sender, String command, SendCommandEvent event);

    public void update(Player player) {
        if (player != null) {
			Enjin.getLogger().debug(player.getName() + " just got a rank change... processing...");
			Sponge.getScheduler().createTaskBuilder().execute(new DelayedPlayerPermsUpdate(player))
					.delayTicks(2)
					.submit(Enjin.getPlugin());
        }
    }

	public void update(String name) {
		if (name != null && !name.isEmpty()) {
			Optional<Player> player = Sponge.getServer().getPlayer(name);
			if (player.isPresent()) {
				update(player.get());
			}
		}
	}
}
