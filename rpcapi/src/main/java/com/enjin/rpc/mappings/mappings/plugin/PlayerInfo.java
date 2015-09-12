package com.enjin.rpc.mappings.mappings.plugin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class PlayerInfo {
    @Getter
    private String name;
    @Getter
    private boolean vanish;
    @Getter
    private String uuid;

    public PlayerInfo(String name, boolean vanished, UUID uuid) {
        this.name = name;
        this.vanish = vanished;
        this.uuid = uuid.toString().replace("-", "");
    }

    public PlayerInfo(String name, UUID uuid) {
        this(name, false, uuid);
    }
}
