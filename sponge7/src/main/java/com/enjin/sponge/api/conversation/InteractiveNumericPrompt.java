package com.enjin.sponge.api.conversation;

import org.apache.commons.lang3.math.NumberUtils;
import org.spongepowered.api.text.Text;

public abstract class InteractiveNumericPrompt extends InteractiveValidatingPrompt {
    protected boolean isInputValid(InteractiveContext context, Text input) {
        return NumberUtils.isNumber(input.toPlain()) && this.isNumberValid(context, NumberUtils.createNumber(input.toPlain()));
    }

    protected boolean isNumberValid(InteractiveContext context, Number input) {
        return true;
    }

    protected InteractivePrompt acceptValidatedInput(InteractiveContext context, Text input) {
        try {
            return this.acceptValidatedInput(context, NumberUtils.createNumber(input.toPlain()));
        } catch (NumberFormatException var3) {
            return this.acceptValidatedInput(context, NumberUtils.INTEGER_ZERO);
        }
    }

    protected abstract InteractivePrompt acceptValidatedInput(InteractiveContext context, Number number);

    protected String getFailedValidationText(InteractiveContext context, Text invalidInput) {
        return NumberUtils.isNumber(invalidInput.toPlain()) ? this.getFailedValidationText(context, NumberUtils.createNumber(invalidInput.toPlain())) : this.getInputNotNumericText(context, invalidInput);
    }

    protected String getInputNotNumericText(InteractiveContext context, Text invalidInput) {
        return null;
    }

    protected String getFailedValidationText(InteractiveContext context, Number invalidInput) {
        return null;
    }
}
