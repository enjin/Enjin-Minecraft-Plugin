package com.enjin.officialplugin.listeners;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class VotifierListener {
	
	EnjinMinecraftPlugin plugin;
	
	public VotifierListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}

	//TODO: Re-add votifier support
	/*
	@HookHandler(priority = Priority.PASSIVE)
	public void voteRecieved(VotifierEvent event) {
		//Lists when testing will send a user called "test", let's
		//make sure we don't process those votes.
		if(event.getVote().getUsername().equalsIgnoreCase("test") ||
				event.getVote().getUsername().isEmpty()) {
			return;
		}
		Vote vote = event.getVote();
		//Remove anything non-alphanumeric from the username, removing exploits
		String username = vote.getUsername().replaceAll("[^0-9A-Za-z_]", "");
		if(username.isEmpty()) return;
		String lists = "";
		if(plugin.playervotes.containsKey(username)) {
			lists = plugin.playervotes.get(username);
			lists = lists + "," + vote.getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");
		}else {
			lists = vote.getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");
		}
		plugin.playervotes.put(username, lists);
	}*/

}
