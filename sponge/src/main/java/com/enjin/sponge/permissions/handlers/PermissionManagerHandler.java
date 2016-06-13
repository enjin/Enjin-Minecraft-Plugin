package com.enjin.sponge.permissions.handlers;

import com.enjin.sponge.permissions.PermissionHandler;
import io.github.djxy.permissionManager.PMService;
import io.github.djxy.permissionManager.PermissionManager;
import io.github.djxy.permissionManager.subjects.Group;
import io.github.djxy.permissionManager.subjects.Subject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PermissionManagerHandler implements PermissionHandler {
    private PMService service;

    public PermissionManagerHandler(PermissionService service) {
        if (!(service instanceof PMService)) {
            throw new IllegalArgumentException("PermissionService is not instance of PMService");
        }

        this.service = (PMService) service;
    }

    @Override
    public void onJoin(ClientConnectionEvent.Join event) {
    }

    @Override
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
    }

    @Override
    public Map<String, List<String>> fetchPlayerGroups(GameProfile player) {
        PermissionManager manager = PermissionManager.getInstance();
        io.github.djxy.permissionManager.subjects.Player pp = manager.getOrCreatePlayer(player.getUniqueId());

        Map<String, List<String>> worlds = new HashMap<>();
        if (pp != null) {
            List<Group> global = pp.getGlobalGroups();
            if (global != null && !global.isEmpty()) {
                worlds.put("*", global.stream().map(Subject::getIdentifier).collect(Collectors.toList()));
            }

            for (World world : Sponge.getServer().getWorlds()) {
                List<Group> groups = pp.getWorldGroups(world.getName());
                if (groups != null && !groups.isEmpty()) {
                    worlds.put(world.getName(), groups.stream().map(Subject::getIdentifier).collect(Collectors.toList()));
                }
            }
        }

        return worlds;
    }

    @Override
    public List<String> fetchGroups() {
        PermissionManager manager = PermissionManager.getInstance();
        return manager.getGroups().stream().map(Subject::getIdentifier).collect(Collectors.toList());
    }

    @Override
    public void addGroup(String player, String group, String world) {
        Optional<Player> optional = Sponge.getServer().getPlayer(player);
        if (optional.isPresent()) {
            Player p = optional.get();
            PermissionManager manager = PermissionManager.getInstance();

            Group g = manager.getGroup(group);
            if (g != null) {
                io.github.djxy.permissionManager.subjects.Player pp = manager.getOrCreatePlayer(p.getUniqueId());
                if (pp != null) {
                    pp.addGroup(g);
                }
            }
        }
    }

    @Override
    public void removeGroup(String player, String group, String world) {
        Optional<Player> optional = Sponge.getServer().getPlayer(player);
        if (optional.isPresent()) {
            Player p = optional.get();
            PermissionManager manager = PermissionManager.getInstance();

            Group g = manager.getGroup(group);
            if (g != null) {
                io.github.djxy.permissionManager.subjects.Player pp = manager.getOrCreatePlayer(p.getUniqueId());
                if (pp != null) {
                    pp.removeGroup(g);
                }
            }
        }
    }
}
