package com.enjin.sponge.api.conversation;

import org.spongepowered.api.text.Text;

public interface InteractiveCanceller {
	boolean cancelBasedOnInput(InteractiveContext context, Text input);
}
