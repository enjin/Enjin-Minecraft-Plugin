package com.enjin.officialplugin.shop;

import java.util.ArrayList;

public class ShopItemOptions {
	
	public enum Type {
		MultipleChoice,
		AllText,
		AllTextNoQuotes,
		Alphanumeric,
		Alphabetical,
		Numeric,
		MultipleCheckboxes,
		Undefined;
	}

	String name = "";
	String id = "";
	String minprice = "";
	String maxprice = "";
	String minpoints = "";
	String maxpoints = "";
	Type type = Type.Undefined;
	boolean required = false;
	int minlength = -1;
	int maxlength = -1;
	int maxValue = -1;
	int minValue = -1;
	ArrayList<ShopOptionOptions> options = new ArrayList<ShopOptionOptions>();
	
	public ShopItemOptions(String name, String id, String pricemin, String pricemax, String minpoints, String maxpoints) {
		this.name = name;
		minprice = pricemin;
		maxprice = pricemax;
		this.minpoints = minpoints;
		this.maxpoints = maxpoints;
	}
	
	public void addOption(ShopOptionOptions option) {
		options.add(option);
	}
	
	public ArrayList<ShopOptionOptions> getOptions() {
		return options;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public void setMinLength(int length) {
		minlength = length;
	}
	
	public int getMinLength() {
		return minlength;
	}
	
	public void setMaxLength(int length) {
		maxlength = length;
	}
	
	public int getMaxLength() {
		return maxlength;
	}
	
	public Type getType() {
		return type;
	}
	
	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getMinPrice() {
		return minprice;
	}

	public String getMaxPrice() {
		return maxprice;
	}
	
	public String getMinPoints() {
		return minpoints;
	}
	
	public String getMaxPoints() {
		return maxpoints;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}
}
