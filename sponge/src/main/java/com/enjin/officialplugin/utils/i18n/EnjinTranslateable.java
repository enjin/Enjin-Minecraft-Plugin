package com.enjin.officialplugin.utils.i18n;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translatable;
import org.spongepowered.api.text.translation.Translation;

public class EnjinTranslateable implements Translatable {
    private Translation translation;

    public EnjinTranslateable(Text text) {
        translation = new FixedTranslation(text.toString());
    }

    public EnjinTranslateable(String text) {
        translation = new FixedTranslation(text);
    }

    @Override
    public Translation getTranslation() {
        return translation;
    }
}
