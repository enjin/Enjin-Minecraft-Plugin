package com.enjin.officialplugin.shop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.enjin.officialplugin.util.GlyphUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.ServerShop.Type;

public class ShopUtils {

    public static int MINECRAFT_CONSOLE_WIDTH = 300;
    public static String FORMATTING_CODE = "\u00A7";
    public static int CONSOLE_HEIGHT = 20;

    static Pattern allTextNoQuotes = Pattern.compile("[^\\\"\\\']*");
    static Pattern alphabetical = Pattern.compile("[a-zA-Z]*");
    static Pattern alphanumeric = Pattern.compile("[a-zA-Z0-9]*");
    static Pattern numeric = Pattern.compile("\\d*");

    public static ArrayList<String> parseHistoryJSON(String json, String playername) {
        ArrayList<String> lines = new ArrayList<String>();
        String topline = ChatColor.GRAY + "+++ " + ChatColor.WHITE + "Purchase history for " + playername +
                ChatColor.GRAY + " +++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
        lines.add(TrimText(topline, null));
        lines.add(ChatColor.GRAY + "+   ");
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser.parse(json);
            if (array.size() > 0) {
                lines.add(ChatColor.GRAY + "+   " + ChatColor.WHITE + "Showing recent purchases:");
                lines.add(ChatColor.GRAY + "+   ");
                int i = 1;
                for (Object oitem : array) {
                    if (oitem instanceof JSONObject) {
                        JSONObject item = (JSONObject) oitem;
                        String itemname = (String) item.get("item_name");
                        String purchasedate = (String) item.get("purchase_date");
                        String expires = (String) item.get("expires");
                        String itemline = "";
                        if (expires.equals("")) {
                            itemline = i + ". " + itemname + " (purchased on " + purchasedate + ")";
                        } else {
                            itemline = i + ". " + itemname + " (purchased on " + purchasedate + ") - " + expires;
                        }
                        String[] itemlines = WrapText(itemline, "+   ", "7", "f", 3);
                        for (String line : itemlines) {
                            lines.add(line);
                        }
                        i++;
                    }
                }
            } else {
                lines.add(ChatColor.GRAY + "+   " + ChatColor.WHITE + "There are no recent purchases to display.");
            }

            lines.add(ChatColor.GRAY + "+   ");
            lines.add(TrimText(ChatColor.GRAY + "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++", null));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return lines;
    }

