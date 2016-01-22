package com.enjin.core;

public interface EnjinPlugin {
    InstructionHandler getInstructionHandler();

    void debug(String message);
}
