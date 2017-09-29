package com.enjin.sponge.api.conversation;

import org.spongepowered.api.text.Text;

public abstract class InteractiveMessagePrompt implements InteractivePrompt {
    @Override
    public final boolean waitForInput(InteractiveContext context) {
        return false;
    }

    @Override
    public final InteractivePrompt acceptInput(InteractiveContext context, Text input) {
        return this.getNextPrompt(context);
    }

    protected abstract InteractivePrompt getNextPrompt(InteractiveContext context);
}
