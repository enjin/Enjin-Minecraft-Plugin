package com.enjin.core;

public interface EnjinPlugin {
    public InstructionHandler getInstructionHandler();

    public void debug(String message);
}
