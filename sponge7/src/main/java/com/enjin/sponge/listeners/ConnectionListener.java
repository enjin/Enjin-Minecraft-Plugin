package com.enjin.sponge.listeners;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.PlayerGroupInfo;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.permissions.PermissionHandler;
import com.enjin.sponge.permissions.handlers.SpongePermissionHandler;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.event.network.ClientConnectionEvent.Login;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.permission.PermissionService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConnectionListener {
    @Getter
    private static ConnectionListener instance;
    private        PermissionHandler  permissionHandler = null;

    public ConnectionListener() {
        ConnectionListener.instance = this;
        init();
    }

    private void init() {
        final ServiceManager services = Sponge.getServiceManager();
        if (services.isRegistered(PermissionService.class)) {
            final Optional<ProviderRegistration<PermissionService>> potential = services.getRegistration(
                    PermissionService.class);
            if (potential.isPresent()) {
                initPermissions(potential.get());
            }
        }
    }

    private void initPermissions(ProviderRegistration<PermissionService> registration) {
        permissionHandler = new SpongePermissionHandler(registration.getProvider());
    }

    @Listener
    public void onLogin(Login event) {
        if (permissionsEnabled()) {
            updatePlayerRanks(event.getTargetUser());
        }
    }

    @Listener
    public void onJoin(Join event) {
        if (permissionsEnabled()) {
            permissionHandler.onJoin(event);
        }
    }

    @Listener
    public void onDisconnect(Disconnect event) {
        if (permissionsEnabled()) {
            permissionHandler.onDisconnect(event);
        }
    }

    public static boolean permissionsEnabled() {
        return instance.permissionHandler != null;
    }

    public static void updatePlayerRanks(User user) {
        if (permissionsEnabled()) {
            updatePlayerRanks1(user.getProfile());
            EnjinMinecraftPlugin.saveRankUpdatesConfiguration();
        }
    }

    public static void updatePlayersRanks(GameProfile[] profiles) {
        if (permissionsEnabled()) {
            for (GameProfile profile : profiles) {
                updatePlayerRanks1(profile);
            }

            EnjinMinecraftPlugin.saveRankUpdatesConfiguration();
        }
    }

    private static void updatePlayerRanks1(GameProfile profile) {
        if (profile == null || !profile.getName().isPresent()) {
            Enjin.getLogger()
                 .debug("[ConnectionListener::updatePlayerRanks] Player or their name is null. Unable to update their ranks.");
            return;
        }

        PlayerGroupInfo info = new PlayerGroupInfo(profile.getUniqueId());

        if (info == null) {
            Enjin.getLogger()
                 .debug("[ConnectionListener::updatePlayerRanks] PlayerGroupInfo is null. Unable to update " + profile.getName() + "'s ranks.");
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

        info.getWorlds().putAll(getPlayerGroups(profile));
        EnjinMinecraftPlugin.getRankUpdatesConfiguration().getPlayerPerms().put(profile.getName().get(), info);
    }

    public static Map<String, List<String>> getPlayerGroups(GameProfile profile) {
        Map<String, List<String>> worlds = null;

        if (permissionsEnabled()) {
            worlds = instance.permissionHandler.fetchPlayerGroups(profile);
        }

        return worlds;
    }

    public static List<String> getGroups() {
        List<String> groups = null;

        if (permissionsEnabled()) {
            groups = instance.permissionHandler.fetchGroups();
        }

        return groups;
    }

    public static void addGroup(String player, String group, String world) {
        if (permissionsEnabled()) {
            instance.permissionHandler.addGroup(player, group, world);
        }
    }

    public static void removeGroup(String player, String group, String world) {
        if (permissionsEnabled()) {
            instance.permissionHandler.removeGroup(player, group, world);
        }
    }
}
