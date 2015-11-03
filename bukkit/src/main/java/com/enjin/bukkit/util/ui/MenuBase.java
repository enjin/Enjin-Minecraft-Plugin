package com.enjin.bukkit.util.ui;

import org.bukkit.entity.Player;

public abstract class MenuBase {
    private final int max_items;
    protected MenuItem[] items;
    protected boolean exitOnClickOutside = true;
    protected MenuCloseBehavior menuCloseBehavior;

    protected MenuBase(int max_items) {
        this.max_items = max_items;
        this.items = new MenuItem[max_items];
    }

    public void setMenuCloseBehavior(MenuCloseBehavior menuCloseBehavior) {
        this.menuCloseBehavior = menuCloseBehavior;
    }

    public MenuCloseBehavior getMenuCloseBehavior() {
        return menuCloseBehavior;
    }

    public void setExitOnClickOutside(boolean exit) {
        this.exitOnClickOutside = exit;
    }

    public boolean exitOnClickOutside() {
        return exitOnClickOutside;
    }

    public int getMaxItems() {
        return max_items;
    }

    public abstract void openMenu(Player player);

    public abstract void closeMenu(Player player);

    public void switchMenu(MenuAPI api, Player player, MenuBase menu) {
        api.switchMenu(player, this, menu);
    }
}
