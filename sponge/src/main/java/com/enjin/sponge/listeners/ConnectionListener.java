package com.enjin.sponge.listeners;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.PlayerGroupInfo;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.permissions.PermissionHandler;
import lombok.Getter;
import ninja.leaping.permissionsex.data.SubjectDataReference;
import ninja.leaping.permissionsex.sponge.PEXHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;

import java.util.*;

public class ConnectionListener {
	@Getter
	private static ConnectionListener instance;
	private ProviderRegistration permissionProviderRegistration = null;
	private PermissionHandler permissionHandler = null;

	public ConnectionListener() {
		ConnectionListener.instance = this;
		init();
	}

	private void init() {
		final ServiceManager services = Sponge.getServiceManager();
		if (services.isRegistered(PermissionService.class)) {
			final Optional<ProviderRegistration<PermissionService>> potential = services.getRegistration(PermissionService.class);
			if (potential.isPresent()) {
				initPermissions(potential.get());
			}
		}
	}

	private void initPermissions(ProviderRegistration<PermissionService> registration) {
		permissionProviderRegistration = registration;

		final PluginContainer container = registration.getPlugin();
		if (container.getId().equals("ninja.leaping.permissionsex")) {
			permissionHandler = new PEXHandler();
		}
	}

	@Listener
	public void onJoin(Join event) {
		if (permissionHandler != null) {
			permissionHandler.onJoin(event);
		}
	}

	@Listener
	public void onDisconnect(Disconnect event) {
		if (permissionHandler != null) {
			permissionHandler.onDisconnect(event);
		}
	}

	public static void updatePlayerRanks(Player player) {
		updatePlayerRanks1(player);
		EnjinMinecraftPlugin.saveRankUpdatesConfiguration();
	}

	public static void updatePlayerRanks1(Player player) {
		if (player == null || player.getName() == null) {
			Enjin.getLogger().debug("[ConnectionListener::updatePlayerRanks] Player or their name is null. Unable to update their ranks.");
			return;
		}

		PlayerGroupInfo info = new PlayerGroupInfo(player.getUniqueId());

		if (info == null) {
			Enjin.getLogger().debug("[ConnectionListener::updatePlayerRanks] PlayerGroupInfo is null. Unable to update " + player.getName() + "'s ranks.");
			return;
		}

		if (EnjinMinecraftPlugin.getRankUpdatesConfiguration() == null) {
			Enjin.getLogger().debug("[ConnectionListener::updatePlayerRanks] RankUpdatesConfiguration is null.");
			return;
		}

		if (EnjinMinecraftPlugin.getRankUpdatesConfiguration().getPlayerPerms() == null) {
			Enjin.getLogger().debug("[ConnectionListener::updatePlayerRanks] Player perms is null.");
			return;
		}

		info.getWorlds().putAll(getPlayerGroups(player));
		EnjinMinecraftPlugin.getRankUpdatesConfiguration().getPlayerPerms().put(player.getName(), info);
	}

	public static void updatePlayersRanks(Player[] players) {
		for (Player player : players) {
			updatePlayerRanks1(player);
		}

		EnjinMinecraftPlugin.saveRankUpdatesConfiguration();
	}

	public static Map<String, List<String>> getPlayerGroups(Player player) {
		Map<String, List<String>> groups = new HashMap<>();

		ServiceManager services = Sponge.getServiceManager();
		if (services.isRegistered(PermissionService.class)) {
			ProviderRegistration<PermissionService> registration = services.getRegistration(PermissionService.class).get();
			ConnectionListener.fetchGroups(registration);
		}
		// TODO
//		if (VaultManager.isVaultEnabled() && VaultManager.isPermissionsAvailable()) {
//			Permission permission = VaultManager.getPermission();
//			if (permission.hasGroupSupport()) {
//				String[] g = permission.getPlayerGroups(null, player);
//				if (g.length == 0 && Bukkit.getPluginManager().isPluginEnabled("PermissionsBukkit")) {
//					g = PermissionsBukkitListener.getGroups(player);
//				}
//
//				if (g.length > 0) {
//					groups.put("*", Arrays.asList(g));
//				}
//
//				for (World world : Bukkit.getWorlds()) {
//					g = permission.getPlayerGroups(world.getName(), player);
//					if (g.length > 0) {
//						groups.put(world.getName(), Arrays.asList(g));
//					}
//				}
//			}
//		}

		return groups;
	}

	public static void fetchGroups (ProviderRegistration registration) {
		// TODO
		PluginContainer plugin = registration.getPlugin();
		if (plugin.getId().equalsIgnoreCase("ninja.leaping.permissionsex")) {
			PermissionService service = (PermissionService) registration.getProvider();
			for (Subject subject : service.getUserSubjects().getAllSubjects()) {
				for (Subject parent : subject.getParents()) {
					Enjin.getLogger().info("Group: " + parent.getIdentifier());
				}
			}
		}
	}
}
