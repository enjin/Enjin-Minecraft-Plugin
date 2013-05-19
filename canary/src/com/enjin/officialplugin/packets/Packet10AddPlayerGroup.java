package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import net.canarymod.Canary;
import net.canarymod.api.OfflinePlayer;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.user.Group;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.AddPlayerGroupEvent;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet10AddPlayerGroup {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String[] msg = PacketUtilities.readString(in).split(",");
			if((msg.length == 2) || (msg.length == 3)) {
				String playername = msg[0];
				String groupname = msg[1];
				String world = (msg.length == 3) ? msg[2] : null;
				if("*".equals(world)) {
					world = null;
				}
				EnjinMinecraftPlugin.debug("Adding player " + playername + " from group " + groupname + " in world " + world + " world");

				Player target = Canary.getServer().matchPlayer(playername);
		        Group group = Canary.usersAndGroups().getGroup(groupname);
		        if(group == null) {
		            return;
		        }
		        if(target == null) {
		            OfflinePlayer oplayer = Canary.getServer().getOfflinePlayer(playername);
		            oplayer.addGroup(group);
		        }else {
			        target.addGroup(group);
		        }
			
		        Canary.hooks().callHook(new AddPlayerGroupEvent(playername, groupname, world));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
