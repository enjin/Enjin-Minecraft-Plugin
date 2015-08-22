package com.enjin.officialplugin.utils.commands;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@EqualsAndHashCode
public class CommandWrapper {
    @Getter
    private UUID uuid;
    @Getter
    private String command;
    @Getter
    private String id;
    @Getter @Setter
    private String hash;

    public CommandWrapper(UUID uuid, String command, String id) {
        this.uuid = uuid;
        this.command = command;
        this.id = id;
    }
}
