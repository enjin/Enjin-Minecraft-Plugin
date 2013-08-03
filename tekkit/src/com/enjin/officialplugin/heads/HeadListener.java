package com.enjin.officialplugin.heads;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.HeadsUpdatedEvent;
import com.enjin.officialplugin.heads.HeadLocation.Type;

/**
 * This is the listener that listens for all the events necessary to create new signs,
 * as well as update existing signs with new data. Plugins using the API should not
 * directly call this class. Instead, fire an event if you updated head stats.
 * @author Tux2
 *
 */
public class HeadListener implements Listener {
	
	EnjinMinecraftPlugin plugin;
	Pattern recentitempattern = Pattern.compile("\\[donation([1-9]|10)\\]");
	Pattern topvoterpattern = Pattern.compile("\\[topvoter([1-9]|10)\\]");
	Pattern recentvoterspattern = Pattern.compile("\\[voter([1-9]|10)\\]");
	Pattern topplayerpattern = Pattern.compile("\\[topplayer([1-9]|10)\\]");
	Pattern topposterpattern = Pattern.compile("\\[topposter([1-9]|10)\\]");
	Pattern toplikespattern = Pattern.compile("\\[toplikes([1-9]|10)\\]");
	Pattern latestmemberpattern = Pattern.compile("\\[newmember([1-9]|10)\\]");
	Pattern toppointspattern = Pattern.compile("\\[toppoints([1-9]|10)\\]");
	Pattern topdonatorpointspattern = Pattern.compile("\\[pointsspent([1-9]|10)\\]");
	Pattern topdonatormoneypattern = Pattern.compile("\\[moneyspent([1-9]|10)\\]");
	BlockFace[] blockfaces = {BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
	
	public HeadListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockDestroy(BlockBreakEvent event) {
		if(event.isCancelled()) {
			return;
		}
		int block = event.getBlock().getTypeId();
		if(block == 144 /*Skull*/ || block == Material.WALL_SIGN.getId() || block == Material.SIGN_POST.getId()) {
			if(plugin.headlocation.hasHeadHere(event.getBlock().getLocation())) {
				if(!event.getPlayer().hasPermission("enjin.sign.remove")) {
					event.getPlayer().sendMessage(ChatColor.DARK_RED + "I'm sorry you don't have permission to remove this sign!");
					event.setCancelled(true);
					return;
				}
				plugin.headlocation.removeHead(event.getBlock().getLocation());
				plugin.headlocation.saveHeads();
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onHeadDataUpdated(HeadsUpdatedEvent event) {
		ArrayList<HeadLocation> headlist = plugin.headlocation.headlist.get(event.getType());
		if(headlist == null) {
			return;
		}
		for(HeadLocation hloc : headlist) {
			HeadData headdata = plugin.headdata.getHead(event.getType(), hloc.getPosition(), hloc.getItemid());
			
			if(headdata == null) {
				String[] signlines = plugin.cachedItems.getSignData("", "", event.getType(), hloc.getPosition(), "");
				headdata = new HeadData("", signlines, event.getType(), hloc.getPosition());
			}
			//If the heads don't exist anymore there, let's remove them from the database.
			if(!HeadUtils.updateHead(hloc, headdata)) {
				plugin.headlocation.removeHead(hloc.getSignLocation());
				plugin.headlocation.saveHeads();
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onSignUpdate(SignChangeEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(!event.getPlayer().hasPermission("enjin.sign.set")) {
			return;
		}
		String[] signlines = event.getLines();
		Matcher recentitemmatcher = recentitempattern.matcher(signlines[0]);
		Matcher topvotermatcher = topvoterpattern.matcher(signlines[0]);
		Matcher recentvotersmatcher = recentvoterspattern.matcher(signlines[0]);
		Matcher topplayermatcher = topplayerpattern.matcher(signlines[0]);
		Matcher toppostermatcher = topposterpattern.matcher(signlines[0]);
		Matcher toplikesmatcher = toplikespattern.matcher(signlines[0]);
		Matcher latestmembermatcher = latestmemberpattern.matcher(signlines[0]);
		Matcher toppointsmatcher = toppointspattern.matcher(signlines[0]);
		Matcher topdonatormoneymatcher = topdonatormoneypattern.matcher(signlines[0]);
		Matcher topdonatorpointsmatcher = topdonatorpointspattern.matcher(signlines[0]);
		HeadLocation hl = null;
		if(recentitemmatcher.find()) {
			int position = Integer.parseInt(recentitemmatcher.group(1)) - 1;
			Location signlocation = event.getBlock().getLocation();
			Location headlocation = findHead(signlocation);
			HeadLocation.Type type;
			if(signlines[1].trim().equals("")) {
				type = HeadLocation.Type.RecentDonator;
			}else {
				type = HeadLocation.Type.RecentItemDonator;
			}
			if(headlocation != null) {
				hl = new HeadLocation(signlocation.getWorld().getName(), headlocation.getBlockX(), 
						headlocation.getBlockY(), headlocation.getBlockZ(), signlocation.getBlockX(),
						signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}else {
				hl = new HeadLocation(signlocation.getWorld().getName(), signlocation.getBlockX(),
					signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}
			if(type == HeadLocation.Type.RecentItemDonator) {
				hl.setItemid(signlines[1].trim());
			}
		}else if(topvotermatcher.find()) {
			int position = Integer.parseInt(topvotermatcher.group(1)) - 1;
			Location signlocation = event.getBlock().getLocation();
			Location headlocation = findHead(signlocation);
			Type type;
			if(signlines[1].trim().equals("")) {
				type = HeadLocation.Type.TopMonthlyVoter;
			}else if(signlines[1].trim().toLowerCase().startsWith("m")) {
				type = HeadLocation.Type.TopMonthlyVoter;
			}else if(signlines[1].trim().toLowerCase().startsWith("w")) {
				type = HeadLocation.Type.TopWeeklyVoter;
			}else if(signlines[1].trim().toLowerCase().startsWith("d")) {
				type = HeadLocation.Type.TopDailyVoter;
			}else {
				type = HeadLocation.Type.TopMonthlyVoter;
			}
			if(headlocation != null) {
				hl = new HeadLocation(signlocation.getWorld().getName(), headlocation.getBlockX(), 
						headlocation.getBlockY(), headlocation.getBlockZ(), signlocation.getBlockX(),
						signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}else {
				hl = new HeadLocation(signlocation.getWorld().getName(), signlocation.getBlockX(),
					signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}
		}else if(recentvotersmatcher.find()) {
			int position = Integer.parseInt(recentvotersmatcher.group(1)) - 1;
			Location signlocation = event.getBlock().getLocation();
			Location headlocation = findHead(signlocation);
			Type type = HeadLocation.Type.RecentVoter;
			if(headlocation != null) {
				hl = new HeadLocation(signlocation.getWorld().getName(), headlocation.getBlockX(), 
						headlocation.getBlockY(), headlocation.getBlockZ(), signlocation.getBlockX(),
						signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}else {
				hl = new HeadLocation(signlocation.getWorld().getName(), signlocation.getBlockX(),
					signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}
		}else if(topplayermatcher.find()) {
			int position = Integer.parseInt(topplayermatcher.group(1)) - 1;
			Location signlocation = event.getBlock().getLocation();
			Location headlocation = findHead(signlocation);
			Type type = HeadLocation.Type.TopPlayer;
			if(headlocation != null) {
				hl = new HeadLocation(signlocation.getWorld().getName(), headlocation.getBlockX(), 
						headlocation.getBlockY(), headlocation.getBlockZ(), signlocation.getBlockX(),
						signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}else {
				hl = new HeadLocation(signlocation.getWorld().getName(), signlocation.getBlockX(),
					signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}
		}else if(toppostermatcher.find()) {
			int position = Integer.parseInt(toppostermatcher.group(1)) - 1;
			Location signlocation = event.getBlock().getLocation();
			Location headlocation = findHead(signlocation);
			Type type = HeadLocation.Type.TopPoster;
			if(headlocation != null) {
				hl = new HeadLocation(signlocation.getWorld().getName(), headlocation.getBlockX(), 
						headlocation.getBlockY(), headlocation.getBlockZ(), signlocation.getBlockX(),
						signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}else {
				hl = new HeadLocation(signlocation.getWorld().getName(), signlocation.getBlockX(),
					signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}
		}else if(toplikesmatcher.find()) {
			int position = Integer.parseInt(toplikesmatcher.group(1)) - 1;
			Location signlocation = event.getBlock().getLocation();
			Location headlocation = findHead(signlocation);
			Type type = HeadLocation.Type.TopLikes;
			if(headlocation != null) {
				hl = new HeadLocation(signlocation.getWorld().getName(), headlocation.getBlockX(), 
						headlocation.getBlockY(), headlocation.getBlockZ(), signlocation.getBlockX(),
						signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}else {
				hl = new HeadLocation(signlocation.getWorld().getName(), signlocation.getBlockX(),
					signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}
		}else if(latestmembermatcher.find()) {
			int position = Integer.parseInt(latestmembermatcher.group(1)) - 1;
			Location signlocation = event.getBlock().getLocation();
			Location headlocation = findHead(signlocation);
			Type type = HeadLocation.Type.LatestMembers;
			if(headlocation != null) {
				hl = new HeadLocation(signlocation.getWorld().getName(), headlocation.getBlockX(), 
						headlocation.getBlockY(), headlocation.getBlockZ(), signlocation.getBlockX(),
						signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}else {
				hl = new HeadLocation(signlocation.getWorld().getName(), signlocation.getBlockX(),
					signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}
		}else if(toppointsmatcher.find()) {
			int position = Integer.parseInt(toppointsmatcher.group(1)) - 1;
			Location signlocation = event.getBlock().getLocation();
			Location headlocation = findHead(signlocation);
			Type type = HeadLocation.Type.TopPoints;
			if(signlines[1].trim().toLowerCase().startsWith("m")) {
				type = HeadLocation.Type.TopPointsMonth;
			}else if(signlines[1].trim().toLowerCase().startsWith("w")) {
				type = HeadLocation.Type.TopPointsWeek;
			}else if(signlines[1].trim().toLowerCase().startsWith("d")) {
				type = HeadLocation.Type.TopPointsDay;
			}
			if(headlocation != null) {
				hl = new HeadLocation(signlocation.getWorld().getName(), headlocation.getBlockX(), 
						headlocation.getBlockY(), headlocation.getBlockZ(), signlocation.getBlockX(),
						signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}else {
				hl = new HeadLocation(signlocation.getWorld().getName(), signlocation.getBlockX(),
					signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}
		}else if(topdonatorpointsmatcher.find()) {
			int position = Integer.parseInt(topdonatorpointsmatcher.group(1)) - 1;
			Location signlocation = event.getBlock().getLocation();
			Location headlocation = findHead(signlocation);
			Type type = HeadLocation.Type.TopPointsDonators;
			if(signlines[1].trim().toLowerCase().startsWith("m")) {
				type = HeadLocation.Type.TopPointsDonatorsMonth;
			}else if(signlines[1].trim().toLowerCase().startsWith("w")) {
				type = HeadLocation.Type.TopPointsDonatorsWeek;
			}else if(signlines[1].trim().toLowerCase().startsWith("d")) {
				type = HeadLocation.Type.TopPointsDonatorsDay;
			}
			if(headlocation != null) {
				hl = new HeadLocation(signlocation.getWorld().getName(), headlocation.getBlockX(), 
						headlocation.getBlockY(), headlocation.getBlockZ(), signlocation.getBlockX(),
						signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}else {
				hl = new HeadLocation(signlocation.getWorld().getName(), signlocation.getBlockX(),
					signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}
		}else if(topdonatormoneymatcher.find()) {
			int position = Integer.parseInt(topdonatormoneymatcher.group(1)) - 1;
			Location signlocation = event.getBlock().getLocation();
			Location headlocation = findHead(signlocation);
			Type type = HeadLocation.Type.TopDonators;
			if(signlines[1].trim().toLowerCase().startsWith("m")) {
				type = HeadLocation.Type.TopDonatorsMonth;
			}else if(signlines[1].trim().toLowerCase().startsWith("w")) {
				type = HeadLocation.Type.TopDonatorsWeek;
			}else if(signlines[1].trim().toLowerCase().startsWith("d")) {
				type = HeadLocation.Type.TopDonatorsDay;
			}
			if(headlocation != null) {
				hl = new HeadLocation(signlocation.getWorld().getName(), headlocation.getBlockX(), 
						headlocation.getBlockY(), headlocation.getBlockZ(), signlocation.getBlockX(),
						signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}else {
				hl = new HeadLocation(signlocation.getWorld().getName(), signlocation.getBlockX(),
					signlocation.getBlockY(), signlocation.getBlockZ(), type, position);
			}
		}
		if(hl != null) {
			if(hl.hasHead()) {
			}
			plugin.headlocation.addHead(hl);
			plugin.headlocation.saveHeads();
			EnjinMinecraftPlugin.debug("Grabbing head data");
			HeadData headdata = plugin.headdata.getHead(hl.getType(), hl.getPosition(), hl.getItemid());
			if(headdata == null) {
				//If it's null, we should force a heads update in the background.
				plugin.forceHeadUpdate();
				String[] lines = plugin.cachedItems.getSignData("", "", hl.getType(), hl.getPosition(), "");
				headdata = new HeadData("", lines, hl.getType(), hl.getPosition());
			}
			event.setLine(0, headdata.getSigndata()[0]);
			event.setLine(1, headdata.getSigndata()[1]);
			event.setLine(2, headdata.getSigndata()[2]);
			event.setLine(3, headdata.getSigndata()[3]);
			//If the heads don't exist anymore there, let's remove them from the database.
			if(!HeadUtils.updateHead(hl, headdata)) {
				plugin.headlocation.removeHead(hl.getSignLocation());
				plugin.headlocation.saveHeads();
			}
		}
	}
	
	/**
	 * This finds a head in the vicinity of a block. It looks all around the block in
	 * a radius of 1, including edges. So if the sign was "S" and the other blocks were
	 * "B" it would look like this:<br>
	 * BBB<br>
	 * BSB<br>
	 * BBB<br>
	 * just in three dimensions. It also looks two blocks straight up from the sign to
	 * find a head if the owner chooses to hide the signs under a block.
	 * @param loc The location to look around.
	 * @return The location of the head or null if it cannot find one.
	 */
	private Location findHead(Location loc) {
		for(int x = loc.getBlockX() -1; x < (loc.getBlockX() +2); x++) {
			for(int y = loc.getBlockY() -1; y < (loc.getBlockY() +2); y++) {
				for(int z = loc.getBlockZ() -1; z < (loc.getBlockZ() +2); z++) {
					Block tblock = loc.getWorld().getBlockAt(x, y, z);
					if(tblock.getTypeId() == 144) {
						//Make sure this head isn't already assigned.
						if(!plugin.headlocation.hasHeadHere(tblock.getLocation())) {
							return tblock.getLocation();
						}
					}
				}
			}
		}
		//For those special cases where the owners want to hide the signs under the heads, we need to look two blocks up.
		Block tblock = loc.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP);
		if(tblock.getTypeId() == 144) {
			//Make sure this head isn't already assigned.
			if(!plugin.headlocation.hasHeadHere(tblock.getLocation())) {
				return tblock.getLocation();
			}
		}
		return null;
	}

}
