package com.enjin.bukkit.config;

import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

public class ExecutedCommandsConfig extends JsonConfig {
    @Getter
    private List<ExecutedCommand> executedCommands = Lists.newArrayList();
}
