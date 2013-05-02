package com.enjin.officialplugin.shop;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.map.MinecraftFont;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.ServerShop.Type;

public class ShopUtils {
	
	public static int MINECRAFT_CONSOLE_WIDTH = 310;
	public static String FORMATTING_CODE = "\u00A7";
	public static int CONSOLE_HEIGHT = 20;
	
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
					Boolean simpleitems = (Boolean) shop.get("simpleitems");
					if(simpleitems != null) {
						newshop.setSimpleItems(simpleitems);
					}
					Boolean simplecategories = (Boolean) shop.get("simplecategories");
					if(simplecategories != null) {
						newshop.setSimplecategories(simplecategories);
					}
					Object items = shop.get("items");
					Object categories = shop.get("categories");
					if(items != null && items instanceof JSONArray && ((JSONArray)items).size() > 0) {
						addItems(newshop, (JSONArray) items);
					}else if(categories != null && categories instanceof JSONArray && ((JSONArray)categories).size() > 0) {
						addCategories(newshop, (JSONArray) categories);
					}
					if(newshop.getItems().size() > 0) {
						psi.addServerShop(newshop);
					}
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
			if(shop instanceof ShopCategory) {
				scategory.setParentCategory(shop);
			}
			scategory.setInfo((String) category.get("info"));
			Object items = category.get("items");
			Object categories = category.get("categories");
			if(items != null && items instanceof JSONArray && ((JSONArray)items).size() > 0) {
				addItems(scategory, (JSONArray) items);
			}else if(categories != null && categories instanceof JSONArray && ((JSONArray)categories).size() > 0) {
				addCategories(scategory, (JSONArray) categories);
			}
			//Don't list empty categories
			if(scategory.getItems().size() > 0) {
				try {
					shop.addItem(scategory);
				} catch (ItemTypeNotSupported e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	
	public static String formatPrice(String price, String currency) {
		if(price.equals("0.00")) {
			return "FREE";
		}
		if(currency.equalsIgnoreCase("USD")) {
			if(price.endsWith(".00")) {
				String[] splitprice = price.split("\\.");
				return "$" + splitprice[0];
			}else {
				return "$" + price;
			}
		}else {
			if(price.endsWith(".00")) {
				String[] splitprice = price.split("\\.");
				return splitprice[0] + " " + currency;
			}else {
				return price + " " + currency;
			}
		}
	}
	
	public static String formatPrice(ShopItemOptions item, String currency) {
		
		if(item.getMinPrice().equals("0.00") || item.getMinPrice().equals("0")) {
			if(item.getMaxPrice().equals("0.00") || item.getMaxPrice().equals("0")) {
				return "FREE";
			}else {
				return "FREE - " + formatPrice(item.getMaxPrice(), currency);
			}
		}else if(item.getMinPrice() == null || item.getMinPrice().equals("")) {
			return formatPrice(item.getMaxPrice(), currency);
		}else if(item.getMaxPrice().equals("0.00") || item.getMaxPrice().equals("0")) {
			return "FREE";
		}else {
			return formatPrice(item.getMinPrice(), currency) + " - " + formatPrice(item.getMaxPrice(), currency);
		}
	}
	
	public static ArrayList<ArrayList<String>> formatPages(ServerShop shop, ShopItemAdder itemcategory) {
		//This holds the completed pages
		ArrayList<ArrayList<String>> pages = new ArrayList<ArrayList<String>>();
		//This holds the header for each page
		ArrayList<String> header = new ArrayList<String>();
		//This holds the footer for each page
		ArrayList<String> footer = new ArrayList<String>();
		//Special collapsed shop format.
		boolean collapsed = false;
		String verticalborder = "";
		if(shop.getBorder_c() == null && shop.getBorder_h() == null && shop.getBorder_v() == null) {
			collapsed = true;
			header.add(" ");
			if(itemcategory.getParentCategory() != null) {
				header.add(TrimText(FORMATTING_CODE + shop.getColortitle() + itemcategory.getParentCategory().getName() + " - " + itemcategory.getName(), "..."));
			}else {
				header.add(TrimText(FORMATTING_CODE + shop.getColortitle() + itemcategory.getName(), "..."));
			}
			if(itemcategory.getInfo() != null && !itemcategory.getInfo().trim().equals("")) {
				String[] info = WrapText(itemcategory.getInfo(), shop.getColortext());
				for(String sinfo : info) {
					header.add(sinfo);
				}
			}
			if(itemcategory.getType() == Type.Category) {
				header.add(FORMATTING_CODE + shop.getColortext() + "Prices are in " + shop.getCurrency() + ". Choose a category with " + FORMATTING_CODE + shop.getColorbottom() + "/" + EnjinMinecraftPlugin.BUY_COMMAND + " <#>");
			}else if(itemcategory.getType() == Type.Item) {
				header.add(FORMATTING_CODE + shop.getColortext() + "Prices are in " + shop.getCurrency() + ". Click a link or use " + FORMATTING_CODE + shop.getColorbottom() + "/" + EnjinMinecraftPlugin.BUY_COMMAND + " <#>" + FORMATTING_CODE + shop.getColortext() + " to see details.");
			}
			footer.add(FORMATTING_CODE + shop.getColortext() + "Type /" + EnjinMinecraftPlugin.BUY_COMMAND + " to go back, or type /ec to enable chat");
		}else {
			//Standard shop format
			StringBuilder topline = new StringBuilder(50);
			topline.append(FORMATTING_CODE + shop.getColorborder() + shop.getBorder_c());
			for(int i = 0; i < 4; i++) {
				topline.append(shop.getBorder_h());
			}
			if(itemcategory.getParentCategory() != null) {
				topline.append("[ " + FORMATTING_CODE + shop.getColortitle() + itemcategory.getParentCategory().getName() + " - " + itemcategory.getName() + FORMATTING_CODE + shop.getColorborder() + " ]");
				String toplinetrimmed = TrimText(topline.toString(), "... " + FORMATTING_CODE + shop.getColorborder() + "]");
				if(!toplinetrimmed.endsWith("... " + FORMATTING_CODE + shop.getColorborder() + "]")) {
					for(int i = 0; i < 40; i++) {
						topline.append(shop.getBorder_h());
					}
					header.add(TrimText(topline.toString(), null));
				}else {
					header.add(toplinetrimmed);
				}
			}else {
				topline.append(" " + FORMATTING_CODE + shop.getColortitle() + itemcategory.getName());
				String toplinetrimmed = TrimText(topline.toString(), "...");
				if(!toplinetrimmed.endsWith("...")) {
					topline.append(FORMATTING_CODE + shop.getColorborder() + " ");
					for(int i = 0; i < 40; i++) {
						topline.append(shop.getBorder_h());
					}
					header.add(TrimText(topline.toString(), null));
				}else {
					header.add(toplinetrimmed);
				}
			}
			verticalborder = FORMATTING_CODE + shop.getColorborder() + shop.getBorder_v();
			//Add blank line
			header.add(verticalborder);
			//Add category info
			if(itemcategory.getInfo() != null && !itemcategory.getInfo().trim().equals("")) {
				String[] info = WrapText(itemcategory.getInfo(), shop.getBorder_v(), shop.getColorborder(), shop.getColortext());
				for(String sinfo : info) {
					header.add(sinfo);
				}
			}
			//Add help text
			if(shop.getType() == Type.Category) {
				header.add(verticalborder + FORMATTING_CODE + shop.getColortext() + "Prices are in " + shop.getCurrency() + ". Choose a category with " + FORMATTING_CODE + shop.getColorbottom() + "/" + EnjinMinecraftPlugin.BUY_COMMAND + " <#>");
			}else if(shop.getType() == Type.Item) {
				header.add(verticalborder + FORMATTING_CODE + shop.getColortext() + "Prices are in " + shop.getCurrency() + ". Click a link or use " + FORMATTING_CODE + shop.getColorbottom() + "/" + EnjinMinecraftPlugin.BUY_COMMAND + " <#>" + FORMATTING_CODE + shop.getColortext() + " to see details.");
			}
			header.add(verticalborder);
			//Set the footer text
			footer.add(verticalborder + FORMATTING_CODE + shop.getColortext() + "Type /" + EnjinMinecraftPlugin.BUY_COMMAND + " to go back, or type /ec to enable chat");
		}
		//add together the header size, footer size, and the page line and the blank line.
		int usedlines = header.size() + footer.size() + 2;
		int itemlinestaken = 4;
		if(itemcategory.getType() == Type.Category) {
			if(shop.simpleCategoryModeDisplay()) {
				itemlinestaken = 1;
			}else {
				itemlinestaken = 3;
			}
		}else if(itemcategory.getType() == Type.Item) {
			if(shop.simpleItemModeDisplay()) {
				itemlinestaken = 1;
			}else {
				itemlinestaken = 4;
			}
		}
		//let's get how many items can go on a page
		int itemsperpage = (CONSOLE_HEIGHT - usedlines)/itemlinestaken;
		//Let's make sure we can at least display one item.
		if(itemsperpage < 1) {
			itemsperpage = 1;
		}
		int numofpages = itemcategory.getItems().size()/itemsperpage;
		//Let's add an extra page if there is a remainder
		if((itemcategory.getItems().size()%itemsperpage) > 0) {
			numofpages++;
		}
		int onitem = 0;
		for(int i = 0; i < numofpages; i++) {
			ArrayList<String> currentpage = new ArrayList<String>();
			for(int j = 0; j < header.size(); j++) {
				currentpage.add(header.get(j));
			}
			for(int j = 0; j < itemsperpage && onitem < itemcategory.getItems().size(); j++, onitem++) {
				String[] itemstring;
				if(itemlinestaken == 1) {
					itemstring = formatSimpleShortItem(shop, itemcategory.getItem(onitem), onitem);
				}else {
					itemstring = formatShortItem(shop, itemcategory.getItem(onitem), onitem);
				}
				for(int k = 0; k < itemstring.length; k++) {
					currentpage.add(itemstring[k]);
				}
			}

			//Add whitespace;
			for(int j = currentpage.size(); j < (20 - (footer.size() + 1)); j++) {
				currentpage.add(verticalborder + " ");
			}
			for(int j = 0; j < footer.size(); j++) {
				currentpage.add(footer.get(j));
			}

			//Add in the page numbers, etc.
			if(collapsed) {
				currentpage.add(FORMATTING_CODE + shop.getColorbottom() + "Page " + (i + 1) + " of " + numofpages + ", Type /" + EnjinMinecraftPlugin.BUY_COMMAND + " page #");
			}else {
				StringBuilder bottomline = new StringBuilder(50);
				bottomline.append(FORMATTING_CODE + shop.getColorborder() + shop.getBorder_c());
				for(int j = 0; j < 4; j++) {
					bottomline.append(shop.getBorder_h());
				}
				bottomline.append(FORMATTING_CODE + shop.getColorbottom() + " Type /" + EnjinMinecraftPlugin.BUY_COMMAND + " page # " + FORMATTING_CODE + shop.getColorborder());
				for(int j = 0; j < 40; j++) {
					bottomline.append(shop.getBorder_h());
				}
				String bottomlinetrimmed = TrimText(bottomline.toString(), "", 
						FORMATTING_CODE + shop.getColorbottom() + "Page " + (i + 1) + " of " + numofpages + FORMATTING_CODE + shop.getColorborder() +
						shop.getBorder_h() + shop.getBorder_h() + shop.getBorder_h() + shop.getBorder_h());
				currentpage.add(bottomlinetrimmed);					
			}
			pages.add(currentpage);
		}
		return pages;
	}
	
	public static ArrayList<String> getItemDetailsPage(ServerShop shop, ShopItem item) {
		ArrayList<String> itempage = new ArrayList<String>();
		boolean collapsed = false;
		String verticalborder;
		if(shop.getBorder_c() == null && shop.getBorder_h() == null && shop.getBorder_v() == null) {
			collapsed = true;
			verticalborder = "";
			itempage.add(" ");
			itempage.add(TrimText(FORMATTING_CODE + shop.getColortitle() + item.getName(), "..."));
		}else {
			verticalborder = FORMATTING_CODE + shop.getColorborder() + shop.getBorder_v();
			//Standard shop format
			StringBuilder topline = new StringBuilder(50);
			topline.append(FORMATTING_CODE + shop.getColorborder() + shop.getBorder_c());
			for(int i = 0; i < 4; i++) {
				topline.append(shop.getBorder_h());
			}
			topline.append(" " + FORMATTING_CODE + shop.getColortitle() + item.getName() + FORMATTING_CODE + shop.getColorborder() + " ");
			String toplinetrimmed = TrimText(topline.toString(), "...");
			if(!toplinetrimmed.endsWith("...")) {
				topline.append(FORMATTING_CODE + shop.getColorborder() + " ");
				for(int i = 0; i < 40; i++) {
					topline.append(shop.getBorder_h());
				}
				itempage.add(TrimText(topline.toString(), null));
			}else {
				itempage.add(toplinetrimmed);
			}		
		}
		itempage.add(verticalborder + " ");
		itempage.add(verticalborder + FORMATTING_CODE + shop.getColortext() + "Price: " + FORMATTING_CODE + shop.getColorprice() + formatPrice(item.getPrice(), shop.getCurrency()));
		if(item.getOptions().size() > 0) {
			StringBuilder options = new StringBuilder();
			options.append(FORMATTING_CODE + shop.getColortext() + "Options: ");
			for(int i = 0; i < item.getOptions().size(); i++) {
				if(i > 0) {
					options.append(", ");
				}
				ShopItemOptions option = item.getOption(i);
				options.append(FORMATTING_CODE + shop.getColorname() + option.getName() + FORMATTING_CODE + shop.getColorbracket() + " (");
				options.append(FORMATTING_CODE + shop.getColorprice() + formatPrice(option, shop.getCurrency()) + FORMATTING_CODE + shop.getColorbracket() + ")");
			}
			ArrayList<String> optionlines = WrapFormattedText(verticalborder, options.toString());
			for(String optionline : optionlines) {
				itempage.add(optionline);
			}
		}
		itempage.add(verticalborder + FORMATTING_CODE + shop.getColortext() + "Info:");
		String[] infolines;
		if(collapsed) {
			infolines = WrapText(item.getInfo(), shop.getColorinfo());
		}else {
			infolines = WrapText(item.getInfo(), shop.getBorder_v(), shop.getColorborder(), shop.getColorinfo());
		}
		for(String infoline : infolines) {
			itempage.add(infoline);
		}
		//Add whitespace;
		for(int i = itempage.size(); i < 15; i++) {
			itempage.add(verticalborder + " ");
		}
		itempage.add(verticalborder + FORMATTING_CODE + shop.getColortext() + "Click the following link to checkout:");
		itempage.add(verticalborder + FORMATTING_CODE + shop.getColorurl() + shop.getBuyurl() + item.getId());
		itempage.add(verticalborder + " ");
		itempage.add(verticalborder + FORMATTING_CODE + shop.getColortext() + "Type /" + EnjinMinecraftPlugin.BUY_COMMAND + " to go back, or type /ec to enable chat");
		if(collapsed) {
			itempage.add(FORMATTING_CODE + shop.getColorbottom() + "Click the item link to purchase");
		}else {
			//Standard shop format
			StringBuilder bottomline = new StringBuilder(50);
			bottomline.append(FORMATTING_CODE + shop.getColorborder() + shop.getBorder_c());
			for(int i = 0; i < 4; i++) {
				bottomline.append(shop.getBorder_h());
			}
			bottomline.append(" " + FORMATTING_CODE + shop.getColorbottom() + "Click the item link to purchase" + FORMATTING_CODE + shop.getColorborder() + " ");
			bottomline.append(FORMATTING_CODE + shop.getColorborder() + " ");
			for(int i = 0; i < 40; i++) {
				bottomline.append(shop.getBorder_h());
			}
			itempage.add(TrimText(bottomline.toString(), null));
		}
		
		return itempage;
	}
	
	public static ArrayList<String> getShopListing(PlayerShopsInstance shops) {
		ArrayList<String> shopoutput = new ArrayList<String>();
		shopoutput.add(ChatColor.WHITE + "=== Choose Shop ===");
		shopoutput.add(ChatColor.WHITE + "Please type " + ChatColor.YELLOW + "/" + EnjinMinecraftPlugin.BUY_COMMAND + " shop <#>");
		shopoutput.add(" ");
		for(int i = 0; i < shops.getServerShops().size() && i < 15; i++) {
			shopoutput.add(TrimText(ChatColor.YELLOW + String.valueOf(i +1) + ". " + shops.getServerShop(i).getName(), "..."));
		}
		shopoutput.add(" ");
		shopoutput.add(ChatColor.WHITE + "Type /ec to enable chat");
		return shopoutput;
	}
	
	public static ArrayList<String> WrapFormattedText(String prefix, String text) {
		ArrayList<String> output = new ArrayList<String>();
		String fullline = text;
		if(getWidth(prefix + fullline) > MINECRAFT_CONSOLE_WIDTH) {
			int index = 0;
			while(index < fullline.length() -1) {
				String line = fullline.substring(index);
				while(getWidth(prefix + line) > MINECRAFT_CONSOLE_WIDTH) {
					if(line.lastIndexOf(' ') > 0) {
						line = line.substring(0, line.lastIndexOf(' '));
					}else {
						line = line.substring(0, line.length() - 1);
					}
				}
				if(index > 0) {
					int lastformattingcode = fullline.lastIndexOf(FORMATTING_CODE, index);
					String textcolor = fullline.substring(lastformattingcode + 1, lastformattingcode + 2);
					output.add(prefix + FORMATTING_CODE + textcolor + line);
				}else {
					output.add(prefix + line);
				}
				index += line.length();
			}
		}else {
			output.add(prefix + text);
		}
		return output;
	}
	
	public static int getWidth(String text) {
		String cleanedtext = ChatColor.stripColor(text);
		cleanedtext = cleanedtext.replace(FORMATTING_CODE, "");
		return MinecraftFont.Font.getWidth(cleanedtext) + (cleanedtext.length()-1);
	}

	public static String TrimText(String text, String ellipses) {
		String trimmedtext = text;
		if(getWidth(trimmedtext) > MINECRAFT_CONSOLE_WIDTH) {
			trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
			if(ellipses != null) {
				while(getWidth(trimmedtext + ellipses) > MINECRAFT_CONSOLE_WIDTH) {
					trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
				}
				return trimmedtext + ellipses;
			}else {
				while(getWidth(trimmedtext) > MINECRAFT_CONSOLE_WIDTH) {
					trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
				}
				return trimmedtext;
			}
		}else {
			return text;
		}
	}
	
	public static String TrimText(String text, String ellipses, String suffix) {
		String trimmedtext = text;
		if(getWidth(trimmedtext + suffix) > MINECRAFT_CONSOLE_WIDTH) {
			trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
			if(ellipses != null) {
				while(getWidth(trimmedtext + ellipses + suffix)> MINECRAFT_CONSOLE_WIDTH) {
					trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
				}
				return trimmedtext + ellipses + suffix;
			}else {
				while(getWidth(trimmedtext + suffix) > MINECRAFT_CONSOLE_WIDTH) {
					trimmedtext = trimmedtext.substring(0, trimmedtext.length() - 1);
				}
				return trimmedtext + suffix;
			}
		}else {
			return text + suffix;
		}
	}
	
	public static String[] formatShortItem(ServerShop shop, AbstractShopSuperclass item, int itemnumber) {
		if(item instanceof ShopItem) {
			return formatShortItem(shop, (ShopItem)item, itemnumber);
		}else if(item instanceof ShopCategory) {
			return formatShortItem(shop, (ShopCategory)item, itemnumber);
		}else {
			return new String[0];
		}
	}
	
	public static String[] formatShortItem(ServerShop shop, ShopItem item, int itemnumber) {
		String verticalborder = "";
		if (shop.getBorder_v() != null && !shop.getBorder_v().equals("")) {
			verticalborder = FORMATTING_CODE + shop.getColorborder() + shop.getBorder_v();
		}
		String[] itemstring = new String[4];
		String formattedprice = formatPrice(item.getPrice(), shop.getCurrency());
		itemstring[0] = verticalborder + FORMATTING_CODE + shop.getColorid() + (itemnumber + 1) + ". " + FORMATTING_CODE + shop.getColorname() 
				+ item.getName();
		itemstring[0] = TrimText(itemstring[0], "...",  FORMATTING_CODE + shop.getColorbracket() + " (" + FORMATTING_CODE + shop.getColorprice() + formattedprice
				+ FORMATTING_CODE + shop.getColorbracket() + ")");
		String description = item.getInfo().split("\n")[0];
		itemstring[1] = verticalborder + FORMATTING_CODE + shop.getColorurl() + shop.getBuyurl() + item.getId();
		itemstring[2] = TrimText(verticalborder + FORMATTING_CODE + shop.getColorinfo() + description, "...");
		itemstring[3] = verticalborder;
		return itemstring;
	}
	
	public static String[] formatShortItem(ServerShop shop, ShopCategory item, int itemnumber) {
		String verticalborder = "";
		if (shop.getBorder_v() != null && !shop.getBorder_v().equals("")) {
			verticalborder = FORMATTING_CODE + shop.getColorborder() + shop.getBorder_v();
		}
		String[] itemstring = new String[3];
		itemstring[0] = verticalborder + FORMATTING_CODE + shop.getColorid() + (itemnumber + 1) + ". "+ FORMATTING_CODE + shop.getColorbracket() + "[ " 
				+ FORMATTING_CODE + shop.getColorname() + item.getName() + FORMATTING_CODE + shop.getColorbracket() + " ]";
		itemstring[0] = TrimText(itemstring[0], "..." + FORMATTING_CODE + shop.getColorbracket() + " ]");
		String description = item.getInfo().split("\n")[0];
		itemstring[1] = TrimText(verticalborder + FORMATTING_CODE + shop.getColorinfo() + description, "...");
		itemstring[2] = verticalborder;
		return itemstring;
	}
	
	public static String[] formatSimpleShortItem(ServerShop shop, AbstractShopSuperclass item, int itemnumber) {
		if(item instanceof ShopItem) {
			return formatSimpleShortItem(shop, (ShopItem)item, itemnumber);
		}else if(item instanceof ShopCategory) {
			return formatSimpleShortItem(shop, (ShopCategory)item, itemnumber);
		}else {
			return new String[0];
		}
	}
	
	public static String[] formatSimpleShortItem(ServerShop shop, ShopItem item, int itemnumber) {
		String verticalborder = "";
		if (shop.getBorder_v() != null && !shop.getBorder_v().equals("")) {
			verticalborder = FORMATTING_CODE + shop.getColorborder() + shop.getBorder_v();
		}
		String[] itemstring = new String[4];
		String formattedprice = formatPrice(item.getPrice(), shop.getCurrency());
		itemstring[0] = verticalborder + FORMATTING_CODE + shop.getColorid() + (itemnumber + 1) + ". " + FORMATTING_CODE + shop.getColorname() 
				+ item.getName();
		itemstring[0] = TrimText(itemstring[0], "...",  FORMATTING_CODE + shop.getColorbracket() + " (" + FORMATTING_CODE + shop.getColorprice() + formattedprice
				+ FORMATTING_CODE + shop.getColorbracket() + ")");
		return itemstring;
	}
	
	public static String[] formatSimpleShortItem(ServerShop shop, ShopCategory item, int itemnumber) {
		String verticalborder = "";
		if (shop.getBorder_v() != null && !shop.getBorder_v().equals("")) {
			verticalborder = FORMATTING_CODE + shop.getColorborder() + shop.getBorder_v();
		}
		String[] itemstring = new String[1];
		itemstring[0] = verticalborder + FORMATTING_CODE + shop.getColorid() + (itemnumber + 1) + ". "+ FORMATTING_CODE + shop.getColorbracket() + "[ " 
				+ FORMATTING_CODE + shop.getColorname() + item.getName() + FORMATTING_CODE + shop.getColorbracket() + " ]";
		itemstring[0] = TrimText(itemstring[0], "..." + FORMATTING_CODE + shop.getColorbracket() + " ]");
		return itemstring;
	}
	
	public static String[] WrapText(String text, String prefix, String prefixcolor, String textcolor) {
		String[] lines = text.split("\n");
		ArrayList<String> formattedlines = new ArrayList<String>();
		for(int i = 0; i < lines.length && formattedlines.size() < 6; i++) {
			String fullline = lines[i];
			if(getWidth(prefix + fullline) + ((prefix + fullline).length()) > MINECRAFT_CONSOLE_WIDTH) {
				int index = 0;
				while(index < fullline.length() -1 && formattedlines.size() < 6) {
					String line = fullline.substring(index);
					while(getWidth(prefix + line) > MINECRAFT_CONSOLE_WIDTH) {
						if(line.lastIndexOf(' ') > 0) {
							line = line.substring(0, line.lastIndexOf(' '));
						}else {
							line = line.substring(0, line.length() - 1);
						}
					}
					formattedlines.add(FORMATTING_CODE + prefixcolor + prefix + FORMATTING_CODE + textcolor + line);
					index += line.length();
				}
			}else {
				formattedlines.add(FORMATTING_CODE + prefixcolor + prefix + FORMATTING_CODE + textcolor + fullline);
			}
		}
		String[] returnarray = new String[formattedlines.size()];
		for(int i = 0; i < returnarray.length; i++) {
			returnarray[i] = formattedlines.get(i);
		}
		return returnarray;
	}
	
	public static String[] WrapText(String text, String textcolor) {
		String[] lines = text.split("\n");
		ArrayList<String> formattedlines = new ArrayList<String>();
		for(int i = 0; i < lines.length && formattedlines.size() < 6; i++) {
			String fullline = lines[i];
			if(getWidth(fullline) > MINECRAFT_CONSOLE_WIDTH) {
				int index = 0;
				while(index < fullline.length() -1 && formattedlines.size() < 6) {
					String line = fullline.substring(index);
					while(getWidth(line) > MINECRAFT_CONSOLE_WIDTH) {
						if(line.lastIndexOf(' ') > 0) {
							line = line.substring(0, line.lastIndexOf(' '));
						}else {
							line = line.substring(0, line.length() - 1);
						}
					}
					formattedlines.add(FORMATTING_CODE + textcolor + line);
					index += line.length();
				}
			}else {
				formattedlines.add(FORMATTING_CODE + textcolor + fullline);
			}
		}
		String[] returnarray = new String[formattedlines.size()];
		for(int i = 0; i < returnarray.length; i++) {
			returnarray[i] = formattedlines.get(i);
		}
		return returnarray;
	}
}
