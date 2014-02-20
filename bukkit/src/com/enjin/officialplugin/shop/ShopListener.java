package com.enjin.officialplugin.shop;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.ServerShop.Type;
import com.enjin.officialplugin.threaded.SendItemPurchaseToEnjin;

public class ShopListener implements Listener {

	ConcurrentHashMap<String, PlayerShopsInstance> activeshops = new ConcurrentHashMap<String, PlayerShopsInstance>();
	ConcurrentHashMap<String, String> playersdisabledchat = new ConcurrentHashMap<String, String>();
	ConcurrentHashMap<String, ShopItemBuyer> playersbuying = new ConcurrentHashMap<String, ShopItemBuyer>();
	
	EnjinMinecraftPlugin plugin;
	
	public ShopListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}

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
							psi.setActiveItem(null);
							//If it has items or more than one category show the shop main page.
						}else {
							psi.setActiveCategory(selectedshop);
							psi.setActiveItem(null);
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
										psi.setActiveItem(null);
										sendPlayerShopData(player, psi, category, 0);
										//If it has items or more than one category show the shop main page.
									}else {
										psi.setActiveShop(selectedshop);
										psi.setActiveCategory(selectedshop);
										psi.setActiveItem(null);
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
							psi.setActiveItem(null);
							sendPlayerInitialShopData(player, psi);
						}
					}else if(args[1].equals("page")) {
						if(args.length > 2) {
							if(psi.getActiveCategory() != null) {
								psi.setActiveItem(null);
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
					}else if(args[1].equals("item")) {
						if(args.length > 2) {
							if(psi.getActiveShop() == null) {
								player.sendMessage(ChatColor.RED + "You need to select a shop first! Do /" + EnjinMinecraftPlugin.BUY_COMMAND + " to see the shops list.");
							}else {
								try {
									ShopItemAdder category = psi.getActiveCategory();
									int optionnumber = Integer.parseInt(args[2]) -1;
									if(optionnumber < category.getItems().size() && optionnumber >= 0) {
										//If it's a category, let's go into the category and list the first page.
										if(category.getType() == Type.Category) {
											player.sendMessage(ChatColor.RED + "You need to select a category first!");
										}else {
											ShopItem item = (ShopItem) category.getItem(optionnumber);
											//Make sure you can purchase the item with points.
											if(item.points != "") {
												ShopItemBuyer buyer = new ShopItemBuyer(item);
												playersbuying.put(player.getName(), buyer);
												sendPlayerInitialBuyData(player, buyer);
											}else {
												//Show the player the message to go to the website to purchase.
												player.sendMessage(ChatColor.RED + "Sorry, that item cannot be purchased with points, please go to the website to buy it.");
											}
										}
									}else {
										player.sendMessage(ChatColor.RED + "Invalid item number.");
									}
								}catch(NumberFormatException e) {
									player.sendMessage(ChatColor.RED + "Invalid item number.");
								}
							}
						}else {
							if(psi.getActiveItem() != null) {
								ShopItem item = psi.getActiveItem();
								if(item.points != "") {
									ShopItemBuyer buyer = new ShopItemBuyer(item);
									playersbuying.put(player.getName(), buyer);
									sendPlayerInitialBuyData(player, buyer);
									return;
								}else {
									//Show the player the message to go to the website to purchase.
									player.sendMessage(ChatColor.RED + "Sorry, that item cannot be purchased with points, please go to the website to buy it.");
									return;
								}
							}else {
								player.sendMessage(ChatColor.RED + "Please specify an item number.");
							}
						}
						event.setCancelled(true);
						return;
					}else if(args[1].equals("confirm")) {
						if(playersbuying.containsKey(event.getPlayer().getName())) {
							ShopItemBuyer buying = playersbuying.get(event.getPlayer().getName());
							//They can only confirm buying of an item after they've actually gone through the purchase part.
							if(buying.getCurrentItemOption() == null) {
								playersbuying.remove(event.getPlayer().getName());
								player.sendMessage(ChatColor.GOLD + "Please wait as we verify your purchase...");
								Thread buythread = new Thread(new SendItemPurchaseToEnjin(plugin, buying, player));
								buythread.start();
							}else {
								player.sendMessage(ChatColor.RED + "You haven't filled out all the options yet! If you want you can cancel your purchase by doing /" + EnjinMinecraftPlugin.BUY_COMMAND + " cancel");
							}
							event.setCancelled(true);
							return;
						}
					}else if(args[1].equals("cancel")) {
						if(playersbuying.containsKey(event.getPlayer().getName())) {
							player.sendMessage(ChatColor.GREEN + "Purchase canceled.");
							playersbuying.remove(event.getPlayer().getName());
							
						}
						event.setCancelled(true);
						return;
					}else if(args[1].equals("skip")) {
						if(playersbuying.containsKey(event.getPlayer().getName())) {
							ShopItemBuyer buyitem = playersbuying.get(event.getPlayer().getName());
							//They can only confirm buying of an item after they've actually gone through the purchase part.
							if(buyitem.getCurrentItemOption() != null) {
								if(buyitem.getCurrentItemOption().isRequired()) {
									player.sendMessage(ChatColor.RED + "This field is required. If you want to cancel your purchase do /" + EnjinMinecraftPlugin.BUY_COMMAND + " cancel");
									event.setCancelled(true);
									return;
								}
								ShopItemOptions nextoption = buyitem.getNextItemOption();
								if(nextoption != null) {
									sendShopItemOptionsForm(event.getPlayer(), nextoption);
								}else {
									buyitem.addPoints(buyitem.getItem().getPoints());
									event.getPlayer().sendMessage(ChatColor.GREEN + "The total purchase price is: " + ChatColor.GOLD + ShopUtils.formatPoints(String.valueOf(buyitem.totalpoints), true));
									event.getPlayer().sendMessage(ChatColor.GREEN.toString() + "If you are sure you want to purchase this item do: /" + EnjinMinecraftPlugin.BUY_COMMAND + " confirm");
								}
							}else {
								player.sendMessage(ChatColor.RED + "Nothing can be skipped! If you want to purchase the item do /" + EnjinMinecraftPlugin.BUY_COMMAND + " confirm");
							}
							player.sendMessage(ChatColor.GREEN + "Purchase canceled.");
						}
						event.setCancelled(true);
						return;
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
										psi.setActiveItem(null);
										sendPlayerShopData(player, psi, newcategory, 0);
									}else {
										//It must be an item, let's send the item details page.
										psi.setActiveItem((ShopItem) category.getItem(optionnumber));
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

	private void sendPlayerInitialBuyData(Player player, ShopItemBuyer buyer) {
		if(buyer.getCurrentItemOption() != null) {
			player.sendMessage(ChatColor.GREEN + "You chose item \"" + buyer.getItem().getName() + "\". Please answer the questions below to complete your purchase.");
			player.sendMessage(ChatColor.GREEN + "You can do " + ChatColor.GOLD + "/" + EnjinMinecraftPlugin.BUY_COMMAND + " cancel " + ChatColor.GREEN + "at any time to cancel.");
			
			ShopItemOptions itemoption = buyer.getCurrentItemOption();
			sendShopItemOptionsForm(player, itemoption);
		}else {
			//There are no options, so let's let them confirm the purchase.
			player.sendMessage(ChatColor.GREEN + "You're about to purchase item \"" + buyer.getItem().getName() + "\" for " + ShopUtils.formatPoints(buyer.getItem().getPoints(), true));
			player.sendMessage(ChatColor.GREEN + "If you are sure do " + ChatColor.GOLD + "/" + EnjinMinecraftPlugin.BUY_COMMAND + " confirm" + ChatColor.GREEN + " to confirm your purchase.");
			player.sendMessage(ChatColor.GREEN + "or " + ChatColor.GOLD + "/" + EnjinMinecraftPlugin.BUY_COMMAND + " cancel " + ChatColor.GREEN + "to cancel.");
			
		}
	}

	private void sendShopItemOptionsForm(Player player, ShopItemOptions itemoption) {
		com.enjin.officialplugin.shop.ShopItemOptions.Type type = itemoption.getType();
		ArrayList<ShopOptionOptions> options;
		switch (type) {
		case AllText:
		case AllTextNoQuotes:
		case Alphabetical:
		case Alphanumeric:
		case Numeric:
		case Undefined:
			player.sendMessage(ChatColor.GOLD + itemoption.getName());
			player.sendMessage(ChatColor.GREEN.toString() + ChatColor.ITALIC + "Just type in the answer in chat");
			break;
		case MultipleCheckboxes:
			player.sendMessage(ChatColor.GOLD + itemoption.getName());
			options = itemoption.getOptions();
			for(int i = 0; i < options.size(); i++) {
				player.sendMessage(ChatColor.GOLD.toString() + (i+1) + ". " + ChatColor.AQUA + options.get(i).getName() + " (" + ShopUtils.formatPoints(options.get(i).getPoints(), true) + ")");
			}
			player.sendMessage(ChatColor.GREEN.toString() + ChatColor.ITALIC + "To select more than 1 option just put in commas between the numbers, like this: 1,4,5");
			break;
		case MultipleChoice:
			player.sendMessage(ChatColor.GOLD + itemoption.getName());
			options = itemoption.getOptions();
			for(int i = 0; i < options.size(); i++) {
				player.sendMessage(ChatColor.GOLD.toString() + (i+1) + ". " + ChatColor.AQUA + options.get(i).getName() + " (" + ShopUtils.formatPoints(options.get(i).getPoints(), true) + ")");
			}
			player.sendMessage(ChatColor.GREEN.toString() + ChatColor.ITALIC + "To select an option just type it's number into chat.");
			break;
		default:
			break;
		}
		if(!itemoption.isRequired()) {
			player.sendMessage("This option is optional, if you would like to skip it just do /" + EnjinMinecraftPlugin.BUY_COMMAND + " skip");
		}
	}

	public void removePlayer(String player) {
		player = player.toLowerCase();
		playersdisabledchat.remove(player);
		activeshops.remove(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(!playersbuying.isEmpty()) {
			if(playersbuying.containsKey(event.getPlayer().getName())) {
				ShopItemBuyer buyitem = playersbuying.get(event.getPlayer().getName());
				if(buyitem.getCurrentItemOption() != null) {
					ShopItemOptions option = buyitem.getCurrentItemOption();
					com.enjin.officialplugin.shop.ShopItemOptions.Type type = option.getType();
					boolean cont = false;
					switch (type) {
					case AllText:
					case Alphabetical:
					case Alphanumeric:
					case AllTextNoQuotes:
						//We don't know what it is, so maybe, just maybe we can get it right?
					case Undefined:
						EnjinMinecraftPlugin.debug("Testing string...");
						if(option.getMinLength() <= event.getMessage().length() && option.getMaxLength() >= event.getMessage().length()) {
							if(ShopUtils.isInputValid(option, event.getMessage())) {
								String formattedtext = "item_variables[" + option.getID() + "]=" + encode(event.getMessage());
								buyitem.addOption(formattedtext);
								cont = true;
								EnjinMinecraftPlugin.debug("String passed!...");
							}else {
								event.getPlayer().sendMessage(ChatColor.RED + "I'm sorry, you added invalid characters, please try again.");
							}
						}else {
							event.getPlayer().sendMessage(ChatColor.RED + "Whoops! That wasn't the correct length! Make sure your text is in between " + option.getMinLength() + " - " + option.getMaxLength() + " characters long");
						}
						break;
					case Numeric:
						EnjinMinecraftPlugin.debug("Testing number...");
						if(ShopUtils.isInputValid(option, event.getMessage())) {
							String formattedtext = "item_variables[" + option.getID() + "]=" + encode(event.getMessage().trim());
							buyitem.addOption(formattedtext);
							cont = true;

							EnjinMinecraftPlugin.debug("Number passed!...");
						}else {
							event.getPlayer().sendMessage(ChatColor.RED + "I'm sorry, that number is outside the allowed value, please try again.");
						}
						break;
					case MultipleCheckboxes:
						if(event.getMessage().length() > 0) {
							String[] split = event.getMessage().split(",");
							StringBuilder tstring = new StringBuilder();
							int points = 0;
							try {
								for(int i = 0; i < split.length; i++) {
									int newnumber = Integer.parseInt(split[i].trim()) -1;
									if(i > 0) {
										tstring.append("&");
									}
									points += ShopUtils.getPointsInt(option.getOptions().get(newnumber).getPoints());
									tstring.append("item_variables[" + option.getID() + "][]=" + encode(option.getOptions().get(newnumber).getValue()));
								}
								buyitem.addOption(tstring.toString());
								buyitem.addPoints(points);
								cont = true;
							}catch(Exception e) {
								event.getPlayer().sendMessage(ChatColor.RED + "I'm sorry, those options are invalid, please try again.");
							}
						}
						break;
					case MultipleChoice:
						if(event.getMessage().length() > 0) {
							try {
								int newnumber = Integer.parseInt(event.getMessage().trim()) -1;
								buyitem.addOption("item_variables[" + option.getID() + "]=" + encode(option.getOptions().get(newnumber).getValue()));
								buyitem.addPoints(option.getOptions().get(newnumber).getPoints());
								cont = true;
							}catch(Exception e) {
								event.getPlayer().sendMessage(ChatColor.RED + "I'm sorry, that option is invalid, please try again.");
							}
						}
						break;
					default:
						break;
					}
					if(cont) {
						ShopItemOptions nextoption = buyitem.getNextItemOption();
						if(nextoption != null) {
							sendShopItemOptionsForm(event.getPlayer(), nextoption);
						}else {
							buyitem.addPoints(buyitem.getItem().getPoints());
							event.getPlayer().sendMessage(ChatColor.GREEN + "The total purchase price is: " + ChatColor.GOLD + ShopUtils.formatPoints(String.valueOf(buyitem.totalpoints), true));
							event.getPlayer().sendMessage(ChatColor.GREEN.toString() + "If you are sure you want to purchase this item do: /" + EnjinMinecraftPlugin.BUY_COMMAND + " confirm");
						}
					}
					event.setCancelled(true);
					return;
				}
			}
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

	private String encode(String in) {
		try {
			return URLEncoder.encode(in, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		//return in;
	}
}
