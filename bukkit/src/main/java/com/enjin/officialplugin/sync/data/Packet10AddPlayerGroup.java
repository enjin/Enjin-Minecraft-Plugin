package com.enjin.officialplugin.sync.data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.UUID;

import com.enjin.core.Enjin;
import com.enjin.officialplugin.util.PacketUtilities;
import com.enjin.rpc.mappings.mappings.plugin.data.PlayerGroupUpdateData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.enjin.officialplugin.CommandWrapper;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.AddPlayerGroupEvent;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class Packet10AddPlayerGroup {
    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String[] msg = PacketUtilities.readString(in).split(",");
            if ((msg.length == 2) || (msg.length == 3)) {
                String playername = msg[0];
                String groupname = msg[1];
                String world = (msg.length == 3) ? msg[2] : null;
                if ("*".equals(world)) {
                    world = null;
                }

                if (EnjinMinecraftPlugin.supportsUUID() && !EnjinMinecraftPlugin.vaultneedsupdating && (playername.length() == 32 || playername.length() == 36)) {
                    if (playername.length() == 32) {
                        // expand UUIDs which do not have dashes in them
                        playername = playername.substring(0, 8) + "-" + playername.substring(8, 12) + "-" + playername.substring(12, 16) +
                                "-" + playername.substring(16, 20) + "-" + playername.substring(20, 32);
                    }
                    try {
                        OfflinePlayer oplayer = Bukkit.getOfflinePlayer(UUID.fromString(playername));

                        Enjin.getPlugin().debug("Adding player " + playername + " from group " + groupname + " in world " + world + " world");
                        //Check to see if we have PermissionsBukkit. If we do we have to do something special
                        if (plugin.permissionsbukkit != null) {
                            Enjin.getPlugin().debug("Adding rank " + groupname + " for PermissionsBukkit for user " + playername);
                            plugin.commandqueue.addCommand(new CommandWrapper(Bukkit.getConsoleSender(), "permissions player addgroup " + playername + " " + groupname, ""));
                            //Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new CommandExecuter(Bukkit.getConsoleSender(), "permissions player addgroup " + playername + " " + groupname));
                        } else {
                            //We need some support if they want the group added to all worlds if the plugin doesn't support global groups
                            if ((world != null) || (world == null && plugin.supportsglobalgroups)) {
                                if (!EnjinMinecraftPlugin.permission.playerAddGroup(world, oplayer, groupname)) {
                                    Bukkit.getLogger().warning("Failed to update " + playername + "'s group. Please make sure that you have a valid permission plugin installed, and that your configurations are correct.");
                                }
                            } else {
                                for (World w : Bukkit.getWorlds()) {
                                    if (!EnjinMinecraftPlugin.permission.playerAddGroup(w.getName(), oplayer, groupname)) {
                                        Bukkit.getLogger().warning("Failed to update " + playername + "'s group in world " + w.getName() + ". Please make sure that you have a valid permission plugin installed, and that your configurations are correct.");
                                    }
                                }
                            }
                        }
                        plugin.getServer().getPluginManager().callEvent(new AddPlayerGroupEvent(oplayer, groupname, world));
                    } catch (IllegalArgumentException ex) {
                        Bukkit.getLogger().warning("Invalid UUID format sent for add player group! UUID sent: " + playername);
                    }
                } else {
                    OfflinePlayer oplayer = Bukkit.getOfflinePlayer(playername);
                    Enjin.getPlugin().debug("Adding player " + playername + " from group " + groupname + " in world " + world + " world");
                    //Check to see if we have PermissionsBukkit. If we do we have to do something special
                    if (plugin.permissionsbukkit != null) {
                        Enjin.getPlugin().debug("Adding rank " + groupname + " for PermissionsBukkit for user " + playername);
                        plugin.commandqueue.addCommand(new CommandWrapper(Bukkit.getConsoleSender(), "permissions player addgroup " + playername + " " + groupname, ""));
                        //Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new CommandExecuter(Bukkit.getConsoleSender(), "permissions player addgroup " + playername + " " + groupname));
                    } else {
                        //We need some support if they want the group added to all worlds if the plugin doesn't support global groups
                        if ((world != null) || (world == null && plugin.supportsglobalgroups)) {
                            if (!EnjinMinecraftPlugin.permission.playerAddGroup(world, playername, groupname)) {
                                Bukkit.getLogger().warning("Failed to update " + playername + "'s group. Please make sure that you have a valid permission plugin installed, and that your configurations are correct.");
                            }
                        } else {
                            for (World w : Bukkit.getWorlds()) {
                                if (!EnjinMinecraftPlugin.permission.playerAddGroup(w.getName(), playername, groupname)) {
                                    Bukkit.getLogger().warning("Failed to update " + playername + "'s group in world " + w.getName() + ". Please make sure that you have a valid permission plugin installed, and that your configurations are correct.");
                                }
                            }
                        }
                    }
                    plugin.getServer().getPluginManager().callEvent(new AddPlayerGroupEvent(oplayer, groupname, world));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void handle(PlayerGroupUpdateData data) {
        Enjin.getPlugin().getInstructionHandler().addToGroup(data.getPlayer(), data.getGroup(), data.getWorld());
    }
}
