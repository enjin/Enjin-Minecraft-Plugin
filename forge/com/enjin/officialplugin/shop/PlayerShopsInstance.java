package com.enjin.officialplugin.shop;

import java.util.ArrayList;

public class PlayerShopsInstance {

	ArrayList<ServerShop> servershops = new ArrayList<ServerShop>();
	ServerShop selectedshop = null;
	ShopItemAdder selectedcategory = null;
	long retrievaltime;
	
	public PlayerShopsInstance() {
		retrievaltime = System.currentTimeMillis();
	}
	
	public void addServerShop(ServerShop shop) {
		servershops.add(shop);
	}
	
	public ArrayList<ServerShop> getServerShops() {
		return servershops;
	}
	
	public ServerShop getServerShop(int i) {
		return servershops.get(i);
	}
	
	public int getServerShopCount() {
		return servershops.size();
	}
	
	public void setActiveShop(int i) {
		selectedshop = servershops.get(i);
	}
	
	public void setActiveShop(ServerShop shop) {
		selectedshop = shop;
	}
	
	public ServerShop getActiveShop() {
		return selectedshop;
	}
	
	public void setActiveCategory(ShopItemAdder cat) {
		selectedcategory = cat;
	}
	
	public ShopItemAdder getActiveCategory() {
		return selectedcategory;
	}
	
	public long getRetrievalTime() {
		return retrievaltime;
	}
}