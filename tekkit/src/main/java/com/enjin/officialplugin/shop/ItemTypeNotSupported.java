package com.enjin.officialplugin.shop;

/**
 * If for some reason we try to add an item to a shop which is
 * set to categories, or vise versa then we throw this error.
 *
 * @author Joshua Reetz
 */
public class ItemTypeNotSupported extends Throwable {

    /**
     *
     */
    private static final long serialVersionUID = 5517328419867401859L;

    public ItemTypeNotSupported(String string) {
        super(string);
    }

}
