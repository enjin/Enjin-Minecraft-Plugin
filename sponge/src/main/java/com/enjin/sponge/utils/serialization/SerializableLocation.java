package com.enjin.sponge.utils.serialization;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class SerializableLocation {
    @Getter
    private String world;
    @Getter
    private double x;
    @Getter
    private double y;
    @Getter
    private double z;

    public SerializableLocation (Location<World> location) {
        this.world = location.getExtent().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public Location<World> toLocation() {
        World world = Sponge.getServer().getWorld(this.world).get();
		return world == null ? null : world.getLocation(x, y, z);
    }
}
