package com.enjin.officialplugin.shop;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.ServerShop.Type;

public class ShopListener implements Listener {
	
	ConcurrentHashMap<String, PlayerShopsInstance> activeshops = new ConcurrentHashMap<String, PlayerShopsInstance>();
	ConcurrentHashMap<String, String> playersdisabledchat = new ConcurrentHashMap<String, String>();

	@EventHandler(priority = EventPriority.LOW)
	public void preCommandListener(PlayerCommandPreprocessEvent event) {
		if(event.isCancelled()) {
			return;
		}
		String[] args = event.getMessage().split(" ");
		if(args[0].equalsIgnoreCase("/" + EnjinMinecraftPlugin.BUY_COMMAND)) {
			Player player = event.getPlayer();
			if(args.length > 1 && args[1].equalsIgnoreCase("history")) {
				if(args.length > 2 && player.hasPermission("enjin.history")) {
					player.sendMessage(ChatColor.RED + "Fetching shop history information for " + args[2] + ", please wait...");
					Thread dispatchThread = new Thread(new PlayerHistoryGetter(this, player, args[2]));
		            dispatchThread.start();
		            event.setCancelled(true);
		            return;
				}else {
					player.sendMessage(ChatColor.RED + "Fetching your shop history information, please wait...");
					Thread dispatchThread = new Thread(new PlayerHistoryGetter(this, player, player.getName()));
		            dispatchThread.start();
		            event.setCancelled(true);
		            return;
				}
			}
			if(activeshops.containsKey(player.getName().toLowerCase())) {
				PlayerShopsInstance psi = activeshops.get(player.getName().toLowerCase());
				//If it's been over 10 minutes, re-retrieve it.
				if(psi.getRetrievalTime() + (1000*60*10) < System.currentTimeMillis()) {
					player.sendMessage(ChatColor.RED + "Fetching shop information, please wait...");
					Thread dispatchThread = new Thread(new PlayerShopGetter(this, player));
		            dispatchThread.start();
		            event.setCancelled(true);
		            return;
				}
				playersdisabledchat.put(player.getName().toLowerCase(), player.getName());
				//If it's just the /buy parameter, let's just reset to the shop topmost category.
				if(args.length == 1) {
					//If they haven't selected a shop yet, show them the shop selection screen again.
					if(psi.getActiveShop() == null) {
						sendPlayerInitialShopData(player, psi);
					//Else, if they have, show them the shop main menu again.
					}else {
						ServerShop selectedshop = psi.getActiveShop();
						//We need to see if it only has one category. If so, open that category.
						if(selectedshop.getType() == Type.Category && selectedshop.getItems().size() == 1) {
							ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
							psi.setActiveCategory(category);
						//If it has items or more than one category show the shop main page.
						}else {
							psi.setActiveCategory(selectedshop);
						}
						sendPlayerShopData(player, psi, psi.getActiveCategory(), 0);
					}
				}else {
					if(args[1].equalsIgnoreCase("shop")) {
						if(args.length > 2) {
							try {
								//We need to take off 1 of the shop number because internally we start with 0, not 1.
								int pagenumber = Integer.parseInt(args[2].trim()) -1;
								if(pagenumber < psi.getServerShopCount() && pagenumber >= 0) {
									ServerShop selectedshop = psi.getServerShop(pagenumber);
									//We need to see if it only has one category. If so, open that category.
									if(selectedshop.getType() == Type.Category && selectedshop.getItems().size() == 1) {
										ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
										psi.setActiveShop(selectedshop);
										psi.setActiveCategory(category);
										sendPlayerShopData(player, psi, category, 0);
									//If it has items or more than one category show the shop main page.
									}else {
										psi.setActiveShop(selectedshop);
										psi.setActiveCategory(selectedshop);
										sendPlayerShopData(player, psi, selectedshop, 0);
									}
								}else {
									player.sendMessage(ChatColor.RED + "Invalid page number.");
								}
							}catch (NumberFormatException e) {
								player.sendMessage(ChatColor.RED + "Invalid page number.");
							}
						}else {
							//If they didn't specify a shop, let's show the initial shop data again.
							psi.setActiveCategory(null);
							psi.setActiveShop(null);
							sendPlayerInitialShopData(player, psi);
						}
					}else if(args[1].equals("page")) {
						if(args.length > 2) {
							if(psi.getActiveCategory() != null) {
								ShopItemAdder category = psi.getActiveCategory();
								ArrayList<ArrayList<String>> pages;
								//This should never be null, but just in case.
								if(category.getPages() == null) {
									category.setPages(ShopUtils.formatPages(psi.getActiveShop(), category));
								}
								pages = category.getPages();
								try {
									int pagenumber = Integer.parseInt(args[2]) -1;
									if(pagenumber < pages.size() && pagenumber >= 0) {
										sendPlayerPage(player, pages.get(pagenumber));
									}else {
										player.sendMessage(ChatColor.RED + "Invalid page number.");
									}
								}catch(NumberFormatException e) {
									player.sendMessage(ChatColor.RED + "Invalid page number.");
								}
							}
						}else {
							player.sendMessage(ChatColor.RED + "Please specify a page number.");
						}
					}else if(args.length > 1){
						if(psi.getActiveShop() == null) {
							player.sendMessage(ChatColor.RED + "You need to select a shop first! Do /" + EnjinMinecraftPlugin.BUY_COMMAND + " to see the shops list.");
						}else {
							try {
								ShopItemAdder category = psi.getActiveCategory();
								int optionnumber = Integer.parseInt(args[1]) -1;
								if(optionnumber < category.getItems().size() && optionnumber >= 0) {
									//If it's a category, let's go into the category and list the first page.
									if(category.getType() == Type.Category) {
										ShopItemAdder newcategory = (ShopItemAdder) category.getItem(optionnumber);
										psi.setActiveCategory(newcategory);
										sendPlayerShopData(player, psi, newcategory, 0);
									}else {
										//It must be an item, let's send the item details page.
										sendPlayerPage(player, ShopUtils.getItemDetailsPage(psi.getActiveShop(), (ShopItem) category.getItem(optionnumber)));
									}
								}else {
									player.sendMessage(ChatColor.RED + "Invalid page number.");
								}
							}catch(NumberFormatException e) {
								player.sendMessage(ChatColor.RED + "Invalid page number.");
							}
						}
					}
				}
			}else {
				player.sendMessage(ChatColor.RED + "Fetching shop information, please wait...");
				Thread dispatchThread = new Thread(new PlayerShopGetter(this, player));
	            dispatchThread.start();
			}
			event.setCancelled(true);
		}else if(args[0].equalsIgnoreCase("/ec")) {
			if(playersdisabledchat.containsKey(event.getPlayer().getName().toLowerCase())) {
				playersdisabledchat.remove(event.getPlayer().getName().toLowerCase());
				event.getPlayer().sendMessage(ChatColor.GREEN + "Your chat is now enabled.");
				event.setCancelled(true);
			}
		}
	}
	
