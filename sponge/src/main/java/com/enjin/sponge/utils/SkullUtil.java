package com.enjin.sponge.utils;

import com.enjin.core.Enjin;
import com.enjin.sponge.EnjinMinecraftPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
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
                Optional<GameProfile> existing = entity.get(Keys.REPRESENTED_PLAYER);
                if (existing.isPresent()) {
                    GameProfile e = existing.get();
                    Optional<String> existingName = e.getName();
                    if (existingName.isPresent() && existingName.get().equalsIgnoreCase(name)) {
                        Enjin.getLogger().debug("Head already is set to player: " + name);
                        return;
                    }
                }

                GameProfileManager manager = Sponge.getServer().getGameProfileManager();
                EnjinMinecraftPlugin.getInstance().getAsync().execute(() -> {
                    Optional<GameProfile> profile = manager.getCache().getOrLookupByName(name);
                    if (profile.isPresent()) {
                        Enjin.getLogger().debug("Getting cached profile for " + name);
                        final GameProfile p = profile.get();
                        if (!p.isFilled() || !p.getPropertyMap().containsKey("textures")) {
                            Enjin.getLogger().debug("Filling game profile for " + name);
                            manager.fill(profile.get(), true, true);
                        }
                    }

                    if (profile.isPresent()) {
                        Enjin.getLogger().debug("Updating skull owner to " + name);
                        final GameProfile p = profile.get();
                        EnjinMinecraftPlugin.getInstance().getSync().execute(() -> entity.offer(Keys.REPRESENTED_PLAYER, p));
                    }
                });
            } catch (Exception e) {
                Enjin.getLogger().catching(e);
            }
        }
    }
}
