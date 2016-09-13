package com.enjin.bukkit.util.ui;

import com.enjin.core.Enjin;
import com.google.common.collect.Maps;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public abstract class MenuHolder extends MenuBase implements InventoryHolder {
    private Map<UUID, Long> cooldown = Maps.newConcurrentMap();

    protected MenuHolder(int max_items) {
        super(max_items);
    }

    public void openMenu(Player player) {
        if (!cooldown.containsKey(player.getUniqueId()) || System.currentTimeMillis() - cooldown.get(player.getUniqueId()) > 1000) {
            cooldown.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            return;
        }

        if (getInventory().getViewers().contains(player)) {
            Enjin.getLogger().debug(player.getName() + " is already viewing " + getInventory().getTitle());
            return;
        }

        player.openInventory(getInventory());
        Enjin.getLogger().debug("Opening menu for player " + player.getName());
    }

    public void closeMenu(Player player) {
        if (getInventory().getViewers().contains(player)) {
            getInventory().getViewers().remove(player);
            player.closeInventory();
        }
    }

    protected boolean selectMenuItem(Inventory inventory, Player player, int index, InventoryClickEvent e) {
        boolean allow = false;

        if (index > -1 && index < getMaxItems()) {
            MenuItem item = items[index];

            if (item != null) {
                allow = item.onClick(player, e);
            }
        }

        player.updateInventory();
        return allow;
    }

    public boolean addMenuItem(MenuItem item, int index) {
        if (index < 0 || index >= getMaxItems()) {
            return false;
        }

        if (item == null) {
            return false;
        }

        ItemStack slot = getInventory().getItem(index);

        if (slot != null && slot.getType() != Material.AIR) {
            return false;
        }

        getInventory().setItem(index, item.getItemStack());
        items[index] = item;
        item.addToMenu(this);

        return true;
    }

    public boolean removeMenuItem(int index) {
        if (index < 0 || index >= getMaxItems()) {
            return false;
        }

        ItemStack slot = getInventory().getItem(index);

        if (slot == null || slot.getType() == Material.AIR) {
            return false;
        }

        getInventory().clear(index);
        MenuItem remove = items[index];
        items[index] = null;

        if (remove != null) {
            remove.removeFromMenu(this);
        }

        return true;
    }

    public void updateMenu() {
        for (HumanEntity entity : getInventory().getViewers()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.updateInventory();
            }
        }
    }

    public void updateInventory() {
        getInventory().clear();

        for (int i = 0; i < getMaxItems(); i++) {
            MenuItem item = super.items[i];

            if (item != null) {
                getInventory().setItem(i, item.getItemStack());
            }
        }
    }

    public abstract Inventory getInventory();

    protected abstract MenuHolder clone();
}