	public void removePlayer(String player) {
		player = player.toLowerCase();
		playersdisabledchat.remove(player);
		activeshops.remove(player);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(PlayerChatEvent event) {
		if(event.isCancelled()) {
			return;
		}
		//We don't need to do anything if our list is empty.
		if(!playersdisabledchat.isEmpty()) {
			//If a player in the list chats, remove them from the list, otherwise, don't send him messages.
			if(playersdisabledchat.containsKey(event.getPlayer().getName().toLowerCase())) {
				playersdisabledchat.remove(event.getPlayer().getName().toLowerCase());
			}else {
				Set<Player> recipients = event.getRecipients();
				//Make sure we aren't throwing huge errors on spigot!
				try {
					for(Player recipient : recipients) {
						if(playersdisabledchat.containsKey(recipient.getName().toLowerCase())) {
							recipients.remove(recipient);
						}
					}
				}catch(Exception e) {
					//Don't do anything... bugged bukkit implementation.
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		//If a player quits, let's reset the shop data and remove them from the list.
		String player = event.getPlayer().getName().toLowerCase();
		playersdisabledchat.remove(player);
		activeshops.remove(player);
	}
	
	public void sendPlayerInitialShopData(Player player, PlayerShopsInstance shops) {
		if(shops.getServerShopCount() == 1) {
			ServerShop selectedshop = shops.getServerShop(0);
			shops.setActiveShop(selectedshop);
			shops.setActiveCategory(selectedshop);
			//If the shop only has one category, let's automatically go into it.
			if(selectedshop.getType() == Type.Category && selectedshop.getItems().size() == 1) {
				ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
				shops.setActiveCategory(category);
				sendPlayerShopData(player, shops, category, 0);
			//If it has items or more than one category show the shop main page.
			}else {
				sendPlayerShopData(player, shops, selectedshop, 0);
			}
			return;
		}else {
			sendPlayerPage(player, ShopUtils.getShopListing(shops));
		}
	}
	
	public static void sendPlayerPage(Player player, ArrayList<String> page) {
		for(String line : page) {
			player.sendMessage(line);
		}
	}
	
	public void sendPlayerShopData(Player player, PlayerShopsInstance shops, ShopItemAdder category, int page) {
		ArrayList<ArrayList<String>> pages;
		if(category.getPages() == null) {
			pages = ShopUtils.formatPages(shops.getActiveShop(), category);
			category.setPages(pages);
		}else {
			pages = category.getPages();
		}
		sendPlayerPage(player, pages.get(page));
	}
	
	public void sendPlayerItemData(Player player, PlayerShopsInstance shops, ShopItem item) {
		sendPlayerPage(player, ShopUtils.getItemDetailsPage(shops.getActiveShop(), item));
	}
}
