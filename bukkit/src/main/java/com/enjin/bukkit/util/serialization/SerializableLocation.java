package com.enjin.bukkit.util.serialization;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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

    public SerializableLocation(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(this.world);
        return new Location(world == null ? Bukkit.getWorlds().get(0) : world, x, y, z);
    }
}
