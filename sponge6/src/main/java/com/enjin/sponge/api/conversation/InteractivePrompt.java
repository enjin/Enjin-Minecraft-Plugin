package com.enjin.sponge.api.conversation;

import org.spongepowered.api.text.Text;

public interface InteractivePrompt extends Cloneable {
    InteractivePrompt END_OF_CONVERSATION = null;

    Text getPromptText(InteractiveContext context);

    boolean waitForInput(InteractiveContext context);

    InteractivePrompt acceptInput(InteractiveContext context, Text input);
}
