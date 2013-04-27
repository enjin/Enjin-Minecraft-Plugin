package com.enjin.officialplugin.shop;

import java.util.ArrayList;

public class ShopItem extends AbstractShopSuperclass {
	
	String id = "";
	String name = "";
	String price = "";
	String info = "";
	
	ArrayList<ShopItemOptions> options = new ArrayList<ShopItemOptions>();
	
	public ShopItem(String name, String id, String price, String info) {
		this.name = name;
		this.id = id;
		this.price = price;
		this.info = info;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPrice() {
		return price;
	}

	public String getInfo() {
		return info;
	}
	
	public void addOption(ShopItemOptions option) {
		options.add(option);
	}

	public ArrayList<ShopItemOptions> getOptions() {
		return options;
	}
	
	public ShopItemOptions getOption(int i) {
		try {
			return options.get(i);
		}catch (Exception e) {
			return null;
		}
	}
}
