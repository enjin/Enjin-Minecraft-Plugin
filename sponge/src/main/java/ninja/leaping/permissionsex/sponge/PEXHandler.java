package ninja.leaping.permissionsex.sponge;

import com.enjin.sponge.listeners.ConnectionListener;
import com.enjin.sponge.permissions.PermissionHandler;
import ninja.leaping.permissionsex.PermissionsEx;
import ninja.leaping.permissionsex.data.ImmutableSubjectData;
import ninja.leaping.permissionsex.data.SubjectCache;
import ninja.leaping.permissionsex.data.SubjectDataReference;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PEXHandler implements PermissionHandler {
    private PermissionsExPlugin service = null;
    private Map<UUID, SubjectDataReference> references = new HashMap<>();

    public PEXHandler(PermissionService service) throws IllegalArgumentException {
        if (!(service instanceof PermissionsExPlugin)) {
            throw new IllegalArgumentException("PermissionService is not instance of PermissionsExPlugin");
        }

        this.service = (PermissionsExPlugin) service;
    }

    public void onJoin(Join event) {
        final Player player = event.getTargetEntity();

        final ServiceManager services = Sponge.getServiceManager();
        final Optional<ProviderRegistration<PermissionService>> potentialRegistration = services.getRegistration(PermissionService.class);

        if (potentialRegistration.isPresent()) {
            final PermissionsExPlugin service = (PermissionsExPlugin) potentialRegistration.get().getProvider();
            final SubjectCache cache = service.getManager().getSubjects(PermissionsEx.SUBJECTS_USER).persistentData();

            CompletableFuture<SubjectDataReference> future = cache.getReference(player.getUniqueId().toString());
            future.whenComplete((reference, throwable) -> {
                reference.onUpdate(data -> ConnectionListener.updatePlayerRanks(player));
                references.put(player.getUniqueId(), reference);
            });
        }
    }

    public void onDisconnect(Disconnect event) {
        final Player player = event.getTargetEntity();
        references.remove(player.getUniqueId());
    }

    public Map<String, List<String>> fetchPlayerGroups(GameProfile player) {
        Map<String, List<String>> worlds = new HashMap<>();

        ImmutableSubjectData data = references.get(player.getUniqueId()).get();
        data.getAllParents().entrySet().forEach(e -> {
            final List<String> groups = new ArrayList<>();

            e.getValue().forEach(entry -> {
                // Iterate and validate all groups in the entry
                if (entry.getKey().equalsIgnoreCase("group") && groupExists(entry.getValue())) {
                    groups.add(entry.getValue());
                }
            });

            if (!groups.isEmpty()) {
                // If the key is empty then store the groups to global,
                // otherwise iterate and validate all worlds in the entry
                if (e.getKey().isEmpty()) {
                    worlds.put("*", groups);
                } else {
                    e.getKey().forEach(entry -> {
                        if (entry.getKey().equalsIgnoreCase("world") && worldExists(entry.getValue())) {
                            worlds.put(entry.getValue(), groups);
                        }
                    });
                }
            }
        });

        return worlds;
    }

    @Override
    public List<String> fetchGroups() {
        List<String> groups = new ArrayList<>();

        service.getGroupSubjects().getAllSubjects().forEach(subject -> groups.add(subject.getIdentifier()));

        return groups;
    }

    @Override
    public void addGroup(String player, String group, String world) {
        StringBuilder builder = new StringBuilder()
                .append("pex user ")
                .append(player)
                .append(" parent add group ")
                .append(group);
        if (world != null && !world.isEmpty() && !world.equals("*")) {
            builder.append(" -c world=")
                    .append(world);
        }

        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), builder.toString());
    }

    @Override
    public void removeGroup(String player, String group, String world) {
        StringBuilder builder = new StringBuilder()
                .append("pex user ")
                .append(player)
                .append(" parent remove group ")
                .append(group);
        if (world != null && !world.isEmpty() && !world.equals("*")) {
            builder.append(" -c world=")
                    .append(world);
        }

        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), builder.toString());
    }

    private boolean groupExists(String group) {
        boolean matched = false;

        for (Subject subject : service.getGroupSubjects().getAllSubjects()) {
            if (subject.getIdentifier().equalsIgnoreCase(group)) {
                matched = true;
            }
        }

        return matched;
    }

    private boolean worldExists(String world) {
        boolean matched = false;

        for (World w : Sponge.getServer().getWorlds()) {
            if (w.getName().equals(world)) {
                matched = true;
            }
        }

        return matched;
    }
}