    public static void loadItemData(ShopItems si, String json) {
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser.parse(json);
            si.clearShopItems();
            for (Object oshop : array) {
                if (oshop instanceof JSONObject) {
                    JSONObject shop = (JSONObject) oshop;
                    Object items = shop.get("items");
                    if (items != null && items instanceof JSONArray
                            && ((JSONArray) items).size() > 0) {
                        for (Object oitem : (JSONArray) items) {
                            JSONObject item = (JSONObject) oitem;
                            ShopItem sitem = new ShopItem((String) item.get("name"),
                                    (String) item.get("id"), getPriceString(item.get("price")),
                                    (String) item.get("info"), getPointsString(item.get("points")));
                            Object options = item.get("variables");
                            if (options != null && options instanceof JSONObject
                                    && ((JSONObject) options).size() > 0) {
                                JSONObject joptions = (JSONObject) options;
                                Set<Map.Entry> optionsset = joptions.entrySet();

                                Iterator<Entry> optionsiterator = optionsset.iterator();
                                while (optionsiterator.hasNext()) {
                                    Entry entry = optionsiterator.next();
                                    JSONObject option = (JSONObject) entry.getValue();
                                    ShopItemOptions soptions = new ShopItemOptions(
                                            (String) option.get("name"),
                                            (String) entry.getKey(),
                                            getPriceString(option.get("pricemin")),
                                            getPriceString(option.get("pricemax")),
                                            getPointsString(option.get("pointsmin")),
                                            getPointsString(option.get("pointsmax")));
                                    soptions.setRequired(getBoolean(option.get("required")));
                                    if (option.get("type") != null && option.get("type") instanceof String) {
                                        soptions.setType(getOptionType((String) option.get("type")));
                                    }
                                    if (soptions.getType() == ShopItemOptions.Type.MultipleCheckboxes ||
                                            soptions.getType() == ShopItemOptions.Type.MultipleChoice) {
                                        Object optionOptions = item.get("options");
                                        if (optionOptions != null && optionOptions instanceof JSONObject) {
                                            JSONObject joptionOptions = (JSONObject) optionOptions;
                                            Set<Map.Entry> joptionsset = joptionOptions.entrySet();
                                            Iterator<Entry> joptionsiterator = joptionsset.iterator();
                                            while (joptionsiterator.hasNext()) {
                                                Entry jentry = joptionsiterator.next();
                                                JSONObject jjentry = (JSONObject) jentry.getValue();
                                                ShopOptionOptions ssoptions = new ShopOptionOptions(
                                                        (String) jentry.getKey(),
                                                        (String) jjentry.get("name"),
                                                        (String) jjentry.get("value"),
                                                        getPriceString(jjentry.get("price")),
                                                        getPointsString(jjentry.get("points")));
                                                soptions.addOption(ssoptions);
                                            }
                                        }
                                    }
                                    sitem.addOption(soptions);
                                }
                            }
                            si.addShopItem(sitem);
                        }
                    }
                }
            }
        } catch (ParseException e) {
            if (EnjinMinecraftPlugin.config.isDebug()) {
                e.printStackTrace();
            }
        }

    }

    public static PlayerShopsInstance parseShopsJSON(String json) {
        PlayerShopsInstance psi = new PlayerShopsInstance();
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser.parse(json);
            for (Object oshop : array) {
                if (oshop instanceof JSONObject) {
                    JSONObject shop = (JSONObject) oshop;
                    ServerShop newshop = new ServerShop(
                            (String) shop.get("name"));
                    newshop.setBorder_c((String) shop.get("border_c"));
                    newshop.setBorder_h((String) shop.get("border_h"));
                    newshop.setBorder_v((String) shop.get("border_v"));
                    newshop.setBuyurl((String) shop.get("buyurl"));
                    newshop.setColorborder((String) shop.get("colorborder"));
                    newshop.setColorbottom((String) shop.get("colorbottom"));
                    newshop.setColorbracket((String) shop.get("colorbracket"));
                    newshop.setColorid((String) shop.get("colorid"));
                    newshop.setColorinfo((String) shop.get("colorinfo"));
                    newshop.setColorname((String) shop.get("colorname"));
                    newshop.setColorprice((String) shop.get("colorprice"));
                    newshop.setColortext((String) shop.get("colortext"));
                    newshop.setColortitle((String) shop.get("colortitle"));
                    newshop.setColorurl((String) shop.get("colorurl"));
                    newshop.setCurrency((String) shop.get("currency"));
                    newshop.setInfo((String) shop.get("info"));
                    Boolean simpleitems = (Boolean) shop.get("simpleitems");
                    if (simpleitems != null) {
                        newshop.setSimpleItems(simpleitems);
                    }
                    Boolean simplecategories = (Boolean) shop
                            .get("simplecategories");
                    if (simplecategories != null) {
                        newshop.setSimplecategories(simplecategories);
                    }
                    Object items = shop.get("items");
                    Object categories = shop.get("categories");
                    if (items != null && items instanceof JSONArray
                            && ((JSONArray) items).size() > 0) {
                        addItems(newshop, (JSONArray) items);
                    } else if (categories != null
                            && categories instanceof JSONArray
                            && ((JSONArray) categories).size() > 0) {
                        addCategories(newshop, (JSONArray) categories);
                    }
                    if (newshop.getItems().size() > 0) {
                        psi.addServerShop(newshop);
                    }
                }
            }
        } catch (ParseException e) {
            //e.printStackTrace();
        }
        return psi;
    }

    public static boolean getBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        } else if (obj instanceof String) {
            return (((String) obj).equalsIgnoreCase("true") || obj.equals("1"));
        } else if (obj instanceof Integer) {
            return (((Integer) obj) == 1);
        }
        return false;
    }

    private static void addCategories(ShopItemAdder shop, JSONArray shopcategories) {
        shop.setType(ServerShop.Type.Category);
        for (Object ocategory : shopcategories) {
            JSONObject category = (JSONObject) ocategory;
            ShopCategory scategory = new ShopCategory(
                    (String) category.get("name"), (String) category.get("id"));
            scategory.setParentCategory(shop);
            try {
                String mat = (String) category.get("icon_item");
                Material material = Material.getMaterial(mat.toUpperCase());
                scategory.setMaterial(material);
                int damage = Integer.parseInt((String) category.get("icon_damage"));
                scategory.setMaterialDamage((short) damage);
            } catch (Exception e) {

            }
            scategory.setInfo((String) category.get("info"));
            Object items = category.get("items");
            Object categories = category.get("categories");
            if (items != null && items instanceof JSONArray
                    && ((JSONArray) items).size() > 0) {
                addItems(scategory, (JSONArray) items);
            } else if (categories != null && categories instanceof JSONArray
                    && ((JSONArray) categories).size() > 0) {
                addCategories(scategory, (JSONArray) categories);
            }
            // Don't list empty categories
            if (scategory.getItems().size() > 0) {
                try {
                    shop.addItem(scategory);
                } catch (ItemTypeNotSupported e) {
                    //e.printStackTrace();
                }
            }
        }
    }


    //Real one used to populate stuff, other one is used for other things.
    private static void addItems(ShopItemAdder shop, JSONArray shopitems) {
        shop.setType(ServerShop.Type.Item);
        for (Object oitem : shopitems) {
            JSONObject item = (JSONObject) oitem;
            ShopItem sitem = new ShopItem((String) item.get("name"),
                    (String) item.get("id"), getPriceString(item.get("price")),
                    (String) item.get("info"), getPointsString(item.get("points")));

            try {
                String mat = (String) item.get("icon_item");
                Material material = Material.getMaterial(mat.toUpperCase());
                sitem.setMaterial(material);
                int damage = Integer.parseInt((String) item.get("icon_damage"));
                sitem.setMaterialDamage((short) damage);
            } catch (Exception e) {

            }
            Object options = item.get("variables");
            if (options != null && options instanceof JSONObject
                    && ((JSONObject) options).size() > 0) {
                JSONObject joptions = (JSONObject) options;
                Set<Map.Entry> optionsset = joptions.entrySet();
                Iterator<Entry> optionsiterator = optionsset.iterator();
                while (optionsiterator.hasNext()) {
                    Entry entry = optionsiterator.next();
                    JSONObject option = (JSONObject) entry.getValue();
                    ShopItemOptions soptions = new ShopItemOptions(
                            (String) option.get("name"),
                            (String) entry.getKey(),
                            getPriceString(option.get("pricemin")),
                            getPriceString(option.get("pricemax")),
                            getPointsString(option.get("pointsmin")),
                            getPointsString(option.get("pointsmax")));
                    if (option.get("type") != null && option.get("type") instanceof String) {
                        soptions.setType(getOptionType((String) option.get("type")));
                    }
                    soptions.setMinLength(getLengthInt(option.get("min_length")));
                    soptions.setMaxLength(getLengthInt(option.get("max_length")));
                    if (option.get("required") != null) {
                        soptions.setRequired(getBoolean(option.get("required")));
                    }
                    soptions.setMaxValue(getLengthInt(option.get("max_value")));
                    soptions.setMinValue(getLengthInt(option.get("min_value")));
                    if (option.get("type") != null && option.get("type") instanceof String) {
                        soptions.setType(getOptionType((String) option.get("type")));
                    }
                    if (soptions.getType() == ShopItemOptions.Type.MultipleCheckboxes ||
                            soptions.getType() == ShopItemOptions.Type.MultipleChoice) {
                        Object optionOptions = option.get("options");
                        if (optionOptions != null && optionOptions instanceof JSONObject) {
                            JSONObject joptionOptions = (JSONObject) optionOptions;
                            Set<Map.Entry> joptionsset = joptionOptions.entrySet();
                            Iterator<Entry> joptionsiterator = joptionsset.iterator();
                            while (joptionsiterator.hasNext()) {
                                Entry jentry = joptionsiterator.next();
                                JSONObject jjentry = (JSONObject) jentry.getValue();
                                ShopOptionOptions ssoptions = new ShopOptionOptions(
                                        (String) jentry.getKey(),
                                        (String) jjentry.get("name"),
                                        (String) jjentry.get("value"),
                                        getPriceString(jjentry.get("price")),
                                        getPointsString(jjentry.get("points")));
                                soptions.addOption(ssoptions);
                            }
                        }
                    }
                    sitem.addOption(soptions);
                }
            }
            try {
                shop.addItem(sitem);
            } catch (ItemTypeNotSupported e) {
                //e.printStackTrace();
            }
        }
    }

    public static ShopItemOptions.Type getOptionType(String type) {
        if (type.equalsIgnoreCase("Multiple choice")) {
            return ShopItemOptions.Type.MultipleChoice;
        } else if (type.equalsIgnoreCase("All text")) {
            return ShopItemOptions.Type.AllText;
        } else if (type.equalsIgnoreCase("All text except quotations")) {
            return ShopItemOptions.Type.AllTextNoQuotes;
        } else if (type.equalsIgnoreCase("Alphanumeric")) {
            return ShopItemOptions.Type.Alphanumeric;
        } else if (type.equalsIgnoreCase("Alphabetical")) {
            return ShopItemOptions.Type.Alphabetical;
        } else if (type.equalsIgnoreCase("Numeric")) {
            return ShopItemOptions.Type.Numeric;
        } else if (type.equalsIgnoreCase("Multiple checkboxes")) {
            return ShopItemOptions.Type.MultipleCheckboxes;
        } else {
            return ShopItemOptions.Type.Undefined;
        }
    }

    public static String getPriceString(Object object) {
        String price = "";
        if (object == null) {
            return price;
        }
        if (object instanceof String) {
            price = (String) object;
        } else if (object instanceof Double || object instanceof Float) {
            if (object instanceof Double) {
                price = Double.toString((Double) object);
            } else {
                price = Float.toString((Float) object);
            }
            if (price.indexOf(".") > -1) {
                String[] split = price.split("\\.");
                if (split.length > 1) {
                    if (split[0].equals("")) {
                        price = "0.";
                    } else {
                        price = split[0] + ".";
                    }
                    if (split[1].length() > 2) {
                        price = price + split[1].substring(0, 2);
                    } else if (split[1].length() == 2) {
                        price = price + split[1];
                    } else if (split[1].length() == 1) {
                        price = price + split[1] + "0";
                    } else if (split[1].length() == 0) {
                        price = split[0] + ".00";
                    }
                } else {
                    price = split[0] + ".00";
                }
            }
        } else if (object instanceof Integer) {
            price = Integer.toString((Integer) object) + ".00";
        }
        return price;
    }

    public static boolean isInputValid(ShopItemOptions option, String input) {
        switch (option.getType()) {
            case Undefined:
            case AllText:
                return true;
            case AllTextNoQuotes:
                return allTextNoQuotes.matcher(input).matches();
            case Alphabetical:
                return alphabetical.matcher(input).matches();
            case Alphanumeric:
                return alphanumeric.matcher(input).matches();
            case Numeric:
                if (numeric.matcher(input).matches()) {
                    if (option.getMaxValue() > -1) {
                        try {
                            int tempint = Integer.parseInt(input.trim());
                            return option.getMinValue() <= tempint && option.getMaxValue() >= tempint;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    } else {
                        return true;
                    }
                }
            case MultipleChoice:
                //TODO: Put in validator against options
                return numeric.matcher(input).matches();
            case MultipleCheckboxes:
                //TODO: Put in validator against options
                return true;
            default:
                return true;
        }
    }

    public static String getPointsString(Object object) {
        String points = "";
        if (object == null) {
            return points;
        }
        if (object instanceof String) {
            points = (String) object;
        } else if (object instanceof Double || object instanceof Float) {
            if (object instanceof Double) {
                points = Double.toString((Double) object);
            } else {
                points = Float.toString((Float) object);
            }
            if (points.indexOf(".") > -1) {
                String[] split = points.split("\\.");
                if (split.length > 1) {
                    if (split[0].equals("")) {
                        points = "0.";
                    } else {
                        points = split[0] + ".";
                    }
                    if (split[1].length() > 2) {
                        points = points + split[1].substring(0, 2);
                    } else if (split[1].length() == 2) {
                        points = points + split[1];
                    } else if (split[1].length() == 1) {
                        points = points + split[1] + "0";
                    } else if (split[1].length() == 0) {
                        points = split[0] + ".00";
                    }
                } else {
                    points = split[0] + ".00";
                }
            }
        } else if (object instanceof Integer) {
            points = Integer.toString((Integer) object) + ".00";
        }
        return points;
    }

    public static int getLengthInt(Object object) {
        int length = -1;
        if (object == null) {
            return length;
        }
        if (object instanceof String) {
            try {
                length = Integer.parseInt((String) object);
            } catch (NumberFormatException e) {

            }
        } else if (object instanceof Double || object instanceof Float) {
            if (object instanceof Double) {
                length = ((Double) object).intValue();
            } else {
                length = ((Float) object).intValue();
            }
        } else if (object instanceof Integer) {
            length = ((Integer) object);
        }
        return length;
    }

    public static int getPointsInt(String points) {
        int tot = 0;
        try {
            if (points.contains(".")) {
                String[] split = points.split(".");
                int ipoints = Integer.parseInt(split[0]);
                tot = ipoints;
            } else {
                int ipoints = Integer.parseInt(points.trim());
                tot = ipoints;
            }
        } catch (NumberFormatException e) {

        }
        return tot;
    }

    public static String formatPoints(String points, boolean pointsstring) {
        String formattedpoints = "";
        if (points.equals("")) {
            formattedpoints = "";
        } else if (points.equals("0.00") || points.equals("0")) {
            formattedpoints = "FREE";
        } else if (points.endsWith(".00")) {
            String[] splitprice = points.split("\\.");
            formattedpoints = splitprice[0];
        } else {
            formattedpoints = points;
        }
        if (pointsstring) {
            try {
                double dpoints = Double.parseDouble(points);
                if (dpoints == 1) {
                    formattedpoints = formattedpoints + " point";
                } else {
                    formattedpoints = formattedpoints + " points";
                }
            } catch (NumberFormatException e) {
                if (formattedpoints.equals("FREE")) {
                    formattedpoints = "0 points";
                }
            }
        }
        return formattedpoints;
    }

    public static String formatPrice(String price, String currency) {
        if (price.equals("")) {
            return "";
        }
        if (price.equals("0.00") || price.equals("0")) {
            return "FREE";
        }
        if (currency.equalsIgnoreCase("USD")) {
            if (price.endsWith(".00")) {
                String[] splitprice = price.split("\\.");
                return "$" + splitprice[0];
            } else {
                return "$" + price;
            }
        } else {
            if (price.endsWith(".00")) {
                String[] splitprice = price.split("\\.");
                return splitprice[0] + " " + currency;
            } else {
                return price + " " + currency;
            }
        }
    }

    public static String formatPoints(ShopItemOptions item) {
        if (item.getMinPoints().equals("0.00") || item.getMinPoints().equals("0")) {
            if (item.getMaxPoints().equals("0.00")
                    || item.getMaxPoints().equals("0")) {
                return "FREE";
            } else {
                return "FREE - " + formatPoints(item.getMaxPoints(), true);
            }
        } else if (item.getMinPoints() == null || item.getMinPoints().equals("")) {
            return formatPoints(item.getMaxPoints(), true);
        } else if (item.getMaxPoints().equals("0.00")
                || item.getMaxPoints().equals("0")) {
            return "FREE";
        } else {
            return formatPoints(item.getMinPoints(), false) + " - "
                    + formatPoints(item.getMaxPoints(), true);
        }
    }

    public static String formatPrice(ShopItemOptions item, String currency) {
        if (item.getMinPrice().equals("0.00") || item.getMinPrice().equals("0")) {
            if (item.getMaxPrice().equals("0.00")
                    || item.getMaxPrice().equals("0")) {
                return "FREE";
            } else {
                return "FREE - " + formatPrice(item.getMaxPrice(), currency);
            }
        } else if (item.getMinPrice() == null || item.getMinPrice().equals("")) {
            return formatPrice(item.getMaxPrice(), currency);
        } else if (item.getMaxPrice().equals("0.00")
                || item.getMaxPrice().equals("0")) {
            return "FREE";
        } else {
            return formatPrice(item.getMinPrice(), currency) + " - "
                    + formatPrice(item.getMaxPrice(), currency);
        }
    }

    public static ArrayList<ArrayList<String>> formatPages(ServerShop shop, ShopItemAdder itemcategory) {
        // This holds the completed pages
        ArrayList<ArrayList<String>> pages = new ArrayList<ArrayList<String>>();
        // This holds the header for each page
        ArrayList<String> header = new ArrayList<String>();
        // This holds the footer for each page
        ArrayList<String> footer = new ArrayList<String>();
        // Special collapsed shop format.
        boolean collapsed = false;
        String verticalborder = "";
        String first4chars = "";
        if (shop.getBorder_c() == null && shop.getBorder_h() == null && shop.getBorder_v() == null) {
            collapsed = true;
            header.add(" ");
            if (itemcategory.getParentCategory() != null && itemcategory.getParentCategory() instanceof ShopCategory) {
                header.add(TrimText(FORMATTING_CODE + shop.getColortitle()
                        + itemcategory.getParentCategory().getName() + " - "
                        + itemcategory.getName(), "..."));
            } else {
                header.add(TrimText(FORMATTING_CODE + shop.getColortitle()
                        + itemcategory.getName(), "..."));
            }
            if (itemcategory.getInfo() != null
                    && !itemcategory.getInfo().trim().equals("")) {
                String[] info = WrapText(itemcategory.getInfo(),
                        shop.getColortext(), 6, false);
                for (String sinfo : info) {
                    header.add(sinfo);
                }
            }
            if (itemcategory.getType() == Type.Category) {
                header.add(FORMATTING_CODE + shop.getColortext()
                        + "Prices are in " + shop.getCurrency()
                        + ". Choose a category with " + FORMATTING_CODE
                        + shop.getColorbottom() + "/"
                        + EnjinMinecraftPlugin.config.getBuyCommand() + " <#>");
            } else if (itemcategory.getType() == Type.Item) {
                header.add(FORMATTING_CODE + shop.getColortext()
                        + "Prices are in " + shop.getCurrency()
                        + ". Click a link or use " + FORMATTING_CODE
                        + shop.getColorbottom() + "/"
                        + EnjinMinecraftPlugin.config.getBuyCommand() + " <#>"
                        + FORMATTING_CODE + shop.getColortext()
                        + " to see details.");
            }
            footer.add(FORMATTING_CODE + shop.getColortext() + "Type /"
                    + EnjinMinecraftPlugin.config.getBuyCommand()
                    + " to go back, or type /ec to enable chat");
        } else {
            // Standard shop format
            StringBuilder topline = new StringBuilder(50);
            first4chars = shop.getBorder_c() + shop.getBorder_h() + shop.getBorder_h() + shop.getBorder_h();
            if (first4chars.length() > 4) {
                first4chars = first4chars.substring(0, 4);
            }
            topline.append(FORMATTING_CODE + shop.getColorborder() + first4chars);
            if (itemcategory.getParentCategory() != null && itemcategory.getParentCategory() instanceof ShopCategory) {
                topline.append("[ " + FORMATTING_CODE + shop.getColortitle()
                        + itemcategory.getParentCategory().getName() + " - "
                        + itemcategory.getName() + FORMATTING_CODE
                        + shop.getColorborder() + " ]");
                String toplinetrimmed = TrimText(topline.toString(), "... "
                        + FORMATTING_CODE + shop.getColorborder() + "]");
                if (!toplinetrimmed.endsWith("... " + FORMATTING_CODE
                        + shop.getColorborder() + "]")) {
                    for (int i = 0; i < 40; i++) {
                        topline.append(shop.getBorder_h());
                    }
                    header.add(TrimText(topline.toString(), null));
                } else {
                    header.add(toplinetrimmed);
                }
            } else {
                topline.append(" " + FORMATTING_CODE + shop.getColortitle()
                        + itemcategory.getName());
                String toplinetrimmed = TrimText(topline.toString(), "...");
                if (!toplinetrimmed.endsWith("...")) {
                    topline.append(FORMATTING_CODE + shop.getColorborder()
                            + " ");
                    for (int i = 0; i < 40; i++) {
                        topline.append(shop.getBorder_h());
                    }
                    header.add(TrimText(topline.toString(), null));
                } else {
                    header.add(toplinetrimmed);
                }
            }
            verticalborder = FORMATTING_CODE + shop.getColorborder()
                    + shop.getBorder_v();
            // Add blank line
            header.add(verticalborder);
            // Add category info
            if (itemcategory.getInfo() != null
                    && !itemcategory.getInfo().trim().equals("")) {
                String[] info = WrapText(itemcategory.getInfo(),
                        shop.getBorder_v(), shop.getColorborder(),
                        shop.getColortext(), 6);
                for (String sinfo : info) {
                    header.add(sinfo);
                }
            }
            // Add help text
            if (shop.getType() == Type.Category) {
                header.add(verticalborder + FORMATTING_CODE
                        + shop.getColortext() + "Prices are in "
                        + shop.getCurrency() + ". Choose a category with "
                        + FORMATTING_CODE + shop.getColorbottom() + "/"
                        + EnjinMinecraftPlugin.config.getBuyCommand() + " <#>");
            } else if (shop.getType() == Type.Item) {
                header.add(verticalborder + FORMATTING_CODE
                        + shop.getColortext() + "Prices are in "
                        + shop.getCurrency() + ". Click a link or use "
                        + FORMATTING_CODE + shop.getColorbottom() + "/"
                        + EnjinMinecraftPlugin.config.getBuyCommand() + " <#>"
                        + FORMATTING_CODE + shop.getColortext()
                        + " to see details.");
            }
            header.add(verticalborder);
            // Set the footer text
            footer.add(verticalborder + FORMATTING_CODE + shop.getColortext()
                    + "Type /" + EnjinMinecraftPlugin.config.getBuyCommand()
                    + " to go back, or type /ec to enable chat");
        }
        // add together the header size, footer size, and the page line and the
        // blank line.
        int usedlines = header.size() + footer.size() + 2;
        int itemlinestaken = 4;
        if (itemcategory.getType() == Type.Category) {
            if (shop.simpleCategoryModeDisplay()) {
                itemlinestaken = 1;
            } else {
                itemlinestaken = 3;
            }
        } else if (itemcategory.getType() == Type.Item) {
            if (shop.simpleItemModeDisplay()) {
                itemlinestaken = 1;
            } else {
                itemlinestaken = 4;
            }
        }
        // let's get how many items can go on a page
        int itemsperpage = (CONSOLE_HEIGHT - usedlines) / itemlinestaken;
        // Let's make sure we can at least display one item.
        if (itemsperpage < 1) {
            itemsperpage = 1;
        }
        int numofpages = itemcategory.getItems().size() / itemsperpage;
        // Let's add an extra page if there is a remainder
        if ((itemcategory.getItems().size() % itemsperpage) > 0) {
            numofpages++;
        }
        int onitem = 0;
        for (int i = 0; i < numofpages; i++) {
            ArrayList<String> currentpage = new ArrayList<String>();
            for (int j = 0; j < header.size(); j++) {
                currentpage.add(header.get(j));
            }
            for (int j = 0; j < itemsperpage
                    && onitem < itemcategory.getItems().size(); j++, onitem++) {
                String[] itemstring;
                if (itemlinestaken == 1) {
                    itemstring = formatSimpleShortItem(shop,
                            itemcategory.getItem(onitem), onitem);
                } else {
                    itemstring = formatShortItem(shop,
                            itemcategory.getItem(onitem), onitem);
                }
                for (int k = 0; k < itemstring.length; k++) {
                    currentpage.add(itemstring[k]);
                }
            }

            // Add whitespace;
            for (int j = currentpage.size(); j < (20 - (footer.size() + 1)); j++) {
                currentpage.add(verticalborder + " ");
            }
            for (int j = 0; j < footer.size(); j++) {
                currentpage.add(footer.get(j));
            }

            // Add in the page numbers, etc.
            if (collapsed) {
                currentpage.add(FORMATTING_CODE + shop.getColorbottom()
                        + "Page " + (i + 1) + " of " + numofpages + ", Type /"
                        + EnjinMinecraftPlugin.config.getBuyCommand() + " page #");
            } else {
                StringBuilder bottomline = new StringBuilder(50);
                bottomline.append(FORMATTING_CODE + shop.getColorborder());
                bottomline.append(first4chars);
                bottomline.append(FORMATTING_CODE + shop.getColorbottom()
                        + " Type /" + EnjinMinecraftPlugin.config.getBuyCommand()
                        + " page # " + FORMATTING_CODE + shop.getColorborder());
                for (int j = 0; j < 40; j++) {
                    bottomline.append(shop.getBorder_h());
                }
                String last2chars = shop.getBorder_h() + shop.getBorder_h();
                if (last2chars.length() > 2) {
                    last2chars = last2chars.substring(0, 2);
                }
                String bottomlinetrimmed = TrimText(bottomline.toString(), null,
                        FORMATTING_CODE + shop.getColorbottom() + "Page " + (i + 1) + " of " + numofpages
                                + FORMATTING_CODE + shop.getColorborder() + last2chars);
                currentpage.add(bottomlinetrimmed);
            }
            pages.add(currentpage);
        }
        return pages;
    }

    public static ArrayList<String> getItemDetailsPage(ServerShop shop, ShopItem item, Player player) {
        ArrayList<String> itempage = new ArrayList<String>();
        boolean collapsed = false;
        String verticalborder;
        String first4chars = "";
        if (shop.getBorder_c() == null && shop.getBorder_h() == null
                && shop.getBorder_v() == null) {
            collapsed = true;
            verticalborder = "";
            itempage.add(" ");
            itempage.add(TrimText(
                    FORMATTING_CODE + shop.getColortitle() + item.getName(),
                    "..."));
        } else {
            first4chars = shop.getBorder_c() + shop.getBorder_h() + shop.getBorder_h() + shop.getBorder_h();
            if (first4chars.length() > 4) {
                first4chars = first4chars.substring(0, 4);
            }
            verticalborder = FORMATTING_CODE + shop.getColorborder() + shop.getBorder_v();
            // Standard shop format
            StringBuilder topline = new StringBuilder(50);
            topline.append(FORMATTING_CODE + shop.getColorborder() + first4chars);
            topline.append(" " + FORMATTING_CODE + shop.getColortitle()
                    + item.getName() + FORMATTING_CODE + shop.getColorborder()
                    + " ");
            String toplinetrimmed = TrimText(topline.toString(), "...");
            if (!toplinetrimmed.endsWith("...")) {
                topline.append(FORMATTING_CODE + shop.getColorborder() + " ");
                for (int i = 0; i < 40; i++) {
                    topline.append(shop.getBorder_h());
                }
                itempage.add(TrimText(topline.toString(), null));
            } else {
                itempage.add(toplinetrimmed);
            }
        }
        itempage.add(verticalborder + " ");
        String formattedprice = formatPrice(item.getPrice(), shop.getCurrency());
        if (!formattedprice.equals("")) {
            itempage.add(verticalborder + FORMATTING_CODE + shop.getColortext()
                    + "Price: " + FORMATTING_CODE + shop.getColorprice()
                    + formattedprice);
        }
        String formattedpoints = formatPoints(item.getPoints(), false);
        if (!formattedpoints.equals("")) {
            itempage.add(verticalborder + FORMATTING_CODE + shop.getColortext()
                    + "Points: " + FORMATTING_CODE + shop.getColorprice()
                    + formattedpoints);
        }
        if (item.getOptions().size() > 0) {
            StringBuilder options = new StringBuilder();
            options.append(FORMATTING_CODE + shop.getColortext() + "Options: ");
            for (int i = 0; i < item.getOptions().size(); i++) {
                if (i > 0) {
                    options.append(", ");
                }
                ShopItemOptions option = item.getOption(i);
                options.append(FORMATTING_CODE + shop.getColorname()
                        + option.getName() + FORMATTING_CODE
                        + shop.getColorbracket() + " (");
                String optionprice = formatPrice(option, shop.getCurrency());
                String optionpoints = formatPoints(option);
                if (!optionprice.equals("") && !optionpoints.equals("")) {
                    options.append(FORMATTING_CODE + shop.getColorprice()
                            + optionprice + " or " + optionpoints
                            + FORMATTING_CODE + shop.getColorbracket() + ")");
                } else if (!optionpoints.equals("")) {
                    options.append(FORMATTING_CODE + shop.getColorprice()
                            + optionpoints
                            + FORMATTING_CODE + shop.getColorbracket() + ")");
                } else {
                    options.append(FORMATTING_CODE + shop.getColorprice() + optionprice + FORMATTING_CODE + shop.getColorbracket() + ")");
                }
            }
            ArrayList<String> optionlines = WrapFormattedText(verticalborder,
                    options.toString());
            for (String optionline : optionlines) {
                itempage.add(optionline);
            }
        }
        itempage.add(verticalborder + FORMATTING_CODE + shop.getColortext() + "Info:");
        String[] infolines;
        if (collapsed) {
            infolines = WrapText(item.getInfo(), shop.getColorinfo(), 10, false);
        } else {
            infolines = WrapText(item.getInfo(), shop.getBorder_v(), shop.getColorborder(), shop.getColorinfo(), 10);
        }
        for (String infoline : infolines) {
            itempage.add(infoline);
        }
        // Add whitespace;
        for (int i = itempage.size(); i < 15; i++) {
            itempage.add(verticalborder + " ");
        }
        if (formattedprice.equals("") && formattedpoints.equals("")) {
            itempage.add(verticalborder + " ");
            itempage.add(verticalborder + " ");
        } else {
            itempage.add(verticalborder + FORMATTING_CODE + shop.getColortext() + "Click the following link to checkout:");
            itempage.add(verticalborder + FORMATTING_CODE + shop.getColorurl() + shop.getBuyurl() + item.getId() + "?player=" + player.getName());

        }
        itempage.add(verticalborder + " ");
        itempage.add(verticalborder + FORMATTING_CODE + shop.getColortext() + "Type /" + EnjinMinecraftPlugin.config.getBuyCommand()
                + " to go back, or type /ec to enable chat");
        if (collapsed) {
            if (formattedpoints.equals("")) {
                itempage.add(FORMATTING_CODE + shop.getColorbottom() + "Click the item link to purchase");
            } else {
                itempage.add(FORMATTING_CODE + shop.getColorbottom() + "Click the item link or type /" + EnjinMinecraftPlugin.config.getBuyCommand() + " item");
            }
        } else {
            // Standard shop format
            StringBuilder bottomline = new StringBuilder(50);
            bottomline.append(FORMATTING_CODE + shop.getColorborder() + first4chars);

            if (formattedpoints.equals("")) {
                bottomline.append(" " + FORMATTING_CODE + shop.getColorbottom() + "Click the item link to purchase" + FORMATTING_CODE + shop.getColorborder() + " ");
            } else {
                bottomline.append(" " + FORMATTING_CODE + shop.getColorbottom() + "Click the item link or type /" + EnjinMinecraftPlugin.config.getBuyCommand() + " item" + FORMATTING_CODE + shop.getColorborder() + " ");
            }

            bottomline.append(FORMATTING_CODE + shop.getColorborder() + " ");
            for (int i = 0; i < 40; i++) {
                bottomline.append(shop.getBorder_h());
            }
            itempage.add(TrimText(bottomline.toString(), null));
        }

        return itempage;
    }

    public static ArrayList<String> getShopListing(PlayerShopsInstance shops) {
        ArrayList<String> shopoutput = new ArrayList<String>();
        shopoutput.add(ChatColor.WHITE + "=== Choose Shop ===");
        shopoutput.add(ChatColor.WHITE + "Please type " + ChatColor.YELLOW
                + "/" + EnjinMinecraftPlugin.config.getBuyCommand() + " shop <#>");
        shopoutput.add(" ");
        for (int i = 0; i < shops.getServerShops().size() && i < 15; i++) {
            shopoutput.add(TrimText(ChatColor.YELLOW + String.valueOf(i + 1)
                    + ". " + shops.getServerShop(i).getName(), "..."));
        }
        shopoutput.add(" ");
        shopoutput.add(ChatColor.WHITE + "Type /ec to enable chat");
        return shopoutput;
    }

    public static ArrayList<String> WrapFormattedText(String prefix, String text) {
        ArrayList<String> output = new ArrayList<String>();
        String fullline = text;
        if (getWidth(prefix + fullline) > MINECRAFT_CONSOLE_WIDTH) {
            int index = 0;
            while (index < fullline.length() - 1) {
                String line = fullline.substring(index);
                while (getWidth(prefix + line) > MINECRAFT_CONSOLE_WIDTH) {
                    if (line.lastIndexOf(' ') > 0) {
                        line = line.substring(0, line.lastIndexOf(' '));
                    } else {
                        line = line.substring(0, line.length() - 1);
                    }
                }
                if (index > 0) {
                    int lastformattingcode = fullline.lastIndexOf(
                            FORMATTING_CODE, index);
                    String textcolor = fullline.substring(
                            lastformattingcode + 1, lastformattingcode + 2);
                    output.add(prefix + FORMATTING_CODE + textcolor + line);
                } else {
                    output.add(prefix + line);
                }
                index += line.length();
            }
        } else {
            output.add(prefix + text);
        }
        return output;
    }

    public static int getWidth(String text) {
        if (text == null) {
            return 0;
        }

        String cleanedtext = ChatColor.stripColor(text);
        cleanedtext = cleanedtext.replace(FORMATTING_CODE, "");

        return GlyphUtil.getTextWidth(cleanedtext) + (cleanedtext.length() - 1);
    }

    public static String TrimText(String text, String ellipses) {
        String trimmedtext = text;
        if (getWidth(trimmedtext) > MINECRAFT_CONSOLE_WIDTH) {
            trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
            if (ellipses != null) {
                while (getWidth(trimmedtext + ellipses) > MINECRAFT_CONSOLE_WIDTH) {
                    trimmedtext = trimmedtext.substring(0,
                            trimmedtext.length() - 1);
                }
                return trimmedtext + ellipses;
            } else {
                while (getWidth(trimmedtext) > MINECRAFT_CONSOLE_WIDTH) {
                    trimmedtext = trimmedtext.substring(0,
                            trimmedtext.length() - 1);
                }
                return trimmedtext;
            }
        } else {
            return text;
        }
    }

    public static String TrimText(String text, String ellipses, String suffix) {
        String trimmedtext = text;
        if (getWidth(trimmedtext + suffix) > MINECRAFT_CONSOLE_WIDTH) {
            trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
            if (ellipses != null) {
                while (getWidth(trimmedtext + ellipses + suffix) > MINECRAFT_CONSOLE_WIDTH) {
                    trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
                }
                return trimmedtext + ellipses + suffix;
            } else {
                while (getWidth(trimmedtext + suffix) > MINECRAFT_CONSOLE_WIDTH) {
                    trimmedtext = trimmedtext.substring(0,
                            trimmedtext.length() - 1);
                }
                return trimmedtext + suffix;
            }
        } else {
            return text + suffix;
        }
    }

    public static String[] formatShortItem(ServerShop shop, AbstractShopSuperclass item, int itemnumber) {
        if (item instanceof ShopItem) {
            return formatShortItem(shop, (ShopItem) item, itemnumber);
        } else if (item instanceof ShopCategory) {
            return formatShortItem(shop, (ShopCategory) item, itemnumber);
        } else {
            return new String[0];
        }
    }

    public static String[] formatShortItem(ServerShop shop, ShopItem item, int itemnumber) {
        String verticalborder = "";
        if (shop.getBorder_v() != null && !shop.getBorder_v().equals("")) {
            verticalborder = FORMATTING_CODE + shop.getColorborder()
                    + shop.getBorder_v();
        }
        String[] itemstring = new String[4];
        String formattedprice = formatPrice(item.getPrice(), shop.getCurrency());
        String formattedpoints = formatPoints(item.getPoints(), true);
        itemstring[0] = verticalborder + FORMATTING_CODE + shop.getColorid()
                + (itemnumber + 1) + ". " + FORMATTING_CODE
                + shop.getColorname() + item.getName();
        if (!formattedprice.equals("") && !formattedpoints.equals("")) {
            itemstring[0] = TrimText(
                    itemstring[0],
                    "...",
                    FORMATTING_CODE + shop.getColorbracket() + " ("
                            + FORMATTING_CODE + shop.getColorprice()
                            + formattedprice + " or " + formattedpoints + FORMATTING_CODE
                            + shop.getColorbracket() + ")");
        } else if (!formattedpoints.equals("")) {
            itemstring[0] = TrimText(
                    itemstring[0],
                    "...",
                    FORMATTING_CODE + shop.getColorbracket() + " ("
                            + FORMATTING_CODE + shop.getColorprice()
                            + formattedpoints + FORMATTING_CODE
                            + shop.getColorbracket() + ")");
        } else if (!formattedprice.equals("")) {
            itemstring[0] = TrimText(
                    itemstring[0],
                    "...",
                    FORMATTING_CODE + shop.getColorbracket() + " ("
                            + FORMATTING_CODE + shop.getColorprice()
                            + formattedprice + FORMATTING_CODE
                            + shop.getColorbracket() + ")");
        } else {
            itemstring[0] = TrimText(itemstring[0], "...", "");
        }
        String[] fulldescription = item.getInfo().split("\n");
        String description = fulldescription[0];
        itemstring[1] = verticalborder + FORMATTING_CODE + shop.getColorurl()
                + shop.getBuyurl() + item.getId();
        if (fulldescription.length > 1) {
            itemstring[2] = TrimText(
                    verticalborder + FORMATTING_CODE + shop.getColorinfo()
                            + description, "", "...");
        } else {
            itemstring[2] = TrimText(
                    verticalborder + FORMATTING_CODE + shop.getColorinfo()
                            + description, "...");
        }
        itemstring[3] = verticalborder;
        return itemstring;
    }

    public static String[] formatShortItem(ServerShop shop, ShopCategory item, int itemnumber) {
        String verticalborder = "";
        if (shop.getBorder_v() != null && !shop.getBorder_v().equals("")) {
            verticalborder = FORMATTING_CODE + shop.getColorborder()
                    + shop.getBorder_v();
        }
        String[] itemstring = new String[3];
        itemstring[0] = verticalborder + FORMATTING_CODE + shop.getColorid()
                + (itemnumber + 1) + ". " + FORMATTING_CODE
                + shop.getColorbracket() + "[ " + FORMATTING_CODE
                + shop.getColorname() + item.getName() + FORMATTING_CODE
                + shop.getColorbracket() + " ]";
        itemstring[0] = TrimText(itemstring[0],
                "..." + FORMATTING_CODE + shop.getColorbracket() + " ]");
        String description = item.getInfo().split("\n")[0];
        itemstring[1] = TrimText(
                verticalborder + FORMATTING_CODE + shop.getColorinfo()
                        + description, "...");
        itemstring[2] = verticalborder;
        return itemstring;
    }

    public static String[] formatSimpleShortItem(ServerShop shop, AbstractShopSuperclass item, int itemnumber) {
        if (item instanceof ShopItem) {
            return formatSimpleShortItem(shop, (ShopItem) item, itemnumber);
        } else if (item instanceof ShopCategory) {
            return formatSimpleShortItem(shop, (ShopCategory) item, itemnumber);
        } else {
            return new String[0];
        }
    }

    public static String[] formatSimpleShortItem(ServerShop shop, ShopItem item, int itemnumber) {
        String verticalborder = "";
        if (shop.getBorder_v() != null && !shop.getBorder_v().equals("")) {
            verticalborder = FORMATTING_CODE + shop.getColorborder()
                    + shop.getBorder_v();
        }
        String[] itemstring = new String[1];
        String formattedprice = formatPrice(item.getPrice(), shop.getCurrency());
        String formattedpoints = formatPoints(item.getPoints(), true);
        itemstring[0] = verticalborder + FORMATTING_CODE + shop.getColorid()
                + (itemnumber + 1) + ". " + FORMATTING_CODE + shop.getColorname() + item.getName();
        if (!formattedprice.equals("") && !formattedpoints.equals("")) {
            itemstring[0] = TrimText(itemstring[0], "...", FORMATTING_CODE + shop.getColorbracket() + " ("
                    + FORMATTING_CODE + shop.getColorprice() + formattedprice + " or " + formattedpoints + FORMATTING_CODE + shop.getColorbracket() + ")");
        } else if (!formattedpoints.equals("")) {
            itemstring[0] = TrimText(itemstring[0], "...", FORMATTING_CODE + shop.getColorbracket() + " ("
                    + FORMATTING_CODE + shop.getColorprice() + formattedpoints + FORMATTING_CODE + shop.getColorbracket() + ")");
        } else if (!formattedprice.equals("")) {
            itemstring[0] = TrimText(itemstring[0], "...", FORMATTING_CODE + shop.getColorbracket() + " ("
                    + FORMATTING_CODE + shop.getColorprice() + formattedprice + FORMATTING_CODE + shop.getColorbracket() + ")");
        } else {
            itemstring[0] = TrimText(itemstring[0], "...", "");
        }
        return itemstring;
    }

    public static String[] formatSimpleShortItem(ServerShop shop, ShopCategory item, int itemnumber) {
        String verticalborder = "";
        if (shop.getBorder_v() != null && !shop.getBorder_v().equals("")) {
            verticalborder = FORMATTING_CODE + shop.getColorborder()
                    + shop.getBorder_v();
        }
        String[] itemstring = new String[1];
        itemstring[0] = verticalborder + FORMATTING_CODE + shop.getColorid() + (itemnumber + 1) + ". " + FORMATTING_CODE
                + shop.getColorbracket() + "[ " + FORMATTING_CODE + shop.getColorname() + item.getName() + FORMATTING_CODE
                + shop.getColorbracket() + " ]";
        itemstring[0] = TrimText(itemstring[0],
                "..." + FORMATTING_CODE + shop.getColorbracket() + " ]");
        return itemstring;
    }

    public static String[] WrapText(String text, String prefix, String prefixcolor, String textcolor, int numlines) {
        String[] lines = text.split("\n");
        ArrayList<String> formattedlines = new ArrayList<String>();
        for (int i = 0; i < lines.length && formattedlines.size() < numlines; i++) {
            String fullline = lines[i];
            if (getWidth(prefix + fullline) + ((prefix + fullline).length()) > MINECRAFT_CONSOLE_WIDTH) {
                int index = 0;
                while (index < fullline.length() - 1
                        && formattedlines.size() < 6) {
                    String line = fullline.substring(index);
                    if (formattedlines.size() == numlines - 1 && i < lines.length - 1) {
                        while (getWidth(prefix + line + "...") > MINECRAFT_CONSOLE_WIDTH) {
                            if (line.lastIndexOf(' ') > 0) {
                                line = line.substring(0, line.lastIndexOf(' '));
                            } else {
                                line = line.substring(0, line.length() - 1);
                            }
                        }
                        line = line + "...";
                    } else {
                        while (getWidth(prefix + line) > MINECRAFT_CONSOLE_WIDTH) {
                            if (line.lastIndexOf(' ') > 0) {
                                line = line.substring(0, line.lastIndexOf(' '));
                            } else {
                                line = line.substring(0, line.length() - 1);
                            }
                        }
                    }
                    formattedlines.add(FORMATTING_CODE + prefixcolor + prefix
                            + FORMATTING_CODE + textcolor + line);
                    index += line.length();
                }
            } else {
                formattedlines.add(FORMATTING_CODE + prefixcolor + prefix
                        + FORMATTING_CODE + textcolor + fullline);
            }
        }
        String[] returnarray = new String[formattedlines.size()];
        for (int i = 0; i < returnarray.length; i++) {
            returnarray[i] = formattedlines.get(i);
        }
        return returnarray;
    }

    public static String[] WrapText(String text, String textcolor, int numlines, boolean halfwidth) {
        int width = MINECRAFT_CONSOLE_WIDTH;
        if (halfwidth) {
            width = MINECRAFT_CONSOLE_WIDTH / 2;
        }
        String[] lines = text.split("\n");
        ArrayList<String> formattedlines = new ArrayList<String>();
        for (int i = 0; i < lines.length && formattedlines.size() < 6; i++) {
            String fullline = lines[i];
            if (getWidth(fullline) > width) {
                int index = 0;
                while (index < fullline.length() - 1
                        && formattedlines.size() < numlines) {
                    String line = fullline.substring(index);
                    if (formattedlines.size() == numlines - 1 && i < lines.length - 1) {
                        while (getWidth(line + "...") > width) {
                            if (line.lastIndexOf(' ') > 0) {
                                line = line.substring(0, line.lastIndexOf(' '));
                            } else {
                                line = line.substring(0, line.length() - 1);
                            }
                        }
                        line = line + "...";
                    } else {
                        while (getWidth(line) > width) {
                            if (line.lastIndexOf(' ') > 0) {
                                line = line.substring(0, line.lastIndexOf(' '));
                            } else {
                                line = line.substring(0, line.length() - 1);
                            }
                        }
                    }
                    formattedlines.add(FORMATTING_CODE + textcolor + line);
                    index += line.length();
                }
            } else {
                formattedlines.add(FORMATTING_CODE + textcolor + fullline);
            }
        }
        String[] returnarray = new String[formattedlines.size()];
        for (int i = 0; i < returnarray.length; i++) {
            returnarray[i] = formattedlines.get(i);
        }
        return returnarray;
    }
}
