package com.enjin.officialplugin.shop;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.data.Category;
import com.enjin.officialplugin.shop.data.Shop;
import com.enjin.officialplugin.utils.text.TextUtils;
import com.google.common.collect.Lists;
import com.google.gson.*;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import java.util.Iterator;
import java.util.List;

public class ShopUtil {
    private static Gson gson = new GsonBuilder().create();

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
        TextBuilder builder = Texts.builder();

        if (instance.getActiveShop() == null) {
            sendAvailableShops(player, instance);
        } else {
            if (instance.getActiveCategory() == null) {
                sendAvailableCategories(player, instance, page < 1 ? 1 : page);
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
        Shop shop = instance.getActiveShop();
        TextBuilder builder = Texts.builder()
                .append(buildHeader(shop.getName(), shop))
                .append(buildShopInfo(shop))
                .append(buildCategoryContent(shop, shop.getCategories(), page))
                .append(buildFooterInfo(shop))
                .append(buildFooter("Type /buy page #", shop, page));
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

    private static Text buildShopInfo(Shop shop) {
        StringBuilder builder = new StringBuilder()
                .append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                .append(shop.getBorderV())
                .append(Texts.replaceCodes("&" + shop.getColortext(), '&'))
                .append(" " + shop.getInfo().trim() + "\n")
                .append(Texts.replaceCodes("&" + shop.getColorborder(), '&'))
                .append(shop.getBorderV())
                .append(Texts.replaceCodes("&" + shop.getColortext(), '&'))
                .append(" " + "Prices are in " + shop.getCurrency() + ". Choose a category with ")
                .append(Texts.replaceCodes("&" + shop.getColorbottom(), '&'))
                .append("/buy #")
                .append("\n");

        return Texts.builder(builder.toString()).build();
    }

    private static Text buildCategoryContent(Shop shop, List<Category> categories, int page) {
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
