package com.enjin.sponge.shop;

import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.utils.text.TextUtils;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

public class ShopUtil {
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

        Text.Builder builder = Text.builder("=== Choose Shop ===\n")
                .append(Text.of("Please type "))
                .append(Text.builder("/buy shop <#>\n\n").color(TextColors.YELLOW).build());

        int index = 1;
        Iterator<Shop> iterator = available.iterator();
        while (iterator.hasNext()) {
            Shop shop = iterator.next();
            builder.append(Text.builder(index++ + ". " + shop.getName()).color(TextColors.YELLOW).build());
            if (iterator.hasNext()) {
                builder.append(Text.of("\n"));
            }
        }

        player.sendMessage(builder.build());
    }

    private static void sendAvailableCategories(Player player, PlayerShopInstance instance, int page) {
        Text.Builder builder = Text.builder();
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            builder.append(buildHeader(shop.getName(), shop))
                    .append(buildShopInfo(shop, false))
                    .append(buildCategoryListContent(shop, shop.getCategories(), page))
                    .append(buildFooterInfo(shop))
                    .append(buildFooter("Type /buy page #", shop, page < 1 ? 1 : page));
        } else {
            Category category = instance.getActiveCategory();
            builder.append(buildHeader(category.getName(), shop))
                    .append(buildShopInfo(shop, false))
                    .append(buildCategoryListContent(shop, category.getCategories(), page))
                    .append(buildFooterInfo(shop))
                    .append(buildFooter("Type /buy page #", shop, page < 1 ? 1 : page));
        }

        player.sendMessage(builder.build());
    }

    private static void sendAvailableItems(Player player, PlayerShopInstance instance, int page) {
        Text.Builder builder = Text.builder().color(TextColors.RED);
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            builder.append(Text.of("You must select a category before you can view the item list"));
        } else {
            Category category = instance.getActiveCategory();
            builder.append(buildHeader(category.getName(), shop))
                    .append(buildShopInfo(shop, true))
                    .append(buildItemListContent(player, shop, category.getItems(), page))
                    .append(buildFooterInfo(shop))
                    .append(buildFooter("Type /buy page #", shop, page < 1 ? 1 : page));
        }

        player.sendMessage(builder.build());
    }

    public static void sendItemInfo(Player player, PlayerShopInstance instance, int index) {
        Text.Builder builder = Text.builder().color(TextColors.RED);
        Shop shop = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            builder.append(Text.of("You must select a category before you can view the item list"));
        } else {
            Category category = instance.getActiveCategory();
            Item item = index < 0 ? category.getItems().get(0) : (index < category.getItems().size() ? category.getItems().get(index) : category.getItems().get(category.getItems().size() - 1));
            builder.append(buildHeader(item.getName(), shop))
                    .append(buildItemContent(player, shop, item))
                    .append(buildFooterInfo(shop))
                    .append(buildFooter("", shop, -1));
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

        header.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(prefix + " ")
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorTitle(), '&'))
                .append(title + " ")
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'));

        for (int i = 0; i < 40; i++) {
            header.append(shop.getBorderH());
        }

        return Text.builder(TextUtils.trim(header.toString(), null) + "\n").build();
    }

    private static Text buildShopInfo(Shop shop, boolean items) {
        StringBuilder builder = new StringBuilder()
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(shop.getBorderV())
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorText(), '&'))
                .append(" " + shop.getInfo().trim() + "\n")
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(shop.getBorderV())
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorText(), '&'))
                .append(" " + "Prices are in " + shop.getCurrency() + ". Choose " + (items ? "an item" : "a category") + " with ")
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBottom(), '&'))
                .append("/buy #")
                .append("\n");

        return Text.builder(builder.toString()).build();
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
        builder.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
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

            builder.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                    .append(shop.getBorderV())
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorId(), '&'))
                    .append(" " + (i + 1) + ".")
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBracket(), '&'))
                    .append(" [ ")
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorName(), '&'))
                    .append(category.getName().trim())
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBracket(), '&'))
                    .append(" ]\n");

            StringBuilder descriptionBuilder = new StringBuilder();
            if (category.getInfo() != null && !category.getInfo().isEmpty()) {
                descriptionBuilder.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                        .append(shop.getBorderV())
                        .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorInfo(), '&'))
                        .append(" " + category.getInfo().trim());
            }

            if (descriptionBuilder.length() > 0) {
                builder.append(TextUtils.trim(descriptionBuilder.toString(), null) + "\n");
            }

            builder.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                    .append(shop.getBorderV() + "\n");
        }

        return Text.builder(builder.toString()).build();
    }

    private static Text buildItemListContent(Player player, Shop shop, List<Item> items, int page) {
        int maxEntries = shop.getSimpleItems().booleanValue() ? 8 : 4;

        if (page < 1) {
            page = 1;
        }

        int pages = (int) Math.ceil((double) items.size() / maxEntries);
        if (page > pages) {
            page = pages;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(shop.getBorderV())
                .append("\n");

        Text.Builder textBuilder = Text.builder(builder.toString());
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
            builder.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                    .append(shop.getBorderV())
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorId(), '&'))
                    .append(" " + (i + 1) + ". ")
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorName(), '&'))
                    .append(item.getName().trim())
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBracket(), '&'))
                    .append(" (")
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorPrice(), '&'))
                    .append(item.getPrice() > 0.0 ? priceFormat.format(item.getPrice()) : "FREE")
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBracket(), '&'))
                    .append(")\n");
            textBuilder.append(Text.of(builder.toString()));

            if (!shop.getSimpleItems().booleanValue()) {
                StringBuilder urlBuilder = new StringBuilder();

                try {
                    URL url = new URL(shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
                    urlBuilder.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                            .append(shop.getBorderV())
                            .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorUrl(), '&'))
                            .append(" " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());

                    if (urlBuilder.length() > 0) {
                        Text text = Text.builder(TextUtils.trim(urlBuilder.toString(), null))
                                .onClick(TextActions.openUrl(url))
                                .build();
                        textBuilder.append(text)
                                .append(Text.of("\n"));
                    }
                } catch (MalformedURLException e) {
                    EnjinMinecraftPlugin.getInstance().debug("Malformed URL: " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
                }

                StringBuilder descriptionBuilder = new StringBuilder();
                if (item.getInfo() != null && !item.getInfo().isEmpty()) {
                    descriptionBuilder.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                            .append(shop.getBorderV())
                            .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorInfo(), '&'))
                            .append(" " + item.getInfo().trim());
                }

                if (descriptionBuilder.length() > 0) {
                    textBuilder.append(Text.of(TextUtils.trim(descriptionBuilder.toString(), null) + "\n"));
                }
            }

            builder = new StringBuilder()
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                    .append(shop.getBorderV() + "\n");

            textBuilder.append(Text.of(builder.toString()));
        }

        return textBuilder.build();
    }

    private static Text buildItemContent(Player player, Shop shop, Item item) {
        StringBuilder builder = new StringBuilder();
        builder.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(shop.getBorderV())
                .append("\n")
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(shop.getBorderV())
                .append(TextUtils.translateAlternateColorCodes("&f", '&'))
                .append(TextUtils.translateAlternateColorCodes(" Price: ", '&'))
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorPrice(), '&'))
                .append(item.getPrice() > 0.0 ? priceFormat.format(item.getPrice()) : "FREE")
                .append("\n")
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(shop.getBorderV())
                .append(TextUtils.translateAlternateColorCodes("&f", '&'))
                .append(TextUtils.translateAlternateColorCodes(" Info:\n", '&'));
        StringBuilder info = new StringBuilder()
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(shop.getBorderV())
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorInfo(), '&'))
                .append(" " + item.getInfo());
        Text.Builder textBuilder = Text.builder(builder.toString())
                .append(Text.of(TextUtils.trim(info.toString(), "")))
                .append(Text.builder(TextUtils.translateAlternateColorCodes("\n&" + shop.getColorBorder() + shop.getBorderV(), '&')).build())
                .append(Text.builder(TextUtils.translateAlternateColorCodes("\n&" + shop.getColorBorder() + shop.getBorderV(), '&')).build())
                .append(Text.builder(" Click on the following link to checkout:\n").color(TextColors.RESET).build());

        StringBuilder urlBuilder = new StringBuilder();
        try {
            URL url = new URL(shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
            urlBuilder.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                    .append(shop.getBorderV())
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorUrl(), '&'))
                    .append(" " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());

            if (urlBuilder.length() > 0) {
                Text text = Text.builder(TextUtils.trim(urlBuilder.toString(), null))
                        .onClick(TextActions.openUrl(url))
                        .build();
                textBuilder.append(text)
                        .append(Text.of("\n"));
            }
        } catch (MalformedURLException e) {
            EnjinMinecraftPlugin.getInstance().debug("Malformed URL: " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
        }

        StringBuilder spacer = new StringBuilder()
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(shop.getBorderV())
                .append("\n");
        return textBuilder.append(Text.of(spacer.toString())).build();
    }

    private static Text buildFooterInfo(Shop shop) {
        StringBuilder builder = new StringBuilder()
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(shop.getBorderV())
                .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorText(), '&'))
                .append(" Type /buy to go back\n");

        return Text.builder(builder.toString()).build();
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

        footer.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                .append(prefix);

        if (page > 0) {
            for (int i = 0; i < 12; i++) {
                separator += shop.getBorderH();
            }

            if (separator.length() > 12) {
                separator = separator.substring(0, 12);
            }

            pagination = "Page " + page + " of " + (int) Math.ceil((double) shop.getCategories().size() / 4);

            footer.append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBottom(), '&'))
                    .append(" " + title + " ")
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'))
                    .append(separator + " ")
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBottom(), '&'))
                    .append(pagination + " ")
                    .append(TextUtils.translateAlternateColorCodes("&" + shop.getColorBorder(), '&'));
        }

        for (int i = 0; i < 40; i++) {
            footer.append(shop.getBorderH());
        }

        return Text.builder(TextUtils.trim(footer.toString(), null)).build();
    }
}
