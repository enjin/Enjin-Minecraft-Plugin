package com.enjin.officialplugin.shop;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.map.MinecraftFont;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ShopUtils {
	
	public static int MINECRAFT_CONSOLE_WIDTH = 648;
	public static String FORMATTING_CODE = "\u00A7";
	
	public static PlayerShopsInstance parseShopsJSON (String json) {
		PlayerShopsInstance psi = new PlayerShopsInstance();
		JSONParser parser = new JSONParser();
		try {
			JSONArray array = (JSONArray) parser.parse(json);
			for(Object oshop : array) {
				if(oshop instanceof JSONObject) {
					JSONObject shop = (JSONObject) oshop;
					ServerShop newshop = new ServerShop((String) shop.get("name"));
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
					Object items = shop.get("items");
					Object categories = shop.get("categories");
					if(items != null && items instanceof JSONArray && ((JSONArray)items).size() > 0) {
						addItems(newshop, (JSONArray) items);
					}else if(categories != null && categories instanceof JSONArray && ((JSONArray)categories).size() > 0) {
						addCategories(newshop, (JSONArray) categories);
					}
					psi.addServerShop(newshop);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return psi;
	}
	
	private static void addCategories(ShopItemAdder shop, JSONArray shopcategories) {
		shop.setType(ServerShop.Type.Category);
		for(Object ocategory : shopcategories) {
			JSONObject category = (JSONObject) ocategory;
			ShopCategory scategory = new ShopCategory((String) category.get("name"),(String) category.get("id"));
			scategory.setInfo((String) category.get("info"));
			Object items = category.get("items");
			Object categories = category.get("categories");
			if(items != null && items instanceof JSONArray && ((JSONArray)items).size() > 0) {
				addItems(scategory, (JSONArray) items);
			}else if(categories != null && categories instanceof JSONArray && ((JSONArray)categories).size() > 0) {
				addCategories(scategory, (JSONArray) categories);
			}
			try {
				shop.addItem(scategory);
			} catch (ItemTypeNotSupported e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void addItems(ShopItemAdder shop, JSONArray shopitems) {
		shop.setType(ServerShop.Type.Item);
		for(Object oitem : shopitems) {
			JSONObject item = (JSONObject) oitem;
			ShopItem sitem = new ShopItem((String) item.get("name"), (String) item.get("id"), (String) item.get("price"), (String) item.get("info"));
			Object options = item.get("variables");
			if(options != null && options instanceof JSONArray && ((JSONArray)options).size() > 0) {
				JSONArray joptions = (JSONArray) options;
				@SuppressWarnings("rawtypes")
				Iterator optionsiterator = joptions.iterator();
				while(optionsiterator.hasNext()) {
					JSONObject option = (JSONObject) optionsiterator.next();
					ShopItemOptions soptions = new ShopItemOptions((String) option.get("name"), (String) option.get("pricemin"), (String) option.get("pricemax"));
					sitem.addOption(soptions);
				}
			}
			try {
				shop.addItem(sitem);
			} catch (ItemTypeNotSupported e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static String formatPrice(ShopItem item, String currency, String parentshops) {
		if(item.getPrice().equals("0.00")) {
			return "FREE";
		}
		if(currency.equalsIgnoreCase("USD")) {
			if(item.getPrice().endsWith(".00")) {
				String[] splitprice = item.getPrice().split(".");
				return "$" + splitprice[0];
			}else {
				return "$" + item.getPrice();
			}
		}else {
			if(item.getPrice().endsWith(".00")) {
				String[] splitprice = item.getPrice().split(".");
				return splitprice[0] + " " + currency;
			}else {
				return item.getPrice() + " " + currency;
			}
		}
	}
	
	public static String[] formatPage(ServerShop shop, ShopItemAdder itemcategory, String parentshops, int page) {
		String[] output = new String[20];
		//Special collapsed shop format.
		if(shop.getBorder_c() == null && shop.getBorder_h() == null && shop.getBorder_v() == null) {
			output[0] = " ";
			output[1] = TrimText(parentshops + " - " + itemcategory.getName(), true);
			//TODO: add rest of function.
		}
		return output;
	}
	
	public static String TrimText(String text, boolean ellipses) {
		String trimmedtext = text;
		if(MinecraftFont.Font.getWidth(trimmedtext) + (trimmedtext.length()-1) > MINECRAFT_CONSOLE_WIDTH) {
			trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
			if(ellipses) {
				while(MinecraftFont.Font.getWidth(trimmedtext + "...") + (trimmedtext.length())> MINECRAFT_CONSOLE_WIDTH) {
					trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
				}
				return trimmedtext + "...";
			}else {
				while(MinecraftFont.Font.getWidth(trimmedtext) + (trimmedtext.length()-1) > MINECRAFT_CONSOLE_WIDTH) {
					trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
				}
				return trimmedtext;
			}
		}else {
			return text;
		}
	}
	
	public static String[] WrapText(String text, String prefix, String prefixcolor, String textcolor) {
		String[] lines = text.split("\n");
		ArrayList<String> formattedlines = new ArrayList<String>();
		for(int i = 0; i < lines.length && formattedlines.size() < 6; i++) {
			String fullline = lines[i];
			if(MinecraftFont.Font.getWidth(prefix + fullline) + ((prefix + fullline).length()) > MINECRAFT_CONSOLE_WIDTH) {
				int index = 0;
				while(index < fullline.length() -1 && formattedlines.size() < 6) {
					String line = fullline.substring(index);
					while(MinecraftFont.Font.getWidth(prefix + line) + ((prefix + line).length()) > MINECRAFT_CONSOLE_WIDTH) {
						if(line.lastIndexOf(' ') > 0) {
							line = line.substring(0, line.lastIndexOf(' '));
						}else {
							line = line.substring(0, line.length() - 1);
						}
					}
					formattedlines.add(FORMATTING_CODE + prefixcolor + prefix + FORMATTING_CODE + textcolor + line);
					index += line.length();
				}
			}
		}
		return (String[]) formattedlines.toArray();
	}
	
	public static String[] WrapText(String text, String textcolor) {
		String[] lines = text.split("\n");
		ArrayList<String> formattedlines = new ArrayList<String>();
		for(int i = 0; i < lines.length && formattedlines.size() < 6; i++) {
			String fullline = lines[i];
			if(MinecraftFont.Font.getWidth(fullline) + (fullline.length()) > MINECRAFT_CONSOLE_WIDTH) {
				int index = 0;
				while(index < fullline.length() -1 && formattedlines.size() < 6) {
					String line = fullline.substring(index);
					while(MinecraftFont.Font.getWidth(line) + (line.length()) > MINECRAFT_CONSOLE_WIDTH) {
						if(line.lastIndexOf(' ') > 0) {
							line = line.substring(0, line.lastIndexOf(' '));
						}else {
							line = line.substring(0, line.length() - 1);
						}
					}
					formattedlines.add(FORMATTING_CODE + textcolor + line);
					index += line.length();
				}
			}
		}
		return (String[]) formattedlines.toArray();
	}
}
