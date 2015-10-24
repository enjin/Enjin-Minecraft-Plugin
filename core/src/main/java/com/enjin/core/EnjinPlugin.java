package com.enjin.core;

public interface EnjinPlugin {
    public InstructionHandler getInstructionHandler();

    public default void debug(String message) {
        System.out.println(message);
    }
}
