package com.enjin.officialplugin.shop;

import java.util.ArrayList;

public class PlayerShopsInstance {

	ArrayList<ServerShop> servershops = new ArrayList<ServerShop>();
	
	public void addServerShop(ServerShop shop) {
		servershops.add(shop);
	}
	
	public ArrayList<ServerShop> getServerShops() {
		return servershops;
	}
	
}
