package com.enjin.core;

public interface EnjinPlugin {
    InstructionHandler getInstructionHandler();

    long getServerId();
}
