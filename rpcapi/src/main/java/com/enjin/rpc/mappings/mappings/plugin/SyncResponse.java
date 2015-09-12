package com.enjin.rpc.mappings.mappings.plugin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class SyncResponse {
    @Getter
    private List<Object> instructions;
    @Getter
    private List<Object> commands;
    @Getter
    private String status;
}
