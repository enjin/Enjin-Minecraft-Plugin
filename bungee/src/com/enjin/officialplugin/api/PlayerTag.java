package com.enjin.officialplugin.api;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class stores everything about a tag on a player.
 * @author Tux2
 *
 */
public class PlayerTag {
	
	int tagid = -1;
	String name = "";
	long expirytime = 0;
	
	ConcurrentHashMap<String, Object> customtags = new ConcurrentHashMap<String, Object>();
	
	public PlayerTag(int id, String name) {
		tagid = id;
		this.name = name;
	}
	
	public PlayerTag(int id, String name, long expiry) {
		tagid = id;
		this.name = name;
		expirytime = expiry;
	}
	
	/**
	 * Gets the unique tag ID. This ID is unique to that particular tag and
	 * doesn't change, even when the tag name may change.
	 * @return the ID of the tag, or -1 if not found.
	 */
	public int getTagID() {
		return tagid;
	}
	
	/**
	 * Gets the tag's name.
	 * @return
	 */
	public String getTagName()  {
		return name;
	}
	
	/**
	 * Gets the tag's expiry time in milliseconds, or 0 if set not to expire.
	 * @return
	 */
	public long getExpiryTime() {
		return expirytime;
	}
	
	/**
	 * Returns a map of custom data for the tag. This is useful for information
	 * added to the API that the plugin cannot specifically handle quite yet.
	 * @return
	 */
	public ConcurrentHashMap<String, Object> getCustomData() {
		return customtags;
	}
	
	/**
	 * Gets the particular data assigned to a key this plugin cannot handle.
	 * @param key the name of the variable.
	 * @return the object. Returns null if it isn't set.
	 */
	public Object getCustomData(String key) {
		return customtags.get(key);
	}

	@Override
	public String toString() {
		return "{tag_id: " + tagid + ", name: " + name + ", expiry_time: " + expirytime + ", custom_data: " + customtags.toString() + "}";
	} 
}
