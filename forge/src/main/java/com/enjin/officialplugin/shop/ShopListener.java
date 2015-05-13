package com.enjin.officialplugin.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinConsole;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.PlayerLogoutEvent;
import com.enjin.officialplugin.shop.ServerShop.Type;

public class ShopListener extends CommandBase {

    ConcurrentHashMap<String, PlayerShopsInstance> activeshops = new ConcurrentHashMap<String, PlayerShopsInstance>();
    ConcurrentHashMap<String, String> playersdisabledchat = new ConcurrentHashMap<String, String>();

    public void removePlayer(String player) {
        player = player.toLowerCase();
        playersdisabledchat.remove(player);
        activeshops.remove(player);
    }

    @ForgeSubscribe(priority = EventPriority.LOWEST)
    public void playerChatEvent(ServerChatEvent event) {
        if (event.isCanceled()) {
            return;
        }
        //If a player in the list chats, remove them from the list, otherwise, don't send him messages.
        if (playersdisabledchat.containsKey(event.player.username.toLowerCase())) {
            playersdisabledchat.remove(event.player.username.toLowerCase());
        }
        //We don't need to do anything if our list is empty.
        if (!playersdisabledchat.isEmpty()) {
            List<EntityPlayerMP> playerlist = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
            for (EntityPlayerMP recipient : playerlist) {
                //Only send the message to players that are not contained in the list.
                if (!playersdisabledchat.containsKey(recipient.username.toLowerCase())) {
                    recipient.sendChatToPlayer(event.component);
                }
            }
        }
    }

    @ForgeSubscribe(priority = EventPriority.LOWEST)
    public void onPlayerDisconnect(PlayerLogoutEvent event) {
        //If a player quits, let's reset the shop data and remove them from the list.
        String player = event.entityPlayer.username.toLowerCase();
        playersdisabledchat.remove(player);
        activeshops.remove(player);
    }

    public void sendPlayerInitialShopData(EntityPlayerMP player, PlayerShopsInstance shops) {
        if (shops.getServerShopCount() == 1) {
            ServerShop selectedshop = shops.getServerShop(0);
            shops.setActiveShop(selectedshop);
            shops.setActiveCategory(selectedshop);
            //If the shop only has one category, let's automatically go into it.
            if (selectedshop.getType() == Type.Category && selectedshop.getItems().size() == 1) {
                ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
                shops.setActiveCategory(category);
                sendPlayerShopData(player, shops, category, 0);
                //If it has items or more than one category show the shop main page.
            } else {
                sendPlayerShopData(player, shops, selectedshop, 0);
            }
            return;
        } else {
            sendPlayerPage(player, ShopUtils.getShopListing(shops));
        }
    }

    public static void sendPlayerPage(EntityPlayerMP player, ArrayList<String> page) {
        for (String line : page) {
            player.addChatMessage(line);
        }
    }

    public void sendPlayerShopData(EntityPlayerMP player, PlayerShopsInstance shops, ShopItemAdder category, int page) {
        ArrayList<ArrayList<String>> pages;
        if (category.getPages() == null) {
            pages = ShopUtils.formatPages(shops.getActiveShop(), category);
            category.setPages(pages);
        } else {
            pages = category.getPages();
        }
        sendPlayerPage(player, pages.get(page));
    }

    public void sendPlayerItemData(EntityPlayerMP player, PlayerShopsInstance shops, ShopItem item) {
        sendPlayerPage(player, ShopUtils.getItemDetailsPage(shops.getActiveShop(), item));
    }

