package com.enjin.rpc.mappings.mappings.plugin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
public class SyncResponse {
    @Getter
    private List<Instruction> instructions = new ArrayList<>();
    @Getter
    private String            status;
}
