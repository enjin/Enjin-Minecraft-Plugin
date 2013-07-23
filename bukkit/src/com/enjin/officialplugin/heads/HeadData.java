package com.enjin.officialplugin.heads;

import com.enjin.officialplugin.heads.HeadLocation.Type;

public class HeadData {

	String playername;
	String[] signdata;
	HeadLocation.Type type;
	int ranking = 0;
	String itemID = "";
	
	/**
	 * This stores all the data for the signs and player heads.
	 * @param playername The playername for the head.
	 * @param signdata The raw data for the sign.
	 * @param type The type of data this data type holds.
	 * @param ranking The ranking. 0 = highest, 4 = lowest.
	 */
	public HeadData(String playername, String[] signdata, Type type, int ranking) {
		super();
		this.playername = playername;
		this.signdata = signdata;
		this.type = type;
		this.ranking = ranking;
	}

	public HeadData(String playername, String[] signdata, Type type,
			int ranking, String itemID) {
		super();
		this.playername = playername;
		this.signdata = signdata;
		this.type = type;
		this.ranking = ranking;
		this.itemID = itemID;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public String getPlayername() {
		return playername;
	}

	public String[] getSigndata() {
		return signdata;
	}
	
	public void setSignData(String[] signdata) {
		this.signdata = signdata;
	}
	
	public HeadLocation.Type getType() {
		return type;
	}
	
	public void incrementRanking() {
		ranking++;
	}
	
	public String getItemID() {
		return itemID;
	}
	
}