    @Override
    public String getCommandName() {
        return EnjinMinecraftPlugin.BUY_COMMAND;
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] args) {
        EntityPlayerMP player = null;
        if (icommandsender instanceof EntityPlayerMP) {
            player = (EntityPlayerMP) icommandsender;
        } else {
            return;
        }

        //The new history command
        if (args.length > 0 && args[0].equalsIgnoreCase("history")) {
            if (args.length > 1 && isPlayerOp(player)) {
                player.addChatMessage(ChatColor.RED + "Fetching shop history information for " + args[1] + ", please wait...");
                Thread dispatchThread = new Thread(new PlayerHistoryGetter(this, player, args[1]));
                dispatchThread.start();
            } else {
                player.addChatMessage(ChatColor.RED + "Fetching your shop history information, please wait...");
                Thread dispatchThread = new Thread(new PlayerHistoryGetter(this, player, player.username));
                dispatchThread.start();
            }
            return;
        }

        if (activeshops.containsKey(player.username.toLowerCase())) {
            PlayerShopsInstance psi = activeshops.get(player.username.toLowerCase());
            //If it's been over 10 minutes, re-retrieve it.
            if (psi.getRetrievalTime() + (1000 * 60 * 10) < System.currentTimeMillis()) {
                player.addChatMessage(ChatColor.RED + "Fetching shop information, please wait...");
                Thread dispatchThread = new Thread(new PlayerShopGetter(this, player));
                dispatchThread.start();
                return;
            }
            playersdisabledchat.put(player.username.toLowerCase(), player.username);
            //If it's just the /buy parameter, let's just reset to the shop topmost category.
            if (args.length == 0) {
                //If they haven't selected a shop yet, show them the shop selection screen again.
                if (psi.getActiveShop() == null) {
                    sendPlayerInitialShopData(player, psi);
                    //Else, if they have, show them the shop main menu again.
                } else {
                    ServerShop selectedshop = psi.getActiveShop();
                    //We need to see if it only has one category. If so, open that category.
                    if (selectedshop.getType() == Type.Category && selectedshop.getItems().size() == 1) {
                        ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
                        psi.setActiveCategory(category);
                        //If it has items or more than one category show the shop main page.
                    } else {
                        psi.setActiveCategory(selectedshop);
                    }
                    sendPlayerShopData(player, psi, psi.getActiveCategory(), 0);
                }
            } else {
                if (args[0].equalsIgnoreCase("shop")) {
                    if (args.length > 1) {
                        try {
                            //We need to take off 1 of the shop number because internally we start with 0, not 1.
                            int pagenumber = Integer.parseInt(args[1].trim()) - 1;
                            if (pagenumber < psi.getServerShopCount() && pagenumber >= 0) {
                                ServerShop selectedshop = psi.getServerShop(pagenumber);
                                //We need to see if it only has one category. If so, open that category.
                                if (selectedshop.getType() == Type.Category && selectedshop.getItems().size() == 1) {
                                    ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
                                    psi.setActiveShop(selectedshop);
                                    psi.setActiveCategory(category);
                                    sendPlayerShopData(player, psi, category, 0);
                                    //If it has items or more than one category show the shop main page.
                                } else {
                                    psi.setActiveShop(selectedshop);
                                    psi.setActiveCategory(selectedshop);
                                    sendPlayerShopData(player, psi, selectedshop, 0);
                                }
                            } else {
                                player.addChatMessage(ChatColor.RED + "Invalid page number.");
                            }
                        } catch (NumberFormatException e) {
                            player.addChatMessage(ChatColor.RED + "Invalid page number.");
                        }
                    } else {
                        //If they didn't specify a shop, let's show the initial shop data again.
                        psi.setActiveCategory(null);
                        psi.setActiveShop(null);
                        sendPlayerInitialShopData(player, psi);
                    }
                } else if (args[0].equals("page")) {
                    if (args.length > 1) {
                        if (psi.getActiveCategory() != null) {
                            ShopItemAdder category = psi.getActiveCategory();
                            ArrayList<ArrayList<String>> pages;
                            //This should never be null, but just in case.
                            if (category.getPages() == null) {
                                category.setPages(ShopUtils.formatPages(psi.getActiveShop(), category));
                            }
                            pages = category.getPages();
                            try {
                                int pagenumber = Integer.parseInt(args[1]) - 1;
                                if (pagenumber < pages.size() && pagenumber >= 0) {
                                    sendPlayerPage(player, pages.get(pagenumber));
                                } else {
                                    player.addChatMessage(ChatColor.RED + "Invalid page number.");
                                }
                            } catch (NumberFormatException e) {
                                player.addChatMessage(ChatColor.RED + "Invalid page number.");
                            }
                        }
                    } else {
                        player.addChatMessage(ChatColor.RED + "Please specify a page number.");
                    }
                } else if (args.length > 0) {
                    if (psi.getActiveShop() == null) {
                        player.addChatMessage(ChatColor.RED + "You need to select a shop first! Do /" + EnjinMinecraftPlugin.BUY_COMMAND + " to see the shops list.");
                    } else {
                        try {
                            ShopItemAdder category = psi.getActiveCategory();
                            int optionnumber = Integer.parseInt(args[0]) - 1;
                            if (optionnumber < category.getItems().size() && optionnumber >= 0) {
                                //If it's a category, let's go into the category and list the first page.
                                if (category.getType() == Type.Category) {
                                    ShopItemAdder newcategory = (ShopItemAdder) category.getItem(optionnumber);
                                    psi.setActiveCategory(newcategory);
                                    sendPlayerShopData(player, psi, newcategory, 0);
                                } else {
                                    //It must be an item, let's send the item details page.
                                    sendPlayerPage(player, ShopUtils.getItemDetailsPage(psi.getActiveShop(), (ShopItem) category.getItem(optionnumber)));
                                }
                            } else {
                                player.addChatMessage(ChatColor.RED + "Invalid page number.");
                            }
                        } catch (NumberFormatException e) {
                            player.addChatMessage(ChatColor.RED + "Invalid page number.");
                        }
                    }
                }
            }
        } else {
            player.addChatMessage(ChatColor.RED + "Fetching shop information, please wait...");
            Thread dispatchThread = new Thread(new PlayerShopGetter(this, player));
            dispatchThread.start();
        }
    }

    boolean isPlayerOp(EntityPlayerMP player) {
        return MinecraftServer.getServer().getConfigurationManager().getOps().contains(player.getCommandSenderName().toLowerCase());
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "/" + EnjinMinecraftPlugin.BUY_COMMAND;
    }
}
