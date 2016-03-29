package com.enjin.sponge.api.conversation;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

public abstract class InteractiveValidatingPrompt implements InteractivePrompt {
	public InteractivePrompt acceptInput(InteractiveContext context, Text input) {
		if(this.isInputValid(context, input)) {
			return this.acceptValidatedInput(context, input);
		} else {
			String failPrompt = this.getFailedValidationText(context, input);
			if(failPrompt != null) {
				context.getReceiver().sendMessage(ChatTypes.CHAT, Text.of(TextColors.RED, failPrompt));
			}

			return this;
		}
	}

	public boolean waitForInput(InteractiveContext context) {
		return true;
	}

	protected abstract boolean isInputValid(InteractiveContext var1, Text var2);

	protected abstract InteractivePrompt acceptValidatedInput(InteractiveContext var1, Text var2);

	protected String getFailedValidationText(InteractiveContext context, Text invalidInput) {
		return null;
	}
}
