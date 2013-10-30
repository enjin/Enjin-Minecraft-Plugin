package com.enjin.officialplugin.shop;

public class ShopOptionOptions {
	
	String id = "";
	String name = "";
	String value = "";
	String price = "";
	String points = "";
	
	public ShopOptionOptions(String id, String name, String value,
			String price, String points) {
		super();
		this.id = id;
		this.name = name;
		this.value = value;
		this.price = price;
		this.points = points;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getPrice() {
		return price;
	}

	public String getPoints() {
		return points;
	}
	
	
}
