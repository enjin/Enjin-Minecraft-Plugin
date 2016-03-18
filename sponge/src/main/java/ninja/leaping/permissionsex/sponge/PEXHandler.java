package ninja.leaping.permissionsex.sponge;

import com.enjin.core.Enjin;
import com.enjin.sponge.listeners.ConnectionListener;
import com.enjin.sponge.permissions.PermissionHandler;
import ninja.leaping.permissionsex.PermissionsEx;
import ninja.leaping.permissionsex.data.SubjectCache;
import ninja.leaping.permissionsex.data.SubjectDataReference;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.permission.PermissionService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PEXHandler implements PermissionHandler {
	private Map<UUID, SubjectDataReference> references = new HashMap<>();

	public void onJoin(Join event) {
		final Player player = event.getTargetEntity();

		final ServiceManager services = Sponge.getServiceManager();
		final Optional<ProviderRegistration<PermissionService>> potentialRegistration = services.getRegistration(PermissionService.class);

		if (potentialRegistration.isPresent()) {
			final PermissionsExPlugin service = (PermissionsExPlugin) potentialRegistration.get().getProvider();
			final SubjectCache cache = service.getManager().getSubjects(PermissionsEx.SUBJECTS_USER).persistentData();

			try {
				Enjin.getLogger().info("Adding listener to reference.");
				SubjectDataReference reference = cache.getReference(player.getUniqueId().toString());
				reference.onUpdate(data -> ConnectionListener.updatePlayerRanks(player));

				references.put(player.getUniqueId(), reference);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	public void onDisconnect(Disconnect event) {
		final Player player = event.getTargetEntity();
		references.remove(player.getUniqueId());
	}
}
