package com.enjin.sponge.permissions.handlers;

import com.enjin.sponge.permissions.PermissionHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SpongePermissionHandler implements PermissionHandler {
    private PermissionService service;

    public SpongePermissionHandler(PermissionService service) {
        this.service = service;
    }

    @Override
    public void onJoin(Join event) {
    }

    @Override
    public void onDisconnect(Disconnect event) {
    }

    @Override
    public Map<String, List<String>> fetchPlayerGroups(GameProfile player) {
        Map<String, List<String>> worlds = Maps.newHashMap();
        if (player != null) {
            Subject subject = service.getUserSubjects().get(player.getUniqueId().toString());
            for (Map.Entry<Set<Context>, List<Subject>> entry : subject.getSubjectData().getAllParents().entrySet()) {
                String world = "*";
                for (Context ctx : entry.getKey()) {
                    if (ctx.getKey().equalsIgnoreCase("world")) {
                        world = ctx.getValue();
                        break;
                    }
                }
                worlds.put(world, entry.getValue().stream().map(Subject::getIdentifier).collect(Collectors.toList()));
            }
        }
        return worlds;
    }

    @Override
    public List<String> fetchGroups() {
        List<String> groups = Lists.newArrayList();
        for (Subject subject : service.getGroupSubjects().getAllSubjects()) {
            groups.add(subject.getIdentifier());
        }
        return groups;
    }

    @Override
    public void addGroup(String player, String group, String world) {
        if (!fetchGroups().contains(group)) return;

        final Optional<Player> optional = Sponge.getServer().getPlayer(player);
        if (optional.isPresent()) {
            Player p = optional.get();
            if (world != null && !world.equals("*")) {
                p.getSubjectData().addParent(
                        Collections.singleton(new Context("world", world)),
                        service.getGroupSubjects().get(group)
                );
            } else {
                p.getSubjectData().addParent(
                        SubjectData.GLOBAL_CONTEXT,
                        service.getGroupSubjects().get(group)
                );
            }
        }
    }

    @Override
    public void removeGroup(String player, String group, String world) {
        final Optional<Player> optional = Sponge.getServer().getPlayer(player);
        if (optional.isPresent()) {
            Player p = optional.get();
            if (world != null && !world.equals("*")) {
                p.getSubjectData().removeParent(
                        Collections.singleton(new Context("world", world)),
                        service.getGroupSubjects().get(group)
                );
            } else {
                p.getSubjectData().removeParent(
                        SubjectData.GLOBAL_CONTEXT,
                        service.getGroupSubjects().get(group)
                );
            }

        }
    }
}
