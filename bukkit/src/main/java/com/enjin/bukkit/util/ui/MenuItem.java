package com.enjin.bukkit.util.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

public abstract class MenuItem extends MenuClickBehavior {
    private MenuBase menu;
    private int quantity;
    private MaterialData icon;
    private String text;
    private List<String> descriptions = new ArrayList<>();

    // Additional Values
    private short data = 0;
    private int slot = 0;

    public MenuItem(String text) {
        this(text, new MaterialData(Material.PAPER));
    }

    public MenuItem(String text, MaterialData icon) {
        this(text, icon, 1);
    }

    @SuppressWarnings("deprecation")
    public MenuItem(String text, MaterialData icon, int quantity) {
        this.text = text;
        this.icon = icon;
        this.quantity = quantity;
        this.data = icon.getData();
    }

    public MenuItem(String text, MaterialData icon, short data) {
        this.text = text;
        this.icon = icon;
        this.quantity = 1;
        this.data = data;
    }

    public MenuItem(String text, MaterialData icon, int quantity, short data) {
        this.text = text;
        this.icon = icon;
        this.quantity = quantity;
        this.data = data;
    }

    protected void addToMenu(MenuBase menu) {
        this.menu = menu;
    }

    protected void removeFromMenu(MenuBase menu) {
        if (this.menu == null) {
            this.menu = null;
        }
    }

    public MenuBase getMenu() {
        return menu;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public MaterialData getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public void setDescriptions(List<String> lines) {
        descriptions = lines;
    }

    public ItemStack getSingleItemStack() {
        ItemStack slot = new ItemStack(getIcon().getItemType(), 1, data);
        ItemMeta meta = slot.getItemMeta();
        meta.setDisplayName(getText());
        meta.setLore(descriptions);
        slot.setItemMeta(meta);

        return slot;
    }

    public ItemStack getItemStack() {
        ItemStack slot = new ItemStack(getIcon().getItemType(), getQuantity(), data);
        ItemMeta meta = slot.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(getText());
            meta.setLore(descriptions);
            slot.setItemMeta(meta);
        }

        return slot;
    }

    public void setData(short data) {
        this.data = data;
    }

    public short getData() {
        return data;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public abstract void onClick(Player player);

    public boolean onClick(Player player, InventoryClickEvent e) {
        onClick(player);
        return false;
    }
}
