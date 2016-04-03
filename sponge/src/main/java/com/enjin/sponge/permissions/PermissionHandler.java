package com.enjin.sponge.permissions;

import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.profile.GameProfile;

import java.util.List;
import java.util.Map;

public interface PermissionHandler {
	void onJoin(Join event);

	void onDisconnect(Disconnect event);

	Map<String, List<String>> fetchPlayerGroups(GameProfile player);

	List<String> fetchGroups();

	void addGroup(String player, String group, String world);

	void removeGroup(String player, String group, String world);
}
