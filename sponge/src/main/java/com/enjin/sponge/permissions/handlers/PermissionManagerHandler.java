package com.enjin.sponge.permissions.handlers;

import com.enjin.sponge.listeners.ConnectionListener;
import com.enjin.sponge.permissions.PermissionHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.djxy.permissionmanager.subjects.Permission;
import io.github.djxy.permissionmanager.subjects.SubjectData;
import io.github.djxy.permissionmanager.subjects.SubjectDataListener;
import io.github.djxy.permissionmanager.subjects.group.Group;
import io.github.djxy.permissionmanager.subjects.group.GroupCollection;
import io.github.djxy.permissionmanager.subjects.user.User;
import io.github.djxy.permissionmanager.subjects.user.UserCollection;
import java.util.Set;
import java.util.UUID;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PermissionManagerHandler implements PermissionHandler {
    private SubjectDataListener listener = new SubjectDataListener() {
        @Override
        public void onSetPermission(Set<Context> set, Subject subject, Permission permission) {
            //
        }

        @Override
        public void onRemovePermission(Set<Context> set, Subject subject, String s) {
            //
        }

        @Override
        public void onAddGroup(Set<Context> set, Subject subject, Group group) {
            Optional<Player> player = Sponge.getServer().getPlayer(UUID.fromString(subject.getIdentifier()));
            if (player.isPresent()) ConnectionListener.updatePlayerRanks(player.get());
        }

        @Override
        public void onRemoveGroup(Set<Context> set, Subject subject, Group group) {
            Optional<Player> player = Sponge.getServer().getPlayer(UUID.fromString(subject.getIdentifier()));
            if (player.isPresent()) ConnectionListener.updatePlayerRanks(player.get());
        }
    };

    public PermissionManagerHandler(PermissionService service) {
        if (!(service instanceof io.github.djxy.permissionmanager.PermissionService)) {
            throw new IllegalArgumentException("PermissionService is not instance of PMService");
        }
    }

    @Override
    public void onJoin(ClientConnectionEvent.Join event) {
        User user = (User) UserCollection.instance.get(event.getTargetEntity().getUniqueId().toString());
        user.getSubjectData().addListener(listener);
    }

    @Override
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        User user = (User) UserCollection.instance.get(event.getTargetEntity().getUniqueId().toString());
        user.getSubjectData().removeListener(listener);
    }

    @Override
    public Map<String, List<String>> fetchPlayerGroups(GameProfile player) {
        Map<String, List<String>> worlds = new HashMap<>();
        User user = (User) UserCollection.instance.get(player.getUniqueId().toString());

        worlds.put("*", user.getParents(SubjectData.GLOBAL_CONTEXT).stream().map(Subject::getIdentifier).collect(Collectors.toList()));

        for (World world : Sponge.getServer().getWorlds())
            worlds.put(world.getName(), user.getParents(Sets.newHashSet(new Context(Context.WORLD_KEY, world.getName()))).stream()
                    .map(Subject::getIdentifier)
                    .collect(Collectors.toList()));

        return worlds;
    }

    @Override
    public List<String> fetchGroups() {
        List<String> groups = Lists.newArrayList();

        for (Subject subject : GroupCollection.instance.getAllSubjects())
            groups.add(subject.getIdentifier());

        return groups;
    }

    @Override
    public void addGroup(String player, String group, String world) {
        Optional<Player> optional = Sponge.getServer().getPlayer(player);
        if (!optional.isPresent()) return;

        User user = (User) UserCollection.instance.get(optional.get().getUniqueId().toString());
        Subject subjectGroup = GroupCollection.instance.get(group);
        if (subjectGroup == null) return;

        if (world.equals("*"))
            user.getSubjectData().addParent(SubjectData.GLOBAL_CONTEXT, subjectGroup);
        else
            user.getSubjectData().addParent(Sets.newHashSet(new Context(Context.WORLD_KEY, world)), subjectGroup);
    }

    @Override
    public void removeGroup(String player, String group, String world) {
        Optional<Player> optional = Sponge.getServer().getPlayer(player);
        if (!optional.isPresent()) return;

        User user = (User) UserCollection.instance.get(optional.get().getUniqueId().toString());
        Subject subjectGroup = GroupCollection.instance.get(group);
        if (subjectGroup == null) return;

        if (world.equals("*"))
            user.getSubjectData().removeParent(SubjectData.GLOBAL_CONTEXT, subjectGroup);
        else
            user.getSubjectData().removeParent(Sets.newHashSet(new Context(Context.WORLD_KEY, world)), subjectGroup);
    }
}
