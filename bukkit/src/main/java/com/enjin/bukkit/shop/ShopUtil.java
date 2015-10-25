package com.enjin.bukkit.shop;

import com.enjin.bukkit.util.TextUtils;
import com.enjin.core.Enjin;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.PlayerShopInstance;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

public class ShopUtil {
    private static DecimalFormat priceFormat = new DecimalFormat("#.00");

    public static void sendTextShop(Player player, PlayerShopInstance instance, int page) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.instance;

        if (instance.getActiveShop() == null) {
            plugin.debug("Sending a list of shops to " + player.getName());
            sendAvailableShops(player, instance);
        } else {
            if (instance.getActiveCategory() == null) {
                plugin.debug("Sending a list of categories to " + player.getName());
                sendAvailableCategories(player, instance, page < 1 ? 1 : page);
            } else {
                Category category = instance.getActiveCategory();
                if (category.getCategories() != null && !category.getCategories().isEmpty()) {
                    plugin.debug("Sending " + category.getCategories().size() + " sub-categories to " + player.getName());
                    sendAvailableCategories(player, instance, page < 1 ? 1 : page);
                } else {
                    plugin.debug("Sending a list of items to " + player.getName());
                    sendAvailableItems(player, instance, page);
                }
            }
        }
    }

    private static void sendAvailableShops(Player player, PlayerShopInstance instance) {
        List<Shop> available = instance.getShops();

        FancyMessage message = new FancyMessage("=== Choose Shop ===\n")
                .then("Please type ")
                .then("/buy shop <#>\n\n")
                .color(ChatColor.YELLOW);

        int index = 1;
        Iterator<Shop> iterator = available.iterator();
        while (iterator.hasNext()) {
            Shop shop = iterator.next();
            message.then(index++ + ". " + shop.getName())
                    .color(ChatColor.YELLOW);
            if (iterator.hasNext()) {
                message.then("\n");
            }
        }

        message.send(player);
    }

    private static void sendAvailableCategories(Player player, PlayerShopInstance instance, int page) {
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            buildHeader(shop.getName(), shop).send(player);
            buildShopInfo(shop, false).send(player);
            buildCategoryListContent(shop, shop.getCategories(), page).send(player);
            buildFooterInfo(shop).send(player);
            buildFooter("Type /buy page #", shop, page < 1 ? 1 : page).send(player);
        } else {
            Category category = instance.getActiveCategory();

            buildHeader(category.getName(), shop).send(player);
            buildShopInfo(shop, false).send(player);
            buildCategoryListContent(shop, category.getCategories(), page).send(player);
            buildFooterInfo(shop).send(player);
            buildFooter("Type /buy page #", shop, page < 1 ? 1 : page).send(player);
        }
    }

    private static void sendAvailableItems(Player player, PlayerShopInstance instance, int page) {
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            player.sendMessage(ChatColor.RED.toString() + "You must select a category before you can view the item list");
        } else {
            Category category = instance.getActiveCategory();

            buildHeader(category.getName(), shop).send(player);
            buildShopInfo(shop, true).send(player);
            buildItemListContent(player, shop, category.getItems(), page).send(player);
            buildFooterInfo(shop).send(player);
            buildFooter("Type /buy page #", shop, page < 1 ? 1 : page).send(player);
        }
    }

    public static void sendItemInfo(Player player, PlayerShopInstance instance, int index) {
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            player.sendMessage("You must select a category before you can view the item list");
        } else {
            Category category = instance.getActiveCategory();
            Item item = index < 0 ? category.getItems().get(0) : (index < category.getItems().size() ? category.getItems().get(index) : category.getItems().get(category.getItems().size() - 1));

            buildHeader(item.getName(), shop).send(player);
            buildItemContent(player, shop, item).send(player);
            buildFooterInfo(shop).send(player);
            buildFooter("", shop, -1).send(player);
        }
    }

    private static FancyMessage buildHeader(String title, Shop shop) {
        StringBuilder header = new StringBuilder();
        String prefix = shop.getBorderC();

        for (int i = 0; i < 3; i++) {
            prefix += shop.getBorderH();
        }

        if (prefix.length() > 4) {
            prefix = prefix.substring(0, 4);
        }

        header.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(prefix + " ")
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorTitle()))
                .append(title + " ")
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()));

        for (int i = 0; i < 40; i++) {
            header.append(shop.getBorderH());
        }

        return new FancyMessage(TextUtils.trim(header.toString(), null));
    }

    private static FancyMessage buildShopInfo(Shop shop, boolean items) {
        StringBuilder builder = new StringBuilder()
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(shop.getBorderV())
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorText()))
                .append(" " + shop.getInfo().trim() + "\n")
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(shop.getBorderV())
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorText()))
                .append(" " + "Prices are in " + shop.getCurrency() + ". Choose " + (items ? "an item" : "a category") + " with ")
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBottom()))
                .append("/buy #");

        return new FancyMessage(builder.toString());
    }

    private static FancyMessage buildCategoryListContent(Shop shop, List<Category> categories, int page) {
        if (page < 1) {
            page = 1;
        }

        int pages = (int) Math.ceil((double) categories.size() / 4);
        if (page > pages) {
            page = pages;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(shop.getBorderV())
                .append("\n");

        int start = (page - 1) * 4;
        for (int i = start; i < start + 4; i++) {
            if (i >= categories.size()) {
                break;
            }

            Category category = categories.get(i);

            if (category == null) {
                break;
            }

            if (i != start) {
                builder.append("\n");
            }

            builder.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                    .append(shop.getBorderV())
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorId()))
                    .append(" " + (i + 1) + ".")
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBracket()))
                    .append(" [ ")
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorName()))
                    .append(category.getName().trim())
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBracket()))
                    .append(" ]\n");

            StringBuilder descriptionBuilder = new StringBuilder();
            if (category.getInfo() != null && !category.getInfo().isEmpty()) {
                descriptionBuilder.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                        .append(shop.getBorderV())
                        .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorInfo()))
                        .append(" " + category.getInfo().trim());
            }

            if (descriptionBuilder.length() > 0) {
                builder.append(TextUtils.trim(descriptionBuilder.toString(), null) + "\n");
            }

            builder.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                    .append(shop.getBorderV());
        }

        return new FancyMessage(builder.toString());
    }

    private static FancyMessage buildItemListContent(Player player, Shop shop, List<Item> items, int page) {
        int maxEntries = shop.isSimpleItems() ? 8 : 4;

        if (page < 1) {
            page = 1;
        }

        int pages = (int) Math.ceil((double) items.size() / maxEntries);
        if (page > pages) {
            page = pages;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(shop.getBorderV())
                .append("\n");

        FancyMessage message = new FancyMessage(builder.toString());
        int start = (page - 1) * maxEntries;
        for (int i = start; i < start + maxEntries; i++) {
            if (i >= items.size()) {
                break;
            }

            Item item = items.get(i);

            if (item == null) {
                break;
            }

            if (i != start) {
                builder.append("\n");
            }

            builder = new StringBuilder();
            builder.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                    .append(shop.getBorderV())
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorId()))
                    .append(" " + (i + 1) + ". ")
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorName()))
                    .append(item.getName().trim())
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBracket()))
                    .append(" (")
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorPrice()))
                    .append(item.getPrice() > 0.0 ? priceFormat.format(item.getPrice()) : "FREE")
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBracket()))
                    .append(")\n");
            message.then(builder.toString());

            if (!shop.isSimpleItems()) {
                StringBuilder urlBuilder = new StringBuilder();

                try {
                    URL url = new URL(shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
                    urlBuilder.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                            .append(shop.getBorderV())
                            .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorUrl()))
                            .append(" " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());

                    if (urlBuilder.length() > 0) {
                        message.then(TextUtils.trim(urlBuilder.toString(), null))
                                .link(url.toExternalForm())
                                .then("\n");
                    }
                } catch (MalformedURLException e) {
                    Enjin.getPlugin().debug("Malformed URL: " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
                }

                StringBuilder descriptionBuilder = new StringBuilder();
                if (item.getInfo() != null && !item.getInfo().isEmpty()) {
                    descriptionBuilder.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                            .append(shop.getBorderV())
                            .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorInfo()))
                            .append(" " + item.getInfo().trim());
                }

                if (descriptionBuilder.length() > 0) {
                    message.then(TextUtils.trim(descriptionBuilder.toString(), null) + "\n");
                }
            }

            builder = new StringBuilder()
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                    .append(shop.getBorderV());

            message.then(builder.toString());
        }

        return message;
    }

    private static FancyMessage buildItemContent(Player player, Shop shop, Item item) {
        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(shop.getBorderV())
                .append("\n")
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(shop.getBorderV())
                .append(ChatColor.translateAlternateColorCodes('&', "&f"))
                .append(ChatColor.translateAlternateColorCodes('&', " Price: "))
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorPrice()))
                .append(item.getPrice() > 0.0 ? priceFormat.format(item.getPrice()) : "FREE")
                .append("\n")
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(shop.getBorderV())
                .append(ChatColor.translateAlternateColorCodes('&', "&f"))
                .append(ChatColor.translateAlternateColorCodes('&', " Info:\n"));
        StringBuilder info = new StringBuilder()
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(shop.getBorderV())
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorInfo()))
                .append(" " + item.getInfo());
        FancyMessage message = new FancyMessage(builder.toString())
                .then(TextUtils.trim(info.toString(), ""))
                .then(ChatColor.translateAlternateColorCodes('&', "\n&" + shop.getColorBorder() + shop.getBorderV()))
                .then(ChatColor.translateAlternateColorCodes('&', "\n&" + shop.getColorBorder() + shop.getBorderV()))
                .then(ChatColor.RESET.toString() + " Click on the following link to checkout:\n");

        StringBuilder urlBuilder = new StringBuilder();
        try {
            URL url = new URL(shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
            urlBuilder.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                    .append(shop.getBorderV())
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorUrl()))
                    .append(" " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());

            if (urlBuilder.length() > 0) {
                message.then(TextUtils.trim(urlBuilder.toString(), null))
                        .link(url.toExternalForm())
                        .then("\n");
            }
        } catch (MalformedURLException e) {
            Enjin.getPlugin().debug("Malformed URL: " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
        }

        StringBuilder spacer = new StringBuilder()
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(shop.getBorderV());
        return message.then(spacer.toString());
    }

    private static FancyMessage buildFooterInfo(Shop shop) {
        StringBuilder builder = new StringBuilder()
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(shop.getBorderV())
                .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorText()))
                .append(" Type /buy to go back");

        return new FancyMessage(builder.toString());
    }

    private static FancyMessage buildFooter(String title, Shop shop, int page) {
        StringBuilder footer = new StringBuilder();
        String prefix = shop.getBorderC();
        String separator = "";
        String pagination = "";

        for (int i = 0; i < 3; i++) {
            prefix += shop.getBorderH();
        }

        if (prefix.length() > 4) {
            prefix = prefix.substring(0, 4);
        }

        footer.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                .append(prefix);

        if (page > 0) {
            for (int i = 0; i < 12; i++) {
                separator += shop.getBorderH();
            }

            if (separator.length() > 12) {
                separator = separator.substring(0, 12);
            }

            pagination = "Page " + page + " of " + (int) Math.ceil((double) shop.getCategories().size() / 4);

            footer.append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBottom()))
                    .append(" " + title + " ")
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()))
                    .append(separator + " ")
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBottom()))
                    .append(pagination + " ")
                    .append(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorBorder()));
        }

        for (int i = 0; i < 40; i++) {
            footer.append(shop.getBorderH());
        }

        return new FancyMessage(TextUtils.trim(footer.toString(), null));
    }
}
