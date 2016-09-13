package com.enjin.sponge.api.conversation;

public abstract class InteractiveTextPrompt implements InteractivePrompt {
    @Override
    public final boolean waitForInput(InteractiveContext context) {
        return true;
    }
}
