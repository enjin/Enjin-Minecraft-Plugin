package com.enjin.rpc.mappings.mappings.plugin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class PlayerGroupInfo {
    @Getter
    private String uuid;
    @Getter
    private Map<String, List<String>> worlds = new HashMap<String, List<String>>();

    public PlayerGroupInfo(UUID uuid) {
        this.uuid = uuid.toString();
    }
}
