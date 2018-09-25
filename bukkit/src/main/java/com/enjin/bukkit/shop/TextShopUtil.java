package com.enjin.bukkit.shop;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.util.text.LegacyTextUtil;
import com.enjin.bukkit.util.text.MessageUtil;
import com.enjin.bukkit.util.text.TextUtils;
import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.format.TextColor;
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
            Enjin.getLogger().debug("Sending a list of shops to " + player.getName());
            sendAvailableShops(player, instance);
        } else {
            if (instance.getActiveCategory() == null) {
                Enjin.getLogger().debug("Sending a list of categories to " + player.getName());
                sendAvailableCategories(player, instance, page < 1 ? 1 : page);
            } else {
                Category category = instance.getActiveCategory();
                if (category.getCategories() != null && !category.getCategories().isEmpty()) {
                    Enjin.getLogger()
                         .debug("Sending " + category.getCategories()
                                                     .size() + " sub-categories to " + player.getName());
                    sendAvailableCategories(player, instance, page < 1 ? 1 : page);
                } else {
                    Enjin.getLogger().debug("Sending a list of items to " + player.getName());
                    sendAvailableItems(player, instance, page);
                }
            }
        }
    }

    private static void sendAvailableShops(Player player, PlayerShopInstance instance) {
        List<Shop> available = instance.getShops();

        List<TextComponent> messages = new ArrayList<>();
        TextComponent       message  = TextComponent.of("=== Choose Shop ===");
        messages.add(message);
        message = TextComponent.of("Please type ")
                               .append(TextComponent.of(new StringBuilder('/').append(Enjin.getConfiguration(EMPConfig.class)
                                                                                           .getBuyCommand())
                                                                              .append(" shop #")
                                                                              .toString()).color(TextColor.YELLOW));
        messages.add(message);

        int            index    = 1;
        Iterator<Shop> iterator = available.iterator();
        while (iterator.hasNext()) {
            Shop shop = iterator.next();
            message = TextComponent.of(index++ + ". " + shop.getName())
                                   .color(TextColor.YELLOW);
            messages.add(message);
        }

        MessageUtil.sendMessages(player, messages);
    }

    private static void sendAvailableCategories(Player player, PlayerShopInstance instance, int page) {
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            MessageUtil.sendMessage(player, buildHeader(shop.getName(), shop));
            MessageUtil.sendMessages(player, buildShopInfo(shop, false));
            MessageUtil.sendMessages(player, buildCategoryListContent(shop, shop.getCategories(), page));
        } else {
            Category category = instance.getActiveCategory();

            MessageUtil.sendMessage(player, buildHeader(category.getName(), shop));
            MessageUtil.sendMessages(player, buildShopInfo(shop, false));
            MessageUtil.sendMessages(player, buildCategoryListContent(shop, category.getCategories(), page));
        }

        MessageUtil.sendMessage(player, buildFooterInfo(shop));
        MessageUtil.sendMessage(player, buildFooter(new StringBuilder("Type /")
                                                            .append(Enjin.getConfiguration(EMPConfig.class)
                                                                         .getBuyCommand())
                                                            .append(" page #")
                                                            .toString(), instance, shop, page < 1 ? 1 : page));
    }

    private static void sendAvailableItems(Player player, PlayerShopInstance instance, int page) {
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            player.sendMessage(ChatColor.RED.toString() + "You must select a category before you can view the item list");
        } else {
            Category category = instance.getActiveCategory();

            MessageUtil.sendMessage(player, buildHeader(category.getName(), shop));
            MessageUtil.sendMessages(player, buildShopInfo(shop, true));
            MessageUtil.sendMessages(player, buildItemListContent(player, shop, category.getItems(), page));
            MessageUtil.sendMessage(player, buildFooterInfo(shop));
            MessageUtil.sendMessage(player, buildFooter(new StringBuilder("Type /")
                                                                .append(Enjin.getConfiguration(EMPConfig.class)
                                                                             .getBuyCommand())
                                                                .append(" page #")
                                                                .toString(), instance, shop, page < 1 ? 1 : page));
        }
    }

    public static void sendItemInfo(Player player, PlayerShopInstance instance, int index) {
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            player.sendMessage("You must select a category before you can view the item list");
        } else {
            Category category = instance.getActiveCategory();
            Item     item     = index < 0 ? category.getItems().get(0) : (index < category.getItems()
                                                                                          .size() ? category.getItems()
                                                                                                            .get(index) : category
                    .getItems()
                    .get(category.getItems().size() - 1));

            MessageUtil.sendMessage(player, buildHeader(item.getName(), shop));
            MessageUtil.sendMessages(player, buildItemContent(player, shop, item));
            MessageUtil.sendMessage(player, buildFooterInfo(shop));
            MessageUtil.sendMessage(player, buildFooter("", null, shop, -1));

            instance.setActiveItem(item);
        }
    }

    public static void sendItemInfo(Player player, Shop shop, Item item) {
        if (item != null) {
            MessageUtil.sendMessage(player, buildHeader(item.getName(), shop));
            MessageUtil.sendMessages(player, buildItemContent(player, shop, item));
            MessageUtil.sendMessage(player, buildFooterInfo(shop));
            MessageUtil.sendMessage(player, buildFooter("", null, shop, -1));
        }
    }

    private static TextComponent buildHeader(String title, Shop shop) {
        StringBuilder header = new StringBuilder();
        String        prefix = shop.getBorderC();

        for (int i = 0; i < 3; i++) {
            prefix += shop.getBorderH();
        }

        if (prefix.length() > 4) {
            prefix = prefix.substring(0, 4);
        }

        header.append(ChatColor.getByChar(shop.getColorBorder())).append(prefix).append(" ")
              .append(ChatColor.getByChar(shop.getColorTitle())).append(title).append(" ")
              .append(LegacyTextUtil.getLegacyText(shop.getColorBorder()));

        for (int i = 0; i < 40; i++) {
            header.append(shop.getBorderH());
        }

        return TextComponent.of(TextUtils.trim(header.toString(), null));
    }

    private static List<TextComponent> buildShopInfo(Shop shop, boolean items) {
        List<TextComponent> messages = new ArrayList<>();
        TextComponent message = TextComponent.of(shop.getBorderV())
                                             .color(LegacyTextUtil.getColor(shop.getColorBorder()))
                                             .append(TextComponent.of((" " + shop.getInfo().trim()))
                                                                  .color(LegacyTextUtil.getColor(shop.getColorText())));
        messages.add(message);
        message = TextComponent.of(shop.getBorderV())
                               .color(LegacyTextUtil.getColor(shop.getColorBorder()))
                               .append(TextComponent.of((" " + "Prices are in " + shop.getCurrency() + ". Choose " + (items ? "an item" : "a category") + " with "))
                                                    .color(LegacyTextUtil.getColor(shop.getColorText())))
                               .append(TextComponent.of((new StringBuilder('/').append(Enjin.getConfiguration(EMPConfig.class)
                                                                                            .getBuyCommand())
                                                                               .append(" #")
                                                                               .toString()))
                                                    .color(LegacyTextUtil.getColor(shop.getColorBottom())));
        messages.add(message);

        return messages;
    }

    private static List<TextComponent> buildCategoryListContent(Shop shop, List<Category> categories, int page) {
        if (page < 1) {
            page = 1;
        }

        int pages = (int) Math.ceil((double) categories.size() / 4);
        if (page > pages) {
            page = pages;
        }

        List<TextComponent> messages = new ArrayList<>();
        TextComponent message = TextComponent.of(shop.getBorderV())
                                             .color(LegacyTextUtil.getColor(shop.getColorBorder()));
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

            message = TextComponent.of(shop.getBorderV())
                                   .color(LegacyTextUtil.getColor(shop.getColorBorder()))
                                   .append(TextComponent.of((" " + (i + 1) + "."))
                                                        .color(LegacyTextUtil.getColor(shop.getColorId()))
                                                        .append(TextComponent.of((" [ "))
                                                                             .color(LegacyTextUtil.getColor(shop.getColorBracket()))
                                                                             .append(TextComponent.of((category.getName()
                                                                                                               .trim()))
                                                                                                  .color(LegacyTextUtil.getColor(
                                                                                                          shop.getColorName()))
                                                                                                  .append(TextComponent.of(
                                                                                                          (" ]"))
                                                                                                                       .color(LegacyTextUtil
                                                                                                                                      .getColor(
                                                                                                                                              shop.getColorBracket()))))));
            messages.add(message);

            StringBuilder descriptionBuilder = new StringBuilder();
            if (category.getInfo() != null && !category.getInfo().isEmpty()) {
                descriptionBuilder.append(LegacyTextUtil.getLegacyText(shop.getColorBorder()).toString())
                                  .append(shop.getBorderV())
                                  .append(LegacyTextUtil.getLegacyText(shop.getColorInfo()).toString())
                                  .append(" ")
                                  .append(category.getInfo().trim());
            }

            if (descriptionBuilder.length() > 0) {
                message = TextComponent.of(TextUtils.trim(descriptionBuilder.toString(), null));
                messages.add(message);
            }

            message = TextComponent.of(shop.getBorderV())
                                   .color(LegacyTextUtil.getColor(shop.getColorBorder()));
            messages.add(message);
        }

        return messages;
    }

    private static List<TextComponent> buildItemListContent(Player player, Shop shop, List<Item> items, int page) {
        int maxEntries = shop.getSimpleItems().booleanValue() ? 8 : 4;

        if (page < 1) {
            page = 1;
        }

        int pages = (int) Math.ceil((double) items.size() / maxEntries);
        if (page > pages) {
            page = pages;
        }

        List<TextComponent> messages = new ArrayList<>();
        TextComponent message = TextComponent.of(shop.getBorderV())
                                             .color(LegacyTextUtil.getColor(shop.getColorBorder()));
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

            message = TextComponent.of(shop.getBorderV())
                                   .color(LegacyTextUtil.getColor(shop.getColorBorder()))
                                   .append(TextComponent.of((" " + (i + 1) + ". "))
                                                        .color(LegacyTextUtil.getColor(shop.getColorId()))
                                                        .append(TextComponent.of((item.getName().trim()))
                                                                             .color(LegacyTextUtil.getColor(shop.getColorName()))
                                                                             .append(TextComponent.of((" ("))
                                                                                                  .color(LegacyTextUtil.getColor(
                                                                                                          shop.getColorBracket())))));

            if (item.getPrice() != null) {
                message.append(TextComponent.of((item.getPrice() > 0.0 ? priceFormat.format(item.getPrice()) : "FREE"))
                                            .color(LegacyTextUtil.getColor(shop.getColorPrice())));
                if (item.getPrice() > 0.0) {
                    message.append(TextComponent.of((" " + shop.getCurrency()))
                                                .color(LegacyTextUtil.getColor(shop.getColorPrice())));
                }
            }

            if (item.getPoints() != null) {
                if (item.getPrice() != null) {
                    message.append(TextComponent.of((" or "))
                                                .color(LegacyTextUtil.getColor(shop.getColorPrice())));
                }

                message.append(TextComponent.of((item.getPoints() + " Points"))
                                            .color(LegacyTextUtil.getColor(shop.getColorPrice())));
            }

            message.append(TextComponent.of((")"))
                                        .color(LegacyTextUtil.getColor(shop.getColorBracket())));
            messages.add(message);

            if (!shop.getSimpleItems().booleanValue()) {
                StringBuilder urlBuilder = new StringBuilder();

                try {
                    URL url = new URL(shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
                    urlBuilder.append(LegacyTextUtil.getLegacyText(shop.getColorBorder()).toString())
                              .append(shop.getBorderV())
                              .append(LegacyTextUtil.getLegacyText(shop.getColorUrl()))
                              .append(" ")
                              .append(shop.getBuyUrl())
                              .append(item.getId())
                              .append("?player=")
                              .append(player.getName());

                    if (urlBuilder.length() > 0) {
                        message = TextComponent.of(TextUtils.trim(urlBuilder.toString(), null))
                                               .clickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                                                          url.toExternalForm()));
                        messages.add(message);
                    }
                } catch (MalformedURLException e) {
                    Enjin.getLogger()
                         .debug("Malformed URL: " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
                }

                StringBuilder descriptionBuilder = new StringBuilder();
                if (item.getInfo() != null && !item.getInfo().isEmpty()) {
                    descriptionBuilder.append(LegacyTextUtil.getLegacyText(shop.getColorBorder()))
                                      .append(shop.getBorderV())
                                      .append(LegacyTextUtil.getLegacyText(shop.getColorInfo()))
                                      .append(" ")
                                      .append(item.getInfo().trim());
                }

                if (descriptionBuilder.length() > 0) {
                    message = TextComponent.of(TextUtils.trim(descriptionBuilder.toString(), null));
                    messages.add(message);
                }
            }

            message = TextComponent.of(shop.getBorderV())
                                   .color(LegacyTextUtil.getColor(shop.getColorBorder()));
            messages.add(message);
        }

        return messages;
    }

    private static List<TextComponent> buildItemContent(Player player, Shop shop, Item item) {
        List<TextComponent> messages = new ArrayList<>();
        TextComponent       message  = null;

        StringBuilder builder = new StringBuilder();
        builder.append(LegacyTextUtil.getLegacyText(shop.getColorBorder()))
               .append(shop.getBorderV());

        if (item.getPrice() != null) {
            builder.append(LegacyTextUtil.getLegacyText(shop.getColorText()))
                   .append(" Price: ")
                   .append(LegacyTextUtil.getLegacyText(shop.getColorPrice()))
                   .append(item.getPrice() > 0.0 ? priceFormat.format(item.getPrice()) : "FREE");
            message = TextComponent.of(builder.toString());
            messages.add(message);
        }

        if (item.getPoints() != null) {
            message = null;
            if (item.getPrice() != null) {
                message = TextComponent.of(shop.getBorderV())
                                       .color(LegacyTextUtil.getColor(shop.getColorBorder()));
            }

            if (message != null) {
                message.append(TextComponent.of((" Points: "))
                                            .color(LegacyTextUtil.getColor(shop.getColorText()))
                                            .append(TextComponent.of((item.getPoints() > 0 ? item.getPoints()
                                                                                                 .toString() : "FREE"))
                                                                 .color(LegacyTextUtil.getColor(shop.getColorPrice()))));
            } else {
                message = TextComponent.of(" Points: ")
                                       .color(LegacyTextUtil.getColor(shop.getColorText()))
                                       .append(TextComponent.of((item.getPoints() > 0 ? item.getPoints()
                                                                                            .toString() : "FREE"))
                                                            .color(LegacyTextUtil.getColor(shop.getColorPrice())));
            }

            messages.add(message);
        }

        message = TextComponent.of(shop.getBorderV())
                               .color(LegacyTextUtil.getColor(shop.getColorBorder()))
                               .append(TextComponent.of((" Info:"))
                                                    .color(LegacyTextUtil.getColor(shop.getColorText())));
        messages.add(message);
        StringBuilder info = new StringBuilder()
                .append(LegacyTextUtil.getColor(shop.getColorBorder()))
                .append(shop.getBorderV())
                .append(LegacyTextUtil.getColor(shop.getColorInfo())).append(" ").append(item.getInfo());
        message = TextComponent.of(TextUtils.trim(info.toString(), ""));
        messages.add(message);
        message = TextComponent.of(shop.getBorderV())
                               .color(LegacyTextUtil.getColor(shop.getColorBorder()));
        messages.add(message);
        message = TextComponent.of(shop.getBorderV())
                               .color(LegacyTextUtil.getColor(shop.getColorBorder()))
                               .append(TextComponent.of((" Click on the following link to checkout:"))
                                                    .color(LegacyTextUtil.getColor(shop.getColorText())));
        messages.add(message);

        StringBuilder urlBuilder = new StringBuilder();
        try {
            URL url = new URL(shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
            urlBuilder.append(LegacyTextUtil.getLegacyText(shop.getColorBorder()))
                      .append(shop.getBorderV())
                      .append(LegacyTextUtil.getLegacyText(shop.getColorUrl()))
                      .append(" ")
                      .append(shop.getBuyUrl())
                      .append(item.getId())
                      .append("?player=")
                      .append(player.getName());

            if (urlBuilder.length() > 0) {
                message = TextComponent.of(TextUtils.trim(urlBuilder.toString(), null))
                                       .clickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url.toExternalForm()));
                messages.add(message);
            }
        } catch (MalformedURLException e) {
            Enjin.getLogger()
                 .debug("Malformed URL: " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
        }

        message = TextComponent.of(shop.getBorderV())
                               .color(LegacyTextUtil.getColor(shop.getColorBorder()));
        messages.add(message);

        return messages;
    }

    private static TextComponent buildFooterInfo(Shop shop) {
        StringBuilder builder = new StringBuilder()
                .append(LegacyTextUtil.getLegacyText(shop.getColorBorder()))
                .append(shop.getBorderV())
                .append(LegacyTextUtil.getLegacyText(shop.getColorText()))
                .append(new StringBuilder("Type /").append(Enjin.getConfiguration(EMPConfig.class).getBuyCommand())
                                                   .append(" to go back")
                                                   .toString());

        return TextComponent.of(builder.toString());
    }

    private static TextComponent buildFooter(String title, PlayerShopInstance instance, Shop shop, int page) {
        StringBuilder footer     = new StringBuilder();
        String        prefix     = shop.getBorderC();
        String        separator  = "";
        String        pagination = "";

        for (int i = 0; i < 3; i++) {
            prefix += shop.getBorderH();
        }

        if (prefix.length() > 4) {
            prefix = prefix.substring(0, 4);
        }

        footer.append(LegacyTextUtil.getLegacyText(shop.getColorBorder()))
              .append(prefix);

        if (page > 0) {
            for (int i = 0; i < 12; i++) {
                separator += shop.getBorderH();
            }

            if (separator.length() > 12) {
                separator = separator.substring(0, 12);
            }

            int entries  = instance.getActiveCategory() != null ? (instance.getActiveCategory()
                                                                           .getCategories()
                                                                           .size() > 0 ? instance.getActiveCategory()
                                                                                                 .getCategories()
                                                                                                 .size() : instance.getActiveCategory()
                                                                                                                   .getItems()
                                                                                                                   .size()) : shop
                    .getCategories()
                    .size();
            int lastPage = (int) Math.ceil((double) entries / 4);
            pagination = "Page " + (page > lastPage ? lastPage : page) + " of " + lastPage;

            footer.append(LegacyTextUtil.getLegacyText(shop.getColorBottom())).append(" ").append(title).append(" ")
                  .append(LegacyTextUtil.getLegacyText(shop.getColorBorder())).append(separator).append(" ")
                  .append(LegacyTextUtil.getLegacyText(shop.getColorBottom())).append(pagination).append(" ")
                  .append(LegacyTextUtil.getLegacyText(shop.getColorBorder()));
        }

        for (int i = 0; i < 40; i++) {
            footer.append(shop.getBorderH());
        }

        return TextComponent.of(TextUtils.trim(footer.toString(), null));
    }
}
