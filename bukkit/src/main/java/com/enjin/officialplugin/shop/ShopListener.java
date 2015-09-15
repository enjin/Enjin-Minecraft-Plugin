package com.enjin.officialplugin.shop;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.ServerShop.Type;
import com.enjin.officialplugin.threaded.SendItemPurchaseToEnjin;

public class ShopListener implements Listener {

    ConcurrentHashMap<String, PlayerShopsInstance> activeshops = new ConcurrentHashMap<String, PlayerShopsInstance>();
    ConcurrentHashMap<String, String> openshops = new ConcurrentHashMap<String, String>();
    ConcurrentHashMap<String, String> playersdisabledchat = new ConcurrentHashMap<String, String>();
    ConcurrentHashMap<String, ShopItemBuyer> playersbuying = new ConcurrentHashMap<String, ShopItemBuyer>();

    String enjinshopidentifier = ChatColor.BLACK.toString() + ChatColor.RESET;

    EnjinMinecraftPlugin plugin;

    public ShopListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void preCommandListener(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String[] args = event.getMessage().split(" ");
        if (args[0].equalsIgnoreCase("/" + EnjinMinecraftPlugin.config.getBuyCommand())) {
            Player player = event.getPlayer();
            if (args.length > 1 && args[1].equalsIgnoreCase("history")) {
                if (args.length > 2 && player.hasPermission("enjin.history")) {
                    player.sendMessage(ChatColor.RED + "Fetching shop history information for " + args[2] + ", please wait...");
                    Thread dispatchThread = new Thread(new PlayerHistoryGetter(this, player, args[2]));
                    dispatchThread.start();
                    event.setCancelled(true);
                    return;
                } else {
                    player.sendMessage(ChatColor.RED + "Fetching your shop history information, please wait...");
                    Thread dispatchThread = new Thread(new PlayerHistoryGetter(this, player, player.getName()));
                    dispatchThread.start();
                    event.setCancelled(true);
                    return;
                }
            }
            if (activeshops.containsKey(player.getName().toLowerCase())) {
                PlayerShopsInstance psi = activeshops.get(player.getName().toLowerCase());
                //If it's been over 10 minutes, re-retrieve it.
                if (psi.getRetrievalTime() + (1000 * 60 * 10) < System.currentTimeMillis()) {
                    player.sendMessage(ChatColor.RED + "Fetching shop information, please wait...");
                    Thread dispatchThread = new Thread(new PlayerShopGetter(this, player));
                    dispatchThread.start();
                    event.setCancelled(true);
                    return;
                }
                playersdisabledchat.put(player.getName().toLowerCase(), player.getName());
                //If it's just the /buy parameter, let's just reset to the shop topmost category.
                if (args.length == 1) {
                    //If they haven't selected a shop yet, show them the shop selection screen again.
                    if (psi.getActiveShop() == null) {
                        if (EnjinMinecraftPlugin.config.isUseBuyGUI()) {
                            sendPlayerShopChestData(player, psi, psi.getActiveShop(), 0);
                        } else {
                            sendPlayerInitialShopData(player, psi);
                        }
                        //Else, if they have, show them the shop main menu again.
                    } else {
                        ServerShop selectedshop = psi.getActiveShop();
                        //We need to see if it only has one category. If so, open that category.
                        if (selectedshop.getType() == Type.Category && selectedshop.getItems().size() == 1) {
                            ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
                            psi.setActiveCategory(category);
                            psi.setActiveItem(null);
                            //If it has items or more than one category show the shop main page.
                        } else {
                            psi.setActiveCategory(selectedshop);
                            psi.setActiveItem(null);
                        }
                        if (EnjinMinecraftPlugin.config.isUseBuyGUI()) {
                            sendPlayerShopChestData(player, psi, psi.getActiveCategory(), 0);
                        } else {
                            sendPlayerShopData(player, psi, psi.getActiveCategory(), 0);
                        }
                    }
                } else {
                    if (args[1].equalsIgnoreCase("shop")) {
                        if (args.length > 2) {
                            try {
                                //We need to take off 1 of the shop number because internally we start with 0, not 1.
                                int pagenumber = Integer.parseInt(args[2].trim()) - 1;
                                if (pagenumber < psi.getServerShopCount() && pagenumber >= 0) {
                                    ServerShop selectedshop = psi.getServerShop(pagenumber);
                                    //We need to see if it only has one category. If so, open that category.
                                    if (selectedshop.getType() == Type.Category && selectedshop.getItems().size() == 1) {
                                        ShopItemAdder category = (ShopItemAdder) selectedshop.getItem(0);
                                        psi.setActiveShop(selectedshop);
                                        psi.setActiveCategory(category);
                                        psi.setActiveItem(null);
                                        sendPlayerShopData(player, psi, category, 0);
                                        //If it has items or more than one category show the shop main page.
                                    } else {
                                        psi.setActiveShop(selectedshop);
                                        psi.setActiveCategory(selectedshop);
                                        psi.setActiveItem(null);
                                        sendPlayerShopData(player, psi, selectedshop, 0);
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Invalid page number.");
                                }
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Invalid page number.");
                            }
                        } else {
                            //If they didn't specify a shop, let's show the initial shop data again.
                            psi.setActiveCategory(null);
                            psi.setActiveShop(null);
                            psi.setActiveItem(null);
                            sendPlayerInitialShopData(player, psi);
                        }
                    } else if (args[1].equals("page")) {
                        if (args.length > 2) {
                            if (psi.getActiveCategory() != null) {
                                psi.setActiveItem(null);
                                ShopItemAdder category = psi.getActiveCategory();
                                ArrayList<ArrayList<String>> pages;
                                //This should never be null, but just in case.
                                if (category.getPages() == null) {
                                    category.setPages(ShopUtils.formatPages(psi.getActiveShop(), category));
                                }
                                pages = category.getPages();
                                try {
                                    int pagenumber = Integer.parseInt(args[2]) - 1;
                                    if (pagenumber < pages.size() && pagenumber >= 0) {
                                        sendPlayerPage(player, pages.get(pagenumber));
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Invalid page number.");
                                    }
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ChatColor.RED + "Invalid page number.");
                                }
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify a page number.");
                        }
                    } else if (args[1].equals("item")) {
                        if (args.length > 2) {
                            if (psi.getActiveShop() == null) {
                                player.sendMessage(ChatColor.RED + "You need to select a shop first! Do /" + EnjinMinecraftPlugin.config.getBuyCommand() + " to see the shops list.");
                            } else {
                                try {
                                    ShopItemAdder category = psi.getActiveCategory();
                                    int optionnumber = Integer.parseInt(args[2]) - 1;
                                    if (optionnumber < category.getItems().size() && optionnumber >= 0) {
                                        //If it's a category, let's go into the category and list the first page.
                                        if (category.getType() == Type.Category) {
                                            player.sendMessage(ChatColor.RED + "You need to select a category first!");
                                        } else {
                                            ShopItem item = (ShopItem) category.getItem(optionnumber);
                                            //Make sure you can purchase the item with points.
                                            if (item.points != "") {
                                                ShopItemBuyer buyer = new ShopItemBuyer(item);
                                                playersbuying.put(player.getName(), buyer);
                                                sendPlayerInitialBuyData(player, buyer);
                                            } else {
                                                //Show the player the message to go to the website to purchase.
                                                player.sendMessage(ChatColor.RED + "Sorry, that item cannot be purchased with points, please go to the website to buy it.");
                                            }
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Invalid item number.");
                                    }
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ChatColor.RED + "Invalid item number.");
                                }
                            }
                        } else {
                            if (psi.getActiveItem() != null) {
                                ShopItem item = psi.getActiveItem();
                                if (item.points != "") {
                                    ShopItemBuyer buyer = new ShopItemBuyer(item);
                                    playersbuying.put(player.getName(), buyer);
                                    sendPlayerInitialBuyData(player, buyer);
                                    return;
                                } else {
                                    //Show the player the message to go to the website to purchase.
                                    player.sendMessage(ChatColor.RED + "Sorry, that item cannot be purchased with points, please go to the website to buy it.");
                                    return;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Please specify an item number.");
                            }
                        }
                        event.setCancelled(true);
                        return;
                    } else if (args[1].equals("confirm")) {
                        if (playersbuying.containsKey(event.getPlayer().getName())) {
                            ShopItemBuyer buying = playersbuying.get(event.getPlayer().getName());
                            //They can only confirm buying of an item after they've actually gone through the purchase part.
                            if (buying.getCurrentItemOption() == null) {
                                playersbuying.remove(event.getPlayer().getName());
                                player.sendMessage(ChatColor.GOLD + "Please wait as we verify your purchase...");
                                Thread buythread = new Thread(new SendItemPurchaseToEnjin(plugin, buying, player));
                                buythread.start();
                            } else {
                                player.sendMessage(ChatColor.RED + "You haven't filled out all the options yet! If you want you can cancel your purchase by doing /" + EnjinMinecraftPlugin.config.getBuyCommand() + " cancel");
                            }
                            event.setCancelled(true);
                            return;
                        }
                    } else if (args[1].equals("cancel")) {
                        if (playersbuying.containsKey(event.getPlayer().getName())) {
                            player.sendMessage(ChatColor.GREEN + "Purchase canceled.");
                            playersbuying.remove(event.getPlayer().getName());

                        }
                        event.setCancelled(true);
                        return;
                    } else if (args[1].equals("skip")) {
                        if (playersbuying.containsKey(event.getPlayer().getName())) {
                            ShopItemBuyer buyitem = playersbuying.get(event.getPlayer().getName());
                            //They can only confirm buying of an item after they've actually gone through the purchase part.
                            if (buyitem.getCurrentItemOption() != null) {
                                if (buyitem.getCurrentItemOption().isRequired()) {
                                    player.sendMessage(ChatColor.RED + "This field is required. If you want to cancel your purchase do /" + EnjinMinecraftPlugin.config.getBuyCommand() + " cancel");
                                    event.setCancelled(true);
                                    return;
                                }
                                ShopItemOptions nextoption = buyitem.getNextItemOption();
                                if (nextoption != null) {
                                    sendShopItemOptionsForm(event.getPlayer(), nextoption);
                                } else {
                                    buyitem.addPoints(buyitem.getItem().getPoints());
                                    event.getPlayer().sendMessage(ChatColor.GREEN + "The total purchase price is: " + ChatColor.GOLD + ShopUtils.formatPoints(String.valueOf(buyitem.totalpoints), true));
                                    event.getPlayer().sendMessage(ChatColor.GREEN.toString() + "If you are sure you want to purchase this item do: /" + EnjinMinecraftPlugin.config.getBuyCommand() + " confirm");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Nothing can be skipped! If you want to purchase the item do /" + EnjinMinecraftPlugin.config.getBuyCommand() + " confirm");
                            }
                            player.sendMessage(ChatColor.GREEN + "Purchase canceled.");
                        }
                        event.setCancelled(true);
                        return;
                    } else if (args.length > 1) {
                        if (psi.getActiveShop() == null) {
                            player.sendMessage(ChatColor.RED + "You need to select a shop first! Do /" + EnjinMinecraftPlugin.config.getBuyCommand() + " to see the shops list.");
                        } else {
                            try {
                                ShopItemAdder category = psi.getActiveCategory();
                                int optionnumber = Integer.parseInt(args[1]) - 1;
                                if (optionnumber < category.getItems().size() && optionnumber >= 0) {
                                    //If it's a category, let's go into the category and list the first page.
                                    if (category.getType() == Type.Category) {
                                        ShopItemAdder newcategory = (ShopItemAdder) category.getItem(optionnumber);
                                        psi.setActiveCategory(newcategory);
                                        psi.setActiveItem(null);
                                        sendPlayerShopData(player, psi, newcategory, 0);
                                    } else {
                                        //It must be an item, let's send the item details page.
                                        psi.setActiveItem((ShopItem) category.getItem(optionnumber));
                                        sendPlayerPage(player, ShopUtils.getItemDetailsPage(psi.getActiveShop(), (ShopItem) category.getItem(optionnumber), player));
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Invalid page number.");
                                }
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Invalid page number.");
                            }
                        }
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "Fetching shop information, please wait...");
                Thread dispatchThread = new Thread(new PlayerShopGetter(this, player));
                dispatchThread.start();
            }
            event.setCancelled(true);
        } else if (args[0].equalsIgnoreCase("/ec")) {
            if (playersdisabledchat.containsKey(event.getPlayer().getName().toLowerCase())) {
                playersdisabledchat.remove(event.getPlayer().getName().toLowerCase());
                event.getPlayer().sendMessage(ChatColor.GREEN + "Your chat is now enabled.");
                event.setCancelled(true);
            }
        }
    }

    private void sendPlayerInitialBuyData(Player player, ShopItemBuyer buyer) {
        if (buyer.getCurrentItemOption() != null) {
            player.sendMessage(ChatColor.GREEN + "You chose item \"" + buyer.getItem().getName() + "\". Please answer the questions below to complete your purchase.");
            player.sendMessage(ChatColor.GREEN + "You can do " + ChatColor.GOLD + "/" + EnjinMinecraftPlugin.config.getBuyCommand() + " cancel " + ChatColor.GREEN + "at any time to cancel.");

            ShopItemOptions itemoption = buyer.getCurrentItemOption();
            sendShopItemOptionsForm(player, itemoption);
        } else {
            //There are no options, so let's let them confirm the purchase.
            player.sendMessage(ChatColor.GREEN + "You're about to purchase item \"" + buyer.getItem().getName() + "\" for " + ShopUtils.formatPoints(buyer.getItem().getPoints(), true));
            player.sendMessage(ChatColor.GREEN + "If you are sure do " + ChatColor.GOLD + "/" + EnjinMinecraftPlugin.config.getBuyCommand() + " confirm" + ChatColor.GREEN + " to confirm your purchase.");
            player.sendMessage(ChatColor.GREEN + "or " + ChatColor.GOLD + "/" + EnjinMinecraftPlugin.config.getBuyCommand() + " cancel " + ChatColor.GREEN + "to cancel.");

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
                for (int i = 0; i < options.size(); i++) {
                    player.sendMessage(ChatColor.GOLD.toString() + (i + 1) + ". " + ChatColor.AQUA + options.get(i).getName() + " (" + ShopUtils.formatPoints(options.get(i).getPoints(), true) + ")");
                }
                player.sendMessage(ChatColor.GREEN.toString() + ChatColor.ITALIC + "To select more than 1 option just put in commas between the numbers, like this: 1,4,5");
                break;
            case MultipleChoice:
                player.sendMessage(ChatColor.GOLD + itemoption.getName());
                options = itemoption.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    player.sendMessage(ChatColor.GOLD.toString() + (i + 1) + ". " + ChatColor.AQUA + options.get(i).getName() + " (" + ShopUtils.formatPoints(options.get(i).getPoints(), true) + ")");
                }
                player.sendMessage(ChatColor.GREEN.toString() + ChatColor.ITALIC + "To select an option just type it's number into chat.");
                break;
            default:
                break;
        }
        if (!itemoption.isRequired()) {
            player.sendMessage("This option is optional, if you would like to skip it just do /" + EnjinMinecraftPlugin.config.getBuyCommand() + " skip");
        }
    }

    public void removePlayer(String player) {
        player = player.toLowerCase();
        playersdisabledchat.remove(player);
        activeshops.remove(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!playersbuying.isEmpty()) {
            if (playersbuying.containsKey(event.getPlayer().getName())) {
                ShopItemBuyer buyitem = playersbuying.get(event.getPlayer().getName());
                if (buyitem.getCurrentItemOption() != null) {
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
                            if (option.getMaxLength() == 0) {
                                option.setMaxLength(Integer.MAX_VALUE);
                            }
                            if (option.getMinLength() <= event.getMessage().length() && option.getMaxLength() >= event.getMessage().length()) {
                                if (ShopUtils.isInputValid(option, event.getMessage())) {
                                    String formattedtext = "item_variables[" + option.getID() + "]=" + encode(event.getMessage());
                                    buyitem.addOption(formattedtext);
                                    cont = true;
                                    EnjinMinecraftPlugin.debug("String passed!...");
                                } else {
                                    event.getPlayer().sendMessage(ChatColor.RED + "I'm sorry, you added invalid characters, please try again.");
                                }
                            } else {
                                event.getPlayer().sendMessage(ChatColor.RED + "Whoops! That wasn't the correct length! Make sure your text is in between " + option.getMinLength() + " - " + option.getMaxLength() + " characters long");
                            }
                            break;
                        case Numeric:
                            EnjinMinecraftPlugin.debug("Testing number...");
                            if (ShopUtils.isInputValid(option, event.getMessage())) {
                                String formattedtext = "item_variables[" + option.getID() + "]=" + encode(event.getMessage().trim());
                                buyitem.addOption(formattedtext);
                                cont = true;

                                EnjinMinecraftPlugin.debug("Number passed!...");
                            } else {
                                event.getPlayer().sendMessage(ChatColor.RED + "I'm sorry, that number is outside the allowed value, please try again.");
                            }
                            break;
                        case MultipleCheckboxes:
                            if (event.getMessage().length() > 0) {
                                String[] split = event.getMessage().split(",");
                                StringBuilder tstring = new StringBuilder();
                                int points = 0;
                                try {
                                    for (int i = 0; i < split.length; i++) {
                                        int newnumber = Integer.parseInt(split[i].trim()) - 1;
                                        if (i > 0) {
                                            tstring.append("&");
                                        }
                                        points += ShopUtils.getPointsInt(option.getOptions().get(newnumber).getPoints());
                                        tstring.append("item_variables[" + option.getID() + "][]=" + encode(option.getOptions().get(newnumber).getValue()));
                                    }
                                    buyitem.addOption(tstring.toString());
                                    buyitem.addPoints(points);
                                    cont = true;
                                } catch (Exception e) {
                                    event.getPlayer().sendMessage(ChatColor.RED + "I'm sorry, those options are invalid, please try again.");
                                }
                            }
                            break;
                        case MultipleChoice:
                            if (event.getMessage().length() > 0) {
                                try {
                                    int newnumber = Integer.parseInt(event.getMessage().trim()) - 1;
                                    buyitem.addOption("item_variables[" + option.getID() + "]=" + encode(option.getOptions().get(newnumber).getValue()));
                                    buyitem.addPoints(option.getOptions().get(newnumber).getPoints());
                                    cont = true;
                                } catch (Exception e) {
                                    event.getPlayer().sendMessage(ChatColor.RED + "I'm sorry, that option is invalid, please try again.");
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    if (cont) {
                        ShopItemOptions nextoption = buyitem.getNextItemOption();
                        if (nextoption != null) {
                            sendShopItemOptionsForm(event.getPlayer(), nextoption);
                        } else {
                            buyitem.addPoints(buyitem.getItem().getPoints());
                            event.getPlayer().sendMessage(ChatColor.GREEN + "The total purchase price is: " + ChatColor.GOLD + ShopUtils.formatPoints(String.valueOf(buyitem.totalpoints), true));
                            event.getPlayer().sendMessage(ChatColor.GREEN.toString() + "If you are sure you want to purchase this item do: /" + EnjinMinecraftPlugin.config.getBuyCommand() + " confirm");
                        }
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        }
        //We don't need to do anything if our list is empty.
        if (!playersdisabledchat.isEmpty()) {
            //If a player in the list chats, remove them from the list, otherwise, don't send him messages.
            if (playersdisabledchat.containsKey(event.getPlayer().getName().toLowerCase())) {
                playersdisabledchat.remove(event.getPlayer().getName().toLowerCase());
            } else {
                Set<Player> recipients = event.getRecipients();
                //Make sure we aren't throwing huge errors on spigot!
                try {
                    for (Player recipient : recipients) {
                        if (playersdisabledchat.containsKey(recipient.getName().toLowerCase())) {
                            recipients.remove(recipient);
                        }
                    }
                } catch (Exception e) {
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
        openshops.remove(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        //If a player quits, let's reset the shop data and remove them from the list.
        String player = event.getPlayer().getName().toLowerCase();
        if (plugin.config.isUseBuyGUI() && activeshops.containsKey(player)) {
            removeEnjinItems(event.getPlayer());
            activeshops.remove(player);
            openshops.remove(player);
            event.getPlayer().updateInventory();

        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInventoryInteract(InventoryClickEvent event) {
        //If a player quits, let's reset the shop data and remove them from the list.
        Player player = (Player) event.getWhoClicked();
        if (openshops.containsKey(player.getName().toLowerCase())) {
            if (!event.getView().getTitle().startsWith(enjinshopidentifier)) {
                removeEnjinItems(player);
                activeshops.remove(player.getName().toLowerCase());
                openshops.remove(player.getName().toLowerCase());
                return;
            } else {
                if (!activeshops.containsKey(player.getName().toLowerCase())) {
                    event.setCancelled(true);
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "Shop timed out, please re-open the shop to continue browsing.");
                    return;
                }
            }
        }
        if (activeshops.containsKey(player.getName().toLowerCase())) {
            if (!event.getView().getTitle().startsWith(enjinshopidentifier)) {
                activeshops.remove(player.getName().toLowerCase());
                openshops.remove(player.getName().toLowerCase());
                return;
            }
            event.setCancelled(true);
            PlayerShopsInstance psi = activeshops.get(player.getName().toLowerCase());
            ItemStack item = event.getCurrentItem();
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String itemname = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                if (itemname.equalsIgnoreCase("Next Page") || itemname.equalsIgnoreCase("Previous Page")) {
                    String page = item.getItemMeta().getLore().get(0);
                    int ipage = Integer.parseInt(page.split(" ")[1]) - 1;
                    sendPlayerShopChestData(player, psi, psi.getActiveCategory(), ipage);
                } else if (itemname.equalsIgnoreCase("Back")) {
                    if (psi.getActiveItem() != null) {
                        psi.setActiveItem(null);
                        sendPlayerShopChestData(player, psi, psi.getActiveCategory(), 0);
                    } else if (psi.getActiveCategory() != null) {
                        if (psi.getActiveCategory().getParentCategory() == null) {
                            psi.setActiveCategory(null);
                            psi.setActiveShop(null);
                            sendPlayerShopChestData(player, psi, psi.getActiveCategory(), 0);
                        } else {
                            psi.setActiveCategory(psi.getActiveCategory().getParentCategory());
                            sendPlayerShopChestData(player, psi, psi.getActiveCategory(), 0);
                        }
                    }
                } else if (itemname.equalsIgnoreCase("Buy with Points")) {
                    if (psi.getActiveItem() != null) {
                        ShopItem sitem = psi.getActiveItem();
                        if (sitem.points != "") {
                            if (sitem.getOptions().size() > 0) {
                                ShopItemBuyer buyer = new ShopItemBuyer(sitem);
                                playersbuying.put(player.getName(), buyer);
                                sendPlayerInitialBuyData(player, buyer);
                                player.closeInventory();
                                return;
                            } else {
                                player.sendMessage(ChatColor.GOLD + "Please wait as we verify your purchase...");
                                ShopItemBuyer buyer = new ShopItemBuyer(sitem);
                                Thread buythread = new Thread(new SendItemPurchaseToEnjin(plugin, buyer, player));
                                buythread.start();
                                player.closeInventory();
                                return;
                            }
                        }
                    }
                } else if (itemname.equalsIgnoreCase("Buy with Money")) {
                    if (psi.getActiveItem() != null) {
                        ShopItem sitem = psi.getActiveItem();
                        if (sitem.getPrice() != "") {
                            player.sendMessage("--------------------------------------------");
                            player.sendMessage(ShopUtils.FORMATTING_CODE + psi.getActiveShop().getColorname() + sitem.getName());
                            player.sendMessage(ShopUtils.FORMATTING_CODE + psi.getActiveShop().getColortext() + "Click the following link to checkout:");
                            player.sendMessage(ShopUtils.FORMATTING_CODE + psi.getActiveShop().getColorurl() + psi.getActiveShop().getBuyurl() + sitem.getId() + "?player=" + player.getName());
                            player.sendMessage("--------------------------------------------");
                            player.closeInventory();
                            return;
                        }
                    }
                } else {
                    int period = itemname.indexOf(".");
                    if (period == -1) {
                        if (psi.getActiveItem() != null) {
                            sendPlayerShopChestData(player, psi, psi.getActiveItem(), 0);
                        }
                        return;
                    }
                    int selection = 0;
                    try {
                        selection = Integer.parseInt(itemname.substring(0, period)) - 1;
                    } catch (NumberFormatException e) {
                        if (psi.getActiveItem() != null) {
                            sendPlayerShopChestData(player, psi, psi.getActiveItem(), 0);
                        }
                        return;
                    }
                    if (psi.getActiveShop() == null) {
                        if (selection < psi.getServerShopCount()) {
                            psi.setActiveShop(selection);
                            psi.setActiveCategory(psi.getActiveShop());
                            sendPlayerShopChestData(player, psi, psi.getActiveShop(), 0);
                        }
                    } else {
                        if (selection < psi.getActiveCategory().getItems().size()) {
                            AbstractShopSuperclass sitem = psi.getActiveCategory().getItem(selection);
                            if (sitem instanceof ShopCategory) {
                                ShopCategory scat = (ShopCategory) sitem;
                                psi.setActiveCategory(scat);
                                sendPlayerShopChestData(player, psi, scat, 0);
                            } else if (sitem instanceof ShopItem) {
                                psi.setActiveItem((ShopItem) sitem);
                                sendPlayerShopChestData(player, psi, (ShopItem) sitem, 0);
                            }
                        }
                    }
                }
            } else {
                player.updateInventory();
            }
        }
    }

    protected void sendPlayerShopChestData(Player player, PlayerShopsInstance shops, ShopItem item, int page) {
        String windowtitle = item.getName();
        if (windowtitle.length() > 26) {
            windowtitle = windowtitle.substring(0, 26);
        }
        Inventory inv = Bukkit.getServer().createInventory(null, 9, enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortitle() + windowtitle);
        ItemStack back = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName(enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext() + "Back");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorinfo() + "Go back");
        meta.setLore(lore);
        back.setItemMeta(meta);
        inv.setItem(0, back);

        ItemStack is = new ItemStack(item.getMaterial(), 1, item.getMaterialDamage());
        meta = is.getItemMeta();
        String name = item.getName();
        lore = new ArrayList<String>();
        String[] l = ShopUtils.WrapText(((ShopItem) item).getInfo(), shops.getActiveShop().getColorinfo(), 8, true);
        for (String ls : l) {
            lore.add(ls);
        }
        meta.setDisplayName(enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorname() + name);
        meta.setLore(lore);
        is.setItemMeta(meta);
        inv.setItem(4, is);


        String formattedpoints = ShopUtils.formatPoints(item.getPoints(), false);
        if (!formattedpoints.equals("")) {

            ItemStack points = new ItemStack(Material.EMERALD, 1);
            meta = points.getItemMeta();
            meta.setDisplayName(enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext() + "Buy with Points");
            lore = new ArrayList<String>();

            lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext()
                    + "Points: " + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorprice() + formattedpoints);
            meta.setLore(lore);
            points.setItemMeta(meta);
            inv.setItem(7, points);
        }
        String formattedprice = ShopUtils.formatPrice(item.getPrice(), shops.getActiveShop().getCurrency());
        if (!formattedprice.equals("")) {
            ItemStack money = new ItemStack(Material.DIAMOND, 1);
            meta = money.getItemMeta();
            meta.setDisplayName(enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext() + "Buy with Money");
            lore = new ArrayList<String>();

            lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext()
                    + "Price: " + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorprice() + formattedprice);
            meta.setLore(lore);
            money.setItemMeta(meta);
            inv.setItem(8, money);
        }

        player.openInventory(inv);

    }

	/*
    @EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerCloseInventory(InventoryCloseEvent event) {
		//If a player closes the virtual chest, let's reset the shop data and remove them from the list.
		String player = event.getPlayer().getName().toLowerCase();
		playersdisabledchat.remove(player);
		activeshops.remove(player);
	}*/

    public void sendPlayerInitialShopData(Player player, PlayerShopsInstance shops) {
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

    public static void sendPlayerPage(Player player, ArrayList<String> page) {
        for (String line : page) {
            player.sendMessage(line);
        }
    }

    protected void sendPlayerShopChestData(Player player, PlayerShopsInstance shops, ShopItemAdder category, int page) {
        int slot = 0;
        if (shops.getActiveShop() == null) {
            Inventory inv = Bukkit.getServer().createInventory(null, 6 * 9, enjinshopidentifier + ChatColor.GOLD + "Select a Shop");
            for (int i = 0; i < shops.getServerShopCount(); i++) {
                ServerShop item = shops.getServerShop(i);
                ItemStack is = new ItemStack(item.getMaterial(), 1, item.getMaterialDamage());
                ItemMeta meta = is.getItemMeta();
                String name = item.getName();
                meta.setDisplayName(enjinshopidentifier + ChatColor.GOLD.toString() + (i + 1) + ". " + name);
                is.setItemMeta(meta);
                inv.setItem(slot + i, is);
            }
            player.openInventory(inv);
            return;
        }
        String windowtitle = category.getName();
        if (windowtitle.length() > 26) {
            windowtitle = windowtitle.substring(0, 26);
        }
        Inventory inv = Bukkit.getServer().createInventory(null, 6 * 9, enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortitle() + windowtitle);
        if (category.getParentCategory() != null || shops.getServerShopCount() > 1) {
            ItemStack back = new ItemStack(Material.ARROW, 1);
            ItemMeta meta = back.getItemMeta();
            meta.setDisplayName(enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext() + "Back");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorinfo() + "Go back");
            meta.setLore(lore);
            back.setItemMeta(meta);
            inv.setItem(0, back);
            slot = 9;
        }
        if (slot == 9 && category.getItems().size() > 5 * 9) {
            int maxpage = category.getItems().size() / (5 * 9);
            if (category.getItems().size() % (5 * 9) > 0) {
                maxpage++;
            }
            if (page >= maxpage) {
                page = maxpage - 1;
            }
            if (page > 0 && category.getItems().size() > (5 * 9) * (page)) {
                ItemStack previous = new ItemStack(Material.DRAGON_EGG, 1);
                ItemMeta meta = previous.getItemMeta();
                meta.setDisplayName(enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext() + "Previous Page");
                ArrayList<String> lore = new ArrayList<String>();
                lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorinfo() + "Page: " + page);
                lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorinfo() + "You are on page: " + (page + 1));
                meta.setLore(lore);
                previous.setItemMeta(meta);
                inv.setItem(7, previous);
                slot = 9;
            }
            if (page < maxpage - 1) {
                ItemStack next = new ItemStack(Material.HOPPER, 1);
                ItemMeta meta = next.getItemMeta();
                meta.setDisplayName(enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext() + "Next Page");
                ArrayList<String> lore = new ArrayList<String>();
                lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorinfo() + "Page: " + (page + 2));
                lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorinfo() + "You are on page: " + (page + 1));
                meta.setLore(lore);
                next.setItemMeta(meta);
                inv.setItem(8, next);
                slot = 9;
            }
        } else if (slot == 0 && category.getItems().size() > 6 * 9) {
            int maxpage = category.getItems().size() / (5 * 9);
            if (category.getItems().size() % (5 * 9) > 0) {
                maxpage++;
            }
            if (page >= maxpage) {
                page = maxpage - 1;
            }
            if (maxpage > 1) {
                if (page > 0 && category.getItems().size() > (5 * 9) * (page)) {
                    ItemStack previous = new ItemStack(Material.DRAGON_EGG, 1);
                    ItemMeta meta = previous.getItemMeta();
                    meta.setDisplayName(enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext() + "Previous Page");
                    ArrayList<String> lore = new ArrayList<String>();
                    lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorinfo() + "Page: " + page);
                    lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorinfo() + "You are on page: " + (page + 1));
                    meta.setLore(lore);
                    previous.setItemMeta(meta);
                    inv.setItem(7, previous);
                    slot = 9;
                }
                if (page < maxpage - 1) {
                    ItemStack next = new ItemStack(Material.HOPPER, 1);
                    ItemMeta meta = next.getItemMeta();
                    meta.setDisplayName(enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext() + "Next Page");
                    ArrayList<String> lore = new ArrayList<String>();
                    lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorinfo() + "Page: " + (page + 2));
                    lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorinfo() + "You are on page: " + (page + 1));
                    meta.setLore(lore);
                    next.setItemMeta(meta);
                    inv.setItem(8, next);
                    slot = 9;
                }
            }
        }
        if (page > 0 && slot == 0) {
            page = 0;
        }
        int multiplier = 6 * 9;
        if (slot == 9) {
            multiplier = 5 * 9;
        }
        for (int i = 0; (multiplier * page) + i < category.getItems().size() && i < multiplier; i++) {
            AbstractShopSuperclass item = category.getItem(multiplier * page + i);
            short damage = item.getMaterialDamage();
            Material mat = item.getMaterial();
            if (mat == null) {
                mat = Material.CHEST;
            }
            ItemStack is = new ItemStack(mat, 1, damage);
            ItemMeta meta = is.getItemMeta();
            String name = "";
            ArrayList<String> lore = new ArrayList<String>();

            if (item instanceof ShopItem) {
                name = ((ShopItem) item).getName();
                String[] l = ShopUtils.WrapText(((ShopItem) item).getInfo(), shops.getActiveShop().getColorinfo(), 8, true);

                String formattedprice = ShopUtils.formatPrice(((ShopItem) item).getPrice(), shops.getActiveShop().getCurrency());
                if (!formattedprice.equals("")) {
                    lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext()
                            + "Price: " + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorprice() + formattedprice);
                }
                String formattedpoints = ShopUtils.formatPoints(((ShopItem) item).getPoints(), false);
                if (!formattedpoints.equals("")) {
                    lore.add(ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColortext()
                            + "Points: " + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorprice() + formattedpoints);
                }
                for (String ls : l) {
                    lore.add(ls);
                }
            } else if (item instanceof ShopCategory) {
                name = ((ShopCategory) item).getName();
                String[] l = ShopUtils.WrapText(((ShopCategory) item).getInfo(), shops.getActiveShop().getColorinfo(), 8, true);
                for (String ls : l) {
                    lore.add(ls);
                }
            }

            meta.setDisplayName(enjinshopidentifier + ShopUtils.FORMATTING_CODE + shops.getActiveShop().getColorname() + (multiplier * page + i + 1) + ". " + name);
            meta.setLore(lore);
            is.setItemMeta(meta);
            inv.setItem(slot + i, is);
        }
        player.openInventory(inv);
    }

    public void sendPlayerShopData(Player player, PlayerShopsInstance shops, ShopItemAdder category, int page) {
        ArrayList<ArrayList<String>> pages;
        if (category.getPages() == null) {
            pages = ShopUtils.formatPages(shops.getActiveShop(), category);
            category.setPages(pages);
        } else {
            pages = category.getPages();
        }
        sendPlayerPage(player, pages.get(page));
    }

    public void sendPlayerItemData(Player player, PlayerShopsInstance shops, ShopItem item) {
        sendPlayerPage(player, ShopUtils.getItemDetailsPage(shops.getActiveShop(), item, player));
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

    private void removeEnjinItems(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] inventory = inv.getContents();
        boolean inventorychanged = false;
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null && inventory[i].getItemMeta() != null && inventory[i].getItemMeta().getDisplayName() != null && inventory[i].getItemMeta().getDisplayName().startsWith(enjinshopidentifier)) {
                inv.setItem(i, null);
                inventorychanged = true;
            }
        }
        inventory = inv.getArmorContents();
        boolean armorchanged = false;
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null && inventory[i].getItemMeta() != null && inventory[i].getItemMeta().getDisplayName() != null && inventory[i].getItemMeta().getDisplayName().startsWith(enjinshopidentifier)) {
                inventory[i] = null;
                armorchanged = true;
            }
        }
        if (armorchanged) {
            inv.setArmorContents(inventory);
        }
        if (armorchanged || inventorychanged) {
            plugin.getLogger().warning(player.getName() + " just tried glitching an item out of the Enjin in game shop. Item removed successfully from player's inventory.");
            player.updateInventory();
        }
    }
}
