package com.enjin.sponge.utils;

import com.enjin.core.Enjin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class SkullUtil {
	public static void updateSkullOwner(Location<World> location, String name) {
		Optional<TileEntity> optionalEntity = location.getTileEntity();
		if (optionalEntity.isPresent()) {
			updateSkullOwner(optionalEntity.get(), name);
		}
	}

	public static void updateSkullOwner(TileEntity entity, String name) {
		if (entity.supports(SkullData.class) && entity.supports(RepresentedPlayerData.class)) {
			try {
				GameProfileManager manager = Sponge.getServer().getGameProfileManager();
				manager.get(name).whenComplete((profile, throwable) -> {
					if (profile != null) {
						manager.fill(profile, true, false).whenComplete((p, t) -> {
							if (p != null) {
								Enjin.getLogger().debug("Updating skull owner to " + name);
								entity.offer(Keys.REPRESENTED_PLAYER, profile);
							}
						});
					}
				});
			} catch (Exception e) {
				Enjin.getLogger().catching(e);
			}
		}
	}
}
