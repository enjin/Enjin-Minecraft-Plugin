package com.enjin.sponge.shop;

import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.utils.text.TextUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
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

        Text.Builder builder = Text.builder("=== Choose Shop ===\n")
                                   .append(Text.of("Please type "))
                                   .append(Text.builder(new StringBuilder('/').append(Enjin.getConfiguration(EMPConfig.class)
                                                                                           .getBuyCommand())
                                                                              .append(" shop #\n\n")
                                                                              .toString())
                                               .color(TextColors.YELLOW)
                                               .build());

        int            index    = 1;
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
        Shop         shop    = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            builder.append(buildHeader(shop.getName(), shop))
                   .append(buildShopInfo(shop, false))
                   .append(buildCategoryListContent(shop, shop.getCategories(), page))
                   .append(buildFooterInfo(shop))
                   .append(buildFooter(new StringBuilder("Type /").append(Enjin.getConfiguration(EMPConfig.class)
                                                                               .getBuyCommand())
                                                                  .append(" page #")
                                                                  .toString(), instance, shop, page < 1 ? 1 : page));
        } else {
            Category category = instance.getActiveCategory();
            builder.append(buildHeader(category.getName(), shop))
                   .append(buildShopInfo(shop, false))
                   .append(buildCategoryListContent(shop, category.getCategories(), page))
                   .append(buildFooterInfo(shop))
                   .append(buildFooter(new StringBuilder("Type /").append(Enjin.getConfiguration(EMPConfig.class)
                                                                               .getBuyCommand())
                                                                  .append(" page #")
                                                                  .toString(), instance, shop, page < 1 ? 1 : page));
        }

        player.sendMessage(builder.build());
    }

    private static void sendAvailableItems(Player player, PlayerShopInstance instance, int page) {
        Text.Builder builder = Text.builder().color(TextColors.RED);
        Shop         shop    = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            builder.append(Text.of("You must select a category before you can view the item list"));
        } else {
            Category category = instance.getActiveCategory();
            builder.append(buildHeader(category.getName(), shop))
                   .append(buildShopInfo(shop, true))
                   .append(buildItemListContent(player, shop, category.getItems(), page))
                   .append(buildFooterInfo(shop))
                   .append(buildFooter(new StringBuilder("Type /").append(Enjin.getConfiguration(EMPConfig.class)
                                                                               .getBuyCommand())
                                                                  .append(" page #")
                                                                  .toString(), instance, shop, page < 1 ? 1 : page));
        }

        player.sendMessage(builder.build());
    }

    public static void sendItemInfo(Player player, PlayerShopInstance instance, int index) {
        Text.Builder builder = Text.builder().color(TextColors.RED);
        Shop         shop    = instance.getActiveShop();

        if (instance.getActiveCategory() == null) {
            builder.append(Text.of("You must select a category before you can view the item list"));
        } else {
            Category category = instance.getActiveCategory();
            Item     item     = index < 0 ? category.getItems().get(0) : (index < category.getItems()
                                                                                          .size() ? category.getItems()
                                                                                                            .get(index) : category
                    .getItems()
                    .get(category.getItems().size() - 1));
            builder.append(buildHeader(item.getName(), shop))
                   .append(buildItemContent(player, shop, item))
                   .append(buildFooterInfo(shop))
                   .append(buildFooter("", instance, shop, -1));
        }

        player.sendMessage(builder.build());
    }

    public static void sendItemInfo(Player player, Shop shop, Item item) {
        Text.Builder builder = Text.builder();

        if (item != null) {
            builder.append(buildHeader(item.getName(), shop))
                   .append(buildItemContent(player, shop, item))
                   .append(buildFooterInfo(shop))
                   .append(buildFooter("", null, shop, -1));
            player.sendMessage(builder.build());
        }
    }

    private static Text buildHeader(String title, Shop shop) {
        StringBuilder header = new StringBuilder();
        String        prefix = shop.getBorderC();

        for (int i = 0; i < 3; i++) {
            prefix += shop.getBorderH();
        }

        if (prefix.length() > 4) {
            prefix = prefix.substring(0, 4);
        }

        header.append('&').append(shop.getColorBorder()).append(prefix).append(' ')
              .append('&').append(shop.getColorTitle()).append(title).append(' ')
              .append('&').append(shop.getColorBorder());

        for (int i = 0; i < 40; i++) {
            header.append(shop.getBorderH());
        }

        return Text.builder()
                   .append(TextUtils.translateText(TextUtils.trim(header.toString(), null)))
                   .append(Text.NEW_LINE)
                   .build();
    }

    private static Text buildShopInfo(Shop shop, boolean items) {
        Text.Builder text = Text.builder();
        StringBuilder builder = new StringBuilder()
                .append('&').append(shop.getColorBorder()).append(shop.getBorderV())
                .append('&').append(shop.getColorText()).append(' ').append(shop.getInfo().trim());
        text.append(TextUtils.translateText(builder.toString()))
            .append(Text.NEW_LINE);
        builder = new StringBuilder()
                .append('&').append(shop.getColorBorder()).append(shop.getBorderV())
                .append('&').append(shop.getColorText()).append(" Prices are in ").append(shop.getCurrency())
                .append(". Choose ").append(items ? "an item" : "a category").append(" with ")
                .append('&').append(shop.getColorBottom()).append('/')
                .append(Enjin.getConfiguration(EMPConfig.class).getBuyCommand()).append(" #");
        text.append(TextUtils.translateText(builder.toString()))
            .append(Text.NEW_LINE);

        return text.build();
    }

    private static Text buildCategoryListContent(Shop shop, List<Category> categories, int page) {
        if (page < 1) {
            page = 1;
        }

        int pages = (int) Math.ceil((double) categories.size() / 4);
        if (page > pages) {
            page = pages;
        }

        Text.Builder text = Text.builder();
        StringBuilder builder = new StringBuilder()
                .append('&').append(shop.getColorBorder()).append(shop.getBorderV());
        text.append(TextUtils.translateText(builder.toString()))
            .append(Text.NEW_LINE);

        int start = (page - 1) * 4;
        for (int i = start; i < start + 4; i++) {
            if (i >= categories.size()) {
                break;
            }

            Category category = categories.get(i);

            if (category == null) {
                break;
            }

            builder = new StringBuilder()
                    .append('&').append(shop.getColorBorder()).append(shop.getBorderV())
                    .append('&').append(shop.getColorId()).append(' ').append(i + 1).append('.')
                    .append('&').append(shop.getColorBracket()).append(" [ ")
                    .append('&').append(shop.getColorName()).append(category.getName().trim())
                    .append('&').append(shop.getColorBracket()).append(" ]");
            text.append(TextUtils.translateText(builder.toString()))
                .append(Text.NEW_LINE);

            if (category.getInfo() != null && !category.getInfo().isEmpty()) {
                builder = new StringBuilder()
                        .append('&').append(shop.getColorBorder()).append(shop.getBorderV())
                        .append('&').append(shop.getColorInfo()).append(' ').append(category.getInfo().trim());

                text.append(TextUtils.translateText(TextUtils.trim(builder.toString(), null)))
                    .append(Text.NEW_LINE);
            }

            builder = new StringBuilder()
                    .append('&').append(shop.getColorBorder()).append(shop.getBorderV());
            text.append(TextUtils.translateText(builder.toString()))
                .append(Text.NEW_LINE);
        }

        return text.build();
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

        StringBuilder builder = new StringBuilder()
                .append('&')
                .append(shop.getColorBorder())
                .append(shop.getBorderV());

        Text.Builder textBuilder = Text.builder()
                                       .append(TextUtils.translateText(builder.toString()))
                                       .append(Text.NEW_LINE);
        int start = (page - 1) * maxEntries;
        for (int i = start; i < start + maxEntries; i++) {
            if (i >= items.size()) {
                break;
            }

            Item item = items.get(i);

            if (item == null) {
                break;
            }

            builder = new StringBuilder()
                    .append('&')
                    .append(shop.getColorBorder())
                    .append(shop.getBorderV())
                    .append('&')
                    .append(shop.getColorId())
                    .append(" ")
                    .append(i + 1)
                    .append(". ")
                    .append('&')
                    .append(shop.getColorName())
                    .append(item.getName().trim())
                    .append('&')
                    .append(shop.getColorBracket())
                    .append(" (")
                    .append('&')
                    .append(shop.getColorPrice())
                    .append(item.getPrice() > 0.0 ? priceFormat.format(item.getPoints()) : "FREE")
                    .append('&')
                    .append(shop.getColorBracket())
                    .append(')');
            textBuilder.
                               append(TextUtils.translateText(builder.toString()))
                       .append(Text.NEW_LINE);

            if (!shop.getSimpleItems().booleanValue()) {
                StringBuilder urlBuilder = new StringBuilder();

                try {
                    URL url = new URL(shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
                    urlBuilder
                            .append('&').append(shop.getColorBorder()).append(shop.getBorderV())
                            .append('&').append(shop.getColorUrl()).append(' ').append(shop.getBuyUrl())
                            .append(item.getId()).append("?player=").append(player.getName());

                    if (urlBuilder.length() > 0) {
                        Text text = Text.builder()
                                        .append(TextUtils.translateText(TextUtils.trim(urlBuilder.toString(), null)))
                                        .onClick(TextActions.openUrl(url))
                                        .build();
                        textBuilder.append(text)
                                   .append(Text.NEW_LINE);
                    }
                } catch (MalformedURLException e) {
                    Enjin.getLogger()
                         .debug("Malformed URL: " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
                }

                StringBuilder descriptionBuilder = new StringBuilder();
                if (item.getInfo() != null && !item.getInfo().isEmpty()) {
                    descriptionBuilder
                            .append('&').append(shop.getColorBorder()).append(shop.getBorderV())
                            .append('&').append(shop.getColorInfo()).append(' ').append(item.getInfo().trim());
                }

                if (descriptionBuilder.length() > 0) {
                    textBuilder.append(TextUtils.translateText(TextUtils.trim(descriptionBuilder.toString(), null)))
                               .append(Text.NEW_LINE);
                }
            }

            builder = new StringBuilder()
                    .append('&').append(shop.getColorBorder()).append(shop.getBorderV());

            textBuilder.append(TextUtils.translateText(builder.toString()))
                       .append(Text.NEW_LINE);
        }

        return textBuilder.build();
    }

    private static Text buildItemContent(Player player, Shop shop, Item item) {
        Text.Builder text = Text.builder();
        StringBuilder builder = new StringBuilder()
                .append('&').append(shop.getColorBorder()).append(shop.getBorderV());
        text.append(TextUtils.translateText(builder.toString()))
            .append(Text.NEW_LINE);

        builder = new StringBuilder()
                .append('&')
                .append(shop.getColorBorder())
                .append(shop.getBorderV())
                .append('&')
                .append(shop.getColorText())
                .append(" Price: ")
                .append('&')
                .append(shop.getColorPrice())
                .append(item.getPrice() > 0.0 ? priceFormat.format(item.getPrice()) : "FREE");
        text.append(TextUtils.translateText(builder.toString()))
            .append(Text.NEW_LINE);

        builder = new StringBuilder()
                .append('&').append(shop.getColorBorder()).append(shop.getBorderV())
                .append('&').append(shop.getColorText()).append(" Info:");
        text.append(TextUtils.translateText(builder.toString()))
            .append(Text.NEW_LINE);

        builder = new StringBuilder()
                .append('&').append(shop.getColorBorder()).append(shop.getBorderV())
                .append('&').append(shop.getColorInfo()).append(' ').append(item.getInfo());
        text.append(TextUtils.translateText(TextUtils.trim(builder.toString(), null)))
            .append(Text.NEW_LINE);

        builder = new StringBuilder()
                .append('&').append(shop.getColorBorder()).append(shop.getBorderV());
        text.append(TextUtils.translateText(builder.toString()))
            .append(Text.NEW_LINE)
            .append(TextUtils.translateText(builder.toString()))
            .append(Text.builder(" Click on the following link to checkout:").color(TextColors.RESET).build())
            .append(Text.NEW_LINE);

        try {
            URL url = new URL(shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
            builder = new StringBuilder()
                    .append('&').append(shop.getColorBorder()).append(shop.getBorderV())
                    .append('&').append(shop.getColorUrl()).append(' ').append(shop.getBuyUrl()).append(item.getId())
                    .append("?player=").append(player.getName());

            if (builder.length() > 0) {
                Text t = Text.builder()
                             .append(TextUtils.translateText(TextUtils.trim(builder.toString(), null)))
                             .onClick(TextActions.openUrl(url))
                             .build();
                text.append(t)
                    .append(Text.NEW_LINE);
            }
        } catch (MalformedURLException e) {
            Enjin.getLogger()
                 .debug("Malformed URL: " + shop.getBuyUrl() + item.getId() + "?player=" + player.getName());
        }

        builder = new StringBuilder()
                .append('&').append(shop.getColorBorder()).append(shop.getBorderV());
        text.append(TextUtils.translateText(builder.toString()))
            .append(Text.NEW_LINE);

        return text.build();
    }

    private static Text buildFooterInfo(Shop shop) {
        StringBuilder builder = new StringBuilder()
                .append('&').append(shop.getColorBorder()).append(shop.getBorderV())
                .append('&').append(shop.getColorText()).append(" Type /")
                .append(Enjin.getConfiguration(EMPConfig.class).getBuyCommand()).append(" to go back");

        return Text.builder().append(TextUtils.translateText(builder.toString())).append(Text.NEW_LINE).build();
    }

    private static Text buildFooter(String title, PlayerShopInstance instance, Shop shop, int page) {
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

        footer.append('&').append(shop.getColorBorder()).append(prefix);

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

            footer.append('&').append(shop.getColorBottom()).append(' ').append(title).append(' ')
                  .append('&').append(shop.getColorBorder()).append(separator).append(' ')
                  .append('&').append(shop.getColorBottom()).append(pagination).append(' ')
                  .append('&').append(shop.getColorBorder());
        }

        for (int i = 0; i < 40; i++) {
            footer.append(shop.getBorderH());
        }

        return Text.builder().append(TextUtils.translateText(TextUtils.trim(footer.toString(), null))).build();
    }
}
