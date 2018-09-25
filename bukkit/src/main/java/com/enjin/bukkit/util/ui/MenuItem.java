package com.enjin.bukkit.util.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class MenuItem extends MenuClickBehavior {
    private MenuBase menu;
    private ItemStack stack;
    private String text;
    private List<String> descriptions = new ArrayList<>();

    private int slot = 0;

    public MenuItem(String text) {
        this(text, new ItemStack(Material.PAPER));
    }

    public MenuItem(String text, Material material) {
        this(text, new ItemStack(material));
    }

    public MenuItem(String text, ItemStack stack) {
        this.text = text;
        this.stack = stack;
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
        return stack.getAmount();
    }

    public void setQuantity(int quantity) {
        stack.setAmount(quantity);
    }

    public String getText() {
        return text;
    }

    public void setDescriptions(List<String> lines) {
        descriptions = lines;
    }

    public ItemStack getSingleItemStack() {
        ItemStack slot = new ItemStack(stack);
        ItemMeta meta = slot.getItemMeta();
        meta.setDisplayName(getText());
        meta.setLore(descriptions);
        slot.setItemMeta(meta);
        slot.setAmount(1);

        return slot;
    }

    public ItemStack getItemStack() {
        ItemStack slot = new ItemStack(stack);
        ItemMeta meta = slot.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(getText());
            meta.setLore(descriptions);
            slot.setItemMeta(meta);
        }

        return slot;
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
