package com.enjin.officialplugin.heads;

import com.enjin.officialplugin.heads.HeadLocation.Type;

/**
 * This class stores all the data needed to populate a sign, or head with information about this statistic.
 * @author Tux2
 *
 */
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
	 * @param ranking The ranking. 0 = highest, 9 = lowest.
	 */
	public HeadData(String playername, String[] signdata, Type type, int ranking) {
		super();
		this.playername = playername;
		this.signdata = signdata;
		this.type = type;
		this.ranking = ranking;
	}

	/**
	 * This stores all the data for the signs and player heads when an itemID is specified.
	 * @param playername The playername for the head.
	 * @param signdata The raw data for the sign.
	 * @param type The type of data this data type holds.
	 * @param ranking The ranking. 0 = highest, 9 = lowest.
	 * @param itemID The item ID of the item.
	 */
	public HeadData(String playername, String[] signdata, Type type,
			int ranking, String itemID) {
		super();
		this.playername = playername;
		this.signdata = signdata;
		this.type = type;
		this.ranking = ranking;
		this.itemID = itemID;
	}

	/**
	 * Returns the ranking of the head sign. 0 = highest 9 = lowest. 
	 * @return
	 */
	public int getRanking() {
		return ranking;
	}

	/**
	 * Sets the ranking of the head sign. 0 = highest, 9 = lowest.
	 * @param ranking
	 */
	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	/**
	 * Gets the player's name associated with this stat.
	 * @return
	 */
	public String getPlayername() {
		return playername;
	}

	/**
	 * Gets the data that will popluate the sign.
	 * @return A String array that contains the 4 lines of the sign.
	 */
	public String[] getSigndata() {
		return signdata;
	}
	
	/**
	 * Sets the data to display on the sign.
	 * @param signdata The array of lines to display.
	 */
	public void setSignData(String[] signdata) {
		this.signdata = signdata;
	}
	
	/**
	 * Gets the type of stat this head is for.
	 * @return
	 */
	public HeadLocation.Type getType() {
		return type;
	}
	
	/**
	 * This will increment the internal ranking. Please note that this will NOT
	 * change it's location in the {@link CachedHeadData} class.
	 */
	public void incrementRanking() {
		ranking++;
	}
	
	/**
	 * If this item has an item ID, this will get the specific item ID.
	 * @return
	 */
	public String getItemID() {
		return itemID;
	}
	
}
