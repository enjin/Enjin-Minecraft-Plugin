package com.enjin.officialplugin.shop;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.data.Category;
import com.enjin.officialplugin.shop.data.Item;
import com.enjin.officialplugin.shop.data.Shop;
import com.enjin.officialplugin.utils.text.TextUtils;
import com.google.common.collect.Lists;
import com.google.gson.*;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

public class ShopUtil {
    private static Gson gson = new GsonBuilder().create();
    private static DecimalFormat priceFormat = new DecimalFormat("#.00");

    public static List<Shop> getShopsFromJSON(String json) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);

        List<Shop> shops = Lists.newArrayList();
        for (JsonElement elem : element.getAsJsonArray()) {
            try {
                Shop shop = gson.fromJson(elem, Shop.class);
                shops.add(shop);
            } catch (JsonSyntaxException e) {
                plugin.getLogger().error("", e);
            }
        }

        return shops;
    }

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

        TextBuilder builder = Texts.builder("=== Choose Shop ===\n")
                .append(Texts.of("Please type "))
                .append(Texts.builder("/buy shop <#>\n\n").color(TextColors.YELLOW).build());

        int index = 1;
        Iterator<Shop> iterator = available.iterator();
        while (iterator.hasNext()) {
            Shop shop = iterator.next();
            builder.append(Texts.builder(index++ + ". " + shop.getName()).color(TextColors.YELLOW).build());
            if (iterator.hasNext()) {
                builder.append(Texts.of("\n"));
            }
        }

        player.sendMessage(builder.build());
    }

    private static void sendAvailableCategories(Player player, PlayerShopInstance instance, int page) {
        TextBuilder builder = Texts.builder();
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            builder.append(buildHeader(shop.getName(), shop))
                    .append(buildShopInfo(shop, false))
                    .append(buildCategoryListContent(shop, shop.getCategories(), page))
                    .append(buildFooterInfo(shop))
                    .append(buildFooter("Type /buy page #", shop, page));
        } else {
            Category category = instance.getActiveCategory();
            builder.append(buildHeader(category.getName(), shop))
                    .append(buildShopInfo(shop, false))
                    .append(buildCategoryListContent(shop, category.getCategories(), page))
                    .append(buildFooterInfo(shop))
                    .append(buildFooter("Type /buy page #", shop, page));
        }

        player.sendMessage(builder.build());
    }

    private static void sendAvailableItems(Player player, PlayerShopInstance instance, int page) {
        TextBuilder builder = Texts.builder().color(TextColors.RED);
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            builder.append(Texts.of("You must select a category before you can view the item list"));
        } else {
            Category category = instance.getActiveCategory();
            builder.append(buildHeader(category.getName(), shop))
                    .append(buildShopInfo(shop, true))
                    .append(buildItemListContent(player, shop, category.getItems(), page))
                    .append(buildFooterInfo(shop))
                    .append(buildFooter("Type /buy page #", shop, page));
        }

        player.sendMessage(builder.build());
    }

    private static Text buildHeader(String title, Shop shop) {
        StringBuilder header = new StringBuilder();
        String prefix = shop.getBorderC();

        for (int i = 0; i < 3; i++) {
            prefix += shop.getBorderH();
        }

        if (prefix.length() > 4) {
            prefix = prefix.substring(0, 4);
        }

        header.append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                .append(prefix + " ")
                .append(Texts.replaceCodes("&" + shop.getColortitle(), '&'))
                .append(title + " ")
                .append(Texts.replaceCodes("&" + shop.getColorborder(), '&'));

        for (int i = 0; i < 40; i++) {
            header.append(shop.getBorderH());
        }

        return Texts.builder(TextUtils.trim(header.toString(), null) + "\n").build();
    }

    private static Text buildShopInfo(Shop shop, boolean items) {
        StringBuilder builder = new StringBuilder()
                .append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                .append(shop.getBorderV())
                .append(Texts.replaceCodes("&" + shop.getColortext(), '&'))
                .append(" " + shop.getInfo().trim() + "\n")
                .append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                .append(shop.getBorderV())
                .append(Texts.replaceCodes("&" + shop.getColortext(), '&'))
                .append(" " + "Prices are in " + shop.getCurrency() + ". Choose " + (items ? "an item" : "a category") + " with ")
                .append(Texts.replaceCodes("&" + shop.getColorbottom(), '&'))
                .append("/buy #")
                .append("\n");

        return Texts.builder(builder.toString()).build();
    }

    private static Text buildCategoryListContent(Shop shop, List<Category> categories, int page) {
        if (page < 1) {
            page = 1;
        }

        int pages = (int) Math.ceil((double) categories.size() / 4);
        if (page > pages) {
            page = pages;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
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

            builder.append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                    .append(shop.getBorderV())
                    .append(Texts.replaceCodes("&" + shop.getColorid(), '&'))
                    .append(" " + (i + 1) + ".")
                    .append(Texts.replaceCodes("&" + shop.getColorbracket(), '&'))
                    .append(" [ ")
                    .append(Texts.replaceCodes("&" + shop.getColorname(), '&'))
                    .append(category.getName().trim())
                    .append(Texts.replaceCodes("&" + shop.getColorbracket(), '&'))
                    .append(" ]\n");

            StringBuilder descriptionBuilder = new StringBuilder();
            if (category.getInfo() != null && !category.getInfo().isEmpty()) {
                descriptionBuilder.append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                        .append(shop.getBorderV())
                        .append(Texts.replaceCodes("&" + shop.getColorinfo(), '&'))
                        .append(" " + category.getInfo().trim());
            }

            if (descriptionBuilder.length() > 0) {
                builder.append(TextUtils.trim(descriptionBuilder.toString(), null) + "\n");
            }

            builder.append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                    .append(shop.getBorderV() + "\n");
        }

        return Texts.builder(builder.toString()).build();
    }

    private static Text buildItemListContent(Player player, Shop shop, List<Item> items, int page) {
        int maxEntries = shop.isSimpleitems() ? 8 : 4;

        if (page < 1) {
            page = 1;
        }

        int pages = (int) Math.ceil((double) items.size() / maxEntries);
        if (page > pages) {
            page = pages;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                .append(shop.getBorderV())
                .append("\n");

        TextBuilder textBuilder = Texts.builder(builder.toString());
        int start = (page - 1) * maxEntries;
        for (int i = start; i < start + maxEntries; i++) {
            if (i >= items.size()) {
                break;
            }

            Item item = items.get(i);

            if (item == null) {
                break;
            }

            builder = new StringBuilder();
            builder.append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                    .append(shop.getBorderV())
                    .append(Texts.replaceCodes("&" + shop.getColorid(), '&'))
                    .append(" " + (i + 1) + ". ")
                    .append(Texts.replaceCodes("&" + shop.getColorname(), '&'))
                    .append(item.getName().trim())
                    .append(Texts.replaceCodes("&" + shop.getColorbracket(), '&'))
                    .append(" (")
                    .append(Texts.replaceCodes("&" + shop.getColorprice(), '&'))
                    .append(item.getPrice() > 0.0 ? priceFormat.format(item.getPrice()) : "FREE")
                    .append(Texts.replaceCodes("&" + shop.getColorbracket(), '&'))
                    .append(")\n");
            textBuilder.append(Texts.of(builder.toString()));

            if (!shop.isSimpleitems()) {
                StringBuilder urlBuilder = new StringBuilder();

                try {
                    URL url = new URL(shop.getBuyurl() + item.getId() + "?player=" + player.getName());
                    urlBuilder.append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                            .append(shop.getBorderV())
                            .append(Texts.replaceCodes("&" + shop.getColorurl(), '&'))
                            .append(" " + shop.getBuyurl() + item.getId() + "?player=" + player.getName());

                    if (urlBuilder.length() > 0) {
                        Text text = Texts.builder(TextUtils.trim(urlBuilder.toString(), null))
                                .onClick(TextActions.openUrl(url))
                                .build();
                        textBuilder.append(text)
                                .append(Texts.of("\n"));
                    }
                } catch (MalformedURLException e) {
                    EnjinMinecraftPlugin.getInstance().debug("Malformed URL: " + shop.getBuyurl() + item.getId() + "?player=" + player.getName());
                }

                StringBuilder descriptionBuilder = new StringBuilder();
                if (item.getInfo() != null && !item.getInfo().isEmpty()) {
                    descriptionBuilder.append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                            .append(shop.getBorderV())
                            .append(Texts.replaceCodes("&" + shop.getColorinfo(), '&'))
                            .append(" " + item.getInfo().trim());
                }

                if (descriptionBuilder.length() > 0) {
                    textBuilder.append(Texts.of(TextUtils.trim(descriptionBuilder.toString(), null) + "\n"));
                }
            }

            builder = new StringBuilder()
                    .append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                    .append(shop.getBorderV() + "\n");

            textBuilder.append(Texts.of(builder.toString()));
        }

        return textBuilder.build();
    }

    private static Text buildFooterInfo(Shop shop) {
        StringBuilder builder = new StringBuilder()
                .append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                .append(shop.getBorderV())
                .append(Texts.replaceCodes("&" + shop.getColortext(), '&'))
                .append(" Type /buy to go back\n");

        return Texts.builder(builder.toString()).build();
    }

    private static Text buildFooter(String title, Shop shop, int page) {
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

        footer.append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                .append(prefix);

        if (page > 0) {
            for (int i = 0; i < 12; i++) {
                separator += shop.getBorderH();
            }

            if (separator.length() > 12) {
                separator = separator.substring(0, 12);
            }

            pagination = "Page " + page + " of " + (int) Math.ceil((double) shop.getCategories().size() / 4);

            footer.append(Texts.replaceCodes("&" + shop.getColorbottom(), '&'))
                    .append(" " + title + " ")
                    .append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                    .append(separator + " ")
                    .append(Texts.replaceCodes("&" + shop.getColorbottom(), '&'))
                    .append(pagination + " ")
                    .append(Texts.replaceCodes("&" + shop.getColorborder(), '&'));
        }

        for (int i = 0; i < 40; i++) {
            footer.append(shop.getBorderH());
        }

        return Texts.builder(TextUtils.trim(footer.toString(), null)).build();
    }
}
