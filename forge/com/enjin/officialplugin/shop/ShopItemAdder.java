package com.enjin.officialplugin.shop;

import java.util.ArrayList;

import com.enjin.officialplugin.shop.ServerShop.Type;

public interface ShopItemAdder {

	abstract public void addItem(AbstractShopSuperclass item) throws ItemTypeNotSupported;
	abstract public ArrayList<AbstractShopSuperclass> getItems();
	abstract public AbstractShopSuperclass getItem(int i);
	abstract public void setType(Type type);
	abstract public Type getType();
	abstract public String getName();
	abstract public String getInfo();
	abstract public ShopItemAdder getParentCategory();
	abstract public void setParentCategory(ShopItemAdder category);
	abstract public void setPages(ArrayList<ArrayList<String>> pages);
	abstract public ArrayList<ArrayList<String>> getPages();
}
