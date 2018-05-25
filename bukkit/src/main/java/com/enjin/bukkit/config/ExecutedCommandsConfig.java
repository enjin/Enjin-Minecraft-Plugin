package com.enjin.bukkit.config;

import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ExecutedCommandsConfig extends JsonConfig {
    @Getter
    private List<ExecutedCommand> executedCommands = new ArrayList<>();
}
