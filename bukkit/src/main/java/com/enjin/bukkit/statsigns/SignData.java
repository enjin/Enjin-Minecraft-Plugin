package com.enjin.bukkit.statsigns;

import com.enjin.bukkit.util.serialization.SerializableLocation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.block.Block;

@ToString
@NoArgsConstructor
public class SignData {
    @Getter
    private SerializableLocation location;
    @Getter
    private SignType type;
    @Getter
    private SignType.SubType subType;
    @Getter
    private Integer itemId;
    @Getter
    private int index;
    @Getter @Setter
    private transient SerializableLocation headLocation;

    public SignData(Block block, SignType type, SignType.SubType subType, int index) {
        this(block, type, subType, null, index);
    }

    public SignData(Block block, SignType type, SignType.SubType subType, Integer itemId, int index) {
        this.location = new SerializableLocation(block.getLocation());
        this.type = type;
        this.subType = subType;
        this.itemId = itemId;
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SignData data = (SignData) o;

        return location.equals(data.location);

    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }
}
