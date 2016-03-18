package com.enjin.sponge.permissions;

import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;

public interface PermissionHandler {
	void onJoin(Join event);

	void onDisconnect(Disconnect event);
}
