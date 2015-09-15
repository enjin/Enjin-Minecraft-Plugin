package com.enjin.rpc.mappings.mappings.plugin;

import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@ToString
public class Instruction {
    @Getter
    private InstructionCode code;
    @Getter
    private Map<String, Object> data;
}
