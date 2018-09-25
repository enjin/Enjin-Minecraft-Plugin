package com.enjin.bukkit.util.ui;

import com.enjin.core.Enjin;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MenuAPI implements Listener {
    private static boolean listener = false;

    private final JavaPlugin instance;

    public MenuAPI(JavaPlugin plugin) {
        this.instance = plugin;

        if (!listener) {
            MenuAPI.listener = true;
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            Enjin.getLogger().debug("Registering MenuAPI listener to " + plugin.getName());
        }
    }

    public void switchMenu(final Player player, MenuBase fromMenu, final MenuBase toMenu) {
        fromMenu.closeMenu(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                toMenu.openMenu(player);
            }
        }.runTask(instance);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMenuItemDragged(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory != null && inventory.getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMenuItemClicked(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        Player    player    = (Player) event.getWhoClicked();

        if (inventory.getHolder() instanceof MenuHolder) {
            MenuHolder menu  = (MenuHolder) inventory.getHolder();
            boolean    allow = false;

            if (event.isRightClick()) {
                event.setCancelled(true);
                return;
            }

            if (event.getWhoClicked() instanceof Player) {
                if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
                    if (menu.exitOnClickOutside()) {
                        menu.closeMenu(player);
                    }
                } else {
                    int index = event.getRawSlot();

                    if (index < inventory.getSize()) {
                        allow = menu.selectMenuItem(inventory, player, index, event);
                    } else {
                        if (menu.exitOnClickOutside()) {
                            menu.closeMenu(player);
                        } else {
                            // If the Menu allows clicking outside, we'll allow left clicks only
                            allow = ClickType.LEFT.equals(event.getClick());
                        }
                    }
                }
            }

            event.setCancelled(!allow);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMenuClosed(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Inventory inventory = event.getInventory();
            Player    player    = (Player) event.getPlayer();
            if (inventory.getHolder() instanceof MenuHolder) {
                MenuHolder        menu              = (MenuHolder) inventory.getHolder();
                MenuCloseBehavior menuCloseBehavior = menu.getMenuCloseBehavior();

                if (menuCloseBehavior != null) {
                    menuCloseBehavior.onClose(player);
                }
            }
        }
    }

    public static Menu createMenu(String title, int rows) {
        return new Menu(title, rows);
    }

    public static Menu createMenu(String title, boolean center, int rows) {
        if (!center) {
            return new Menu(title, rows);
        }
        int    spaces = (32 - title.length()) / 2;
        String name   = "";
        for (int i = 0; i < spaces; i++) {
            name += " ";
        }
        name += title;
        return new Menu(name, rows);
    }

    public static MenuHolder cloneMenu(MenuHolder menu) {
        return menu.clone();
    }

    public static void removeMenu(MenuHolder menu) {
        for (HumanEntity viewer : menu.getInventory().getViewers()) {
            if (viewer instanceof Player) {
                menu.closeMenu((Player) viewer);
            } else {
                viewer.closeInventory();
            }
        }
    }
}
