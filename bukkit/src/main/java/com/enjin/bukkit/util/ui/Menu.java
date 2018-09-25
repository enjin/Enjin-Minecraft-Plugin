package com.enjin.bukkit.util.ui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class Menu extends MenuHolder {
    private final String    title;
    private       int       rows;
    protected     Inventory inventory;

    public Menu(String title, int rows) {
        super(9 * 6);

        this.title = title;
        this.rows = rows;
    }

    public String getTitle() {
        return title;
    }

    public int getMaxItems() {
        return rows * 9;
    }

    public boolean addMenuItem(MenuItem item, int x, int y) {
        return addMenuItem(item, y * 9 + x);
    }

    public boolean addMenuItem(MenuItem item, int x, int y, short durability) {
        return addMenuItem(item, y * 9 + x, durability);
    }

    public int getRows() {
        return rows;
    }

    public void destroy() {
        this.inventory = null;
        super.items = new MenuItem[items.length];
    }

    public void setRows(int newrows) {
        if (this.rows != newrows) {
            if (this.inventory != null) {
                inventory.clear();
            }

            this.rows = newrows;
            this.inventory = Bukkit.createInventory(this, rows * 9, title);
            updateInventory();
        }
    }

    public Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, rows * 9, title);
        }

        return this.inventory;
    }

    @Override
    protected MenuHolder clone() {
        MenuHolder clone = new Menu(title, rows);
        clone.setExitOnClickOutside(exitOnClickOutside);
        clone.setMenuCloseBehavior(menuCloseBehavior);
        clone.items = items.clone();
        clone.updateInventory();

        return clone;
    }
}
