package com.enjin.bukkit.shop;

import com.enjin.bukkit.util.text.TextUtils;
import com.enjin.core.Enjin;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextShopUtil {
    private static DecimalFormat priceFormat = new DecimalFormat("#.00");

    public static void sendTextShop(Player player, PlayerShopInstance instance, int page) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

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

        List<FancyMessage> messages = new ArrayList<>();
        FancyMessage message = new FancyMessage("=== Choose Shop ===");
        messages.add(message);
        message = new FancyMessage("Please type ")
                .then("/buy shop <#>")
                .color(ChatColor.YELLOW);
        messages.add(message);

        int index = 1;
        Iterator<Shop> iterator = available.iterator();
        while (iterator.hasNext()) {
            Shop shop = iterator.next();
            message = new FancyMessage(index++ + ". " + shop.getName())
                    .color(ChatColor.YELLOW);
            messages.add(message);
        }

        for (FancyMessage m : messages) {
            m.send(player);
        }
    }

    private static void sendAvailableCategories(Player player, PlayerShopInstance instance, int page) {
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            buildHeader(shop.getName(), shop).send(player);
            for (FancyMessage message : buildShopInfo(shop, false)) {
                message.send(player);
            }
            for (FancyMessage message : buildCategoryListContent(shop, shop.getCategories(), page)) {
                message.send(player);
            }
            buildFooterInfo(shop).send(player);
            buildFooter("Type /buy page #", instance, shop, page < 1 ? 1 : page).send(player);
        } else {
            Category category = instance.getActiveCategory();

            buildHeader(category.getName(), shop).send(player);
            for (FancyMessage message : buildShopInfo(shop, false)) {
                message.send(player);
            }
            for (FancyMessage message : buildCategoryListContent(shop, category.getCategories(), page)) {
                message.send(player);
            }
            buildFooterInfo(shop).send(player);
            buildFooter("Type /buy page #", instance, shop, page < 1 ? 1 : page).send(player);
        }
    }

    private static void sendAvailableItems(Player player, PlayerShopInstance instance, int page) {
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            player.sendMessage(ChatColor.RED.toString() + "You must select a category before you can view the item list");
        } else {
            Category category = instance.getActiveCategory();

            buildHeader(category.getName(), shop).send(player);
            for (FancyMessage message : buildShopInfo(shop, true)) {
                message.send(player);
            }
            for (FancyMessage message : buildItemListContent(player, shop, category.getItems(), page)) {
                message.send(player);
            }
            buildFooterInfo(shop).send(player);
            buildFooter("Type /buy page #", instance, shop, page < 1 ? 1 : page).send(player);
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
            for (FancyMessage message : buildItemContent(player, shop, item)) {
                message.send(player);
            }
            buildFooterInfo(shop).send(player);
            buildFooter("", null, shop, -1).send(player);

            instance.setActiveItem(item);
        }
    }

    public static void sendItemInfo(Player player, Shop shop, Item item) {
        if (item != null) {
            buildHeader(item.getName(), shop).send(player);
            for (FancyMessage message : buildItemContent(player, shop, item)) {
                message.send(player);
            }
            buildFooterInfo(shop).send(player);
            buildFooter("", null, shop, -1).send(player);
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

        header.append(ChatColor.getByChar(shop.getColorBorder())).append(prefix).append(" ")
                .append(ChatColor.getByChar(shop.getColorTitle())).append(title).append(" ")
                .append(ChatColor.getByChar(shop.getColorBorder()));

        for (int i = 0; i < 40; i++) {
            header.append(shop.getBorderH());
        }

        return new FancyMessage(TextUtils.trim(header.toString(), null));
    }

    private static List<FancyMessage> buildShopInfo(Shop shop, boolean items) {
        List<FancyMessage> messages = new ArrayList<>();
        FancyMessage message = new FancyMessage(shop.getBorderV())
                .color(ChatColor.getByChar(shop.getColorBorder()))
                .then(" " + shop.getInfo().trim())
                .color(ChatColor.getByChar(shop.getColorText()));
        messages.add(message);
        message = new FancyMessage(shop.getBorderV())
                .color(ChatColor.getByChar(shop.getColorBorder()))
                .then(" " + "Prices are in " + shop.getCurrency() + ". Choose " + (items ? "an item" : "a category") + " with ")
                .color(ChatColor.getByChar(shop.getColorText()))
                .then("/buy #")
                .color(ChatColor.getByChar(shop.getColorBottom()));
        messages.add(message);

        return messages;
    }

    private static List<FancyMessage> buildCategoryListContent(Shop shop, List<Category> categories, int page) {
        if (page < 1) {
            page = 1;
        }

        int pages = (int) Math.ceil((double) categories.size() / 4);
        if (page > pages) {
            page = pages;
        }

        List<FancyMessage> messages = new ArrayList<>();
        FancyMessage message = new FancyMessage(shop.getBorderV())
                .color(ChatColor.getByChar(shop.getColorBorder()));
        messages.add(message);

        int start = (page - 1) * 4;
        for (int i = start; i < start + 4; i++) {
            if (i >= categories.size()) {
                break;
            }

            Category category = categories.get(i);

            if (category == null) {
                break;
            }

            message = new FancyMessage(shop.getBorderV())
                    .color(ChatColor.getByChar(shop.getColorBorder()))
                    .then(" " + (i + 1) + ".")
                    .color(ChatColor.getByChar(shop.getColorId()))
                    .then(" [ ")
                    .color(ChatColor.getByChar(shop.getColorBracket()))
                    .then(category.getName().trim())
                    .color(ChatColor.getByChar(shop.getColorName()))
                    .then(" ]")
                    .color(ChatColor.getByChar(shop.getColorBracket()));
            messages.add(message);

            StringBuilder descriptionBuilder = new StringBuilder();
            if (category.getInfo() != null && !category.getInfo().isEmpty()) {
                descriptionBuilder.append(ChatColor.getByChar(shop.getColorBorder()).toString())
                        .append(shop.getBorderV())
                        .append(ChatColor.getByChar(shop.getColorInfo()).toString()).append(" ").append(category.getInfo().trim());
            }

            if (descriptionBuilder.length() > 0) {
                message = new FancyMessage(TextUtils.trim(descriptionBuilder.toString(), null));
                messages.add(message);
            }

            message = new FancyMessage(shop.getBorderV())
                    .color(ChatColor.getByChar(shop.getColorBorder()));
            messages.add(message);
        }

        return messages;
    }

    private static List<FancyMessage> buildItemListContent(Player player, Shop shop, List<Item> items, int page) {
        int maxEntries = shop.getSimpleItems().booleanValue() ? 8 : 4;

        if (page < 1) {
            page = 1;
        }

        int pages = (int) Math.ceil((double) items.size() / maxEntries);
        if (page > pages) {
            page = pages;
        }

        List<FancyMessage> messages = new ArrayList<>();
        FancyMessage message = new FancyMessage(shop.getBorderV())
                .color(ChatColor.getByChar(shop.getColorBorder()));
        messages.add(message);

        int start = (page - 1) * maxEntries;
        for (int i = start; i < start + maxEntries; i++) {
            if (i >= items.size()) {
                break;
            }

            Item item = items.get(i);

            if (item == null) {
                break;
            }

            message = new FancyMessage(shop.getBorderV())
                    .color(ChatColor.getByChar(shop.getColorBorder()))
                    .then(" " + (i + 1) + ". ")
                    .color(ChatColor.getByChar(shop.getColorId()))
                    .then(item.getName().trim())
                    .color(ChatColor.getByChar(shop.getColorName()))
                    .then(" (")
                    .color(ChatColor.getByChar(shop.getColorBracket()));

            if (item.getPrice() != null) {
                message.then(item.getPrice() > 0.0 ? priceFormat.format(item.getPrice()) : "FREE")
                        .color(ChatColor.getByChar(shop.getColorPrice()));
                if (item.getPrice() > 0.0) {
                    message.then(" " + shop.getCurrency())
                            .color(ChatColor.getByChar(shop.getColorPrice()));
                }
            }

            if (item.getPoints() != null) {
                if (item.getPrice() != null) {
                    message.then(" or ")
                            .color(ChatColor.getByChar(shop.getColorPrice()));
                }

                message.then(item.getPoints() + " Points")
                        .color(ChatColor.getByChar(shop.getColorPrice()));
            }

            message.then(")")
                    .color(ChatColor.getByChar(shop.getColorBracket()));
            messages.add(message);

            if (!shop.getSimpleItems().booleanValue()) {
                StringBuilder urlBuilder = new StringBuilder();

                try {
                    URL url = new URL(shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
                    urlBuilder.append(ChatColor.getByChar(shop.getColorBorder()).toString())
                            .append(shop.getBorderV())
                            .append(ChatColor.getByChar(shop.getColorUrl())).append(" ").append(shop.getBuyUrl()).append(item.getId()).append("?player=").append(player.getName());

                    if (urlBuilder.length() > 0) {
                        message = new FancyMessage(TextUtils.trim(urlBuilder.toString(), null))
                                .link(url.toExternalForm());
                        messages.add(message);
                    }
                } catch (MalformedURLException e) {
                    Enjin.getPlugin().debug("Malformed URL: " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
                }

                StringBuilder descriptionBuilder = new StringBuilder();
                if (item.getInfo() != null && !item.getInfo().isEmpty()) {
                    descriptionBuilder.append(ChatColor.getByChar(shop.getColorBorder()))
                            .append(shop.getBorderV())
                            .append(ChatColor.getByChar(shop.getColorInfo())).append(" ").append(item.getInfo().trim());
                }

                if (descriptionBuilder.length() > 0) {
                    message = new FancyMessage(TextUtils.trim(descriptionBuilder.toString(), null));
                    messages.add(message);
                }
            }

            message = new FancyMessage(shop.getBorderV())
                    .color(ChatColor.getByChar(shop.getColorBorder()));
            messages.add(message);
        }

        return messages;
    }

    private static List<FancyMessage> buildItemContent(Player player, Shop shop, Item item) {
        List<FancyMessage> messages = new ArrayList<>();
        FancyMessage message = null;

        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.getByChar(shop.getColorBorder()))
                .append(shop.getBorderV());

        if (item.getPrice() != null) {
            builder.append(ChatColor.getByChar(shop.getColorText()))
                    .append(" Price: ")
                    .append(ChatColor.getByChar(shop.getColorPrice()))
                    .append(item.getPrice() > 0.0 ? priceFormat.format(item.getPrice()) : "FREE");
            message = new FancyMessage(builder.toString());
            messages.add(message);
        }

        if (item.getPoints() != null) {
            message = null;
            if (item.getPrice() != null) {
                message = new FancyMessage(shop.getBorderV())
                        .color(ChatColor.getByChar(shop.getColorBorder()));
            }

            if (message != null) {
                message.then(" Points: ")
                        .color(ChatColor.getByChar(shop.getColorText()))
                        .then(item.getPoints() > 0 ? item.getPoints().toString() : "FREE")
                        .color(ChatColor.getByChar(shop.getColorPrice()));
            } else {
                message = new FancyMessage(" Points: ")
                        .color(ChatColor.getByChar(shop.getColorText()))
                        .then(item.getPoints() > 0 ? item.getPoints().toString() : "FREE")
                        .color(ChatColor.getByChar(shop.getColorPrice()));
            }

            messages.add(message);
        }

        message = new FancyMessage(shop.getBorderV())
                .color(ChatColor.getByChar(shop.getColorBorder()))
                .then(" Info:")
                .color(ChatColor.getByChar(shop.getColorText()));
        messages.add(message);
        StringBuilder info = new StringBuilder()
                .append(ChatColor.getByChar(shop.getColorBorder()))
                .append(shop.getBorderV())
                .append(ChatColor.getByChar(shop.getColorInfo())).append(" ").append(item.getInfo());
        message = new FancyMessage(TextUtils.trim(info.toString(), ""));
        messages.add(message);
        message = new FancyMessage(shop.getBorderV())
                .color(ChatColor.getByChar(shop.getColorBorder()));
        messages.add(message);
        message = new FancyMessage(shop.getBorderV())
                .color(ChatColor.getByChar(shop.getColorBorder()))
                .then(" Click on the following link to checkout:")
                .color(ChatColor.getByChar(shop.getColorText()));
        messages.add(message);

        StringBuilder urlBuilder = new StringBuilder();
        try {
            URL url = new URL(shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
            urlBuilder.append(ChatColor.getByChar(shop.getColorBorder()))
                    .append(shop.getBorderV())
                    .append(ChatColor.getByChar(shop.getColorUrl())).append(" ").append(shop.getBuyUrl()).append(item.getId()).append("?player=").append(player.getName());

            if (urlBuilder.length() > 0) {
                message = new FancyMessage(TextUtils.trim(urlBuilder.toString(), null))
                        .link(url.toExternalForm());
                messages.add(message);
            }
        } catch (MalformedURLException e) {
            Enjin.getPlugin().debug("Malformed URL: " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
        }

        message = new FancyMessage(shop.getBorderV())
                .color(ChatColor.getByChar(shop.getColorBorder()));
        messages.add(message);

        return messages;
    }

    private static FancyMessage buildFooterInfo(Shop shop) {
        StringBuilder builder = new StringBuilder()
                .append(ChatColor.getByChar(shop.getColorBorder()))
                .append(shop.getBorderV())
                .append(ChatColor.getByChar(shop.getColorText()))
                .append(" Type /buy to go back");

        return new FancyMessage(builder.toString());
    }

    private static FancyMessage buildFooter(String title, PlayerShopInstance instance, Shop shop, int page) {
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

        footer.append(ChatColor.getByChar(shop.getColorBorder()))
                .append(prefix);

        if (page > 0) {
            for (int i = 0; i < 12; i++) {
                separator += shop.getBorderH();
            }

            if (separator.length() > 12) {
                separator = separator.substring(0, 12);
            }

            int entries = instance.getActiveCategory() != null ? (instance.getActiveCategory().getCategories().size() > 0 ? instance.getActiveCategory().getCategories().size() : instance.getActiveCategory().getItems().size()) : shop.getCategories().size();
            int lastPage = (int) Math.ceil((double) entries / 4);
            pagination = "Page " + (page > lastPage ? lastPage : page) + " of " + lastPage;

            footer.append(ChatColor.getByChar(shop.getColorBottom())).append(" ").append(title).append(" ")
                    .append(ChatColor.getByChar(shop.getColorBorder())).append(separator).append(" ")
                    .append(ChatColor.getByChar(shop.getColorBottom())).append(pagination).append(" ")
                    .append(ChatColor.getByChar(shop.getColorBorder()));
        }

        for (int i = 0; i < 40; i++) {
            footer.append(shop.getBorderH());
        }

        return new FancyMessage(TextUtils.trim(footer.toString(), null));
    }
}
