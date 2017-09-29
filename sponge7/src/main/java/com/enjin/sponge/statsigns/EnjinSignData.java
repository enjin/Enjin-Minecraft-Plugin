package com.enjin.sponge.statsigns;

import com.enjin.sponge.utils.serialization.SerializableLocation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@ToString
@NoArgsConstructor
public class EnjinSignData {
    @Getter
    private SerializableLocation location;
    @Getter
    private EnjinSignType type;
    @Getter
    private EnjinSignType.SubType subType;
    @Getter
    private Integer itemId;
    @Getter
    private int index;
    @Getter
    @Setter
    private transient SerializableLocation headLocation;

    public EnjinSignData(Location<World> location, EnjinSignType type, EnjinSignType.SubType subType, int index) {
        this(location, type, subType, null, index);
    }

    public EnjinSignData(Location<World> location, EnjinSignType type, EnjinSignType.SubType subType, Integer itemId, int index) {
        this.location = new SerializableLocation(location);
        this.type = type;
        this.subType = subType;
        this.itemId = itemId;
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnjinSignData data = (EnjinSignData) o;

        return location.equals(data.location);

    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }
}
