package com.enjin.bukkit.config;

import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecutedCommandsConfig extends JsonConfig {
    @Getter
    private List<ExecutedCommand> executedCommands = new ArrayList<>();

    public List<Map<String, Object>> getExecutedCommandsMapList() {
        synchronized (this) {
            return executedCommands.stream().map(ExecutedCommand::toMap).collect(Collectors.toList());
        }
    }
}
