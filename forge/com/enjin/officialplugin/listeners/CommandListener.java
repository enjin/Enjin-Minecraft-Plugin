package com.enjin.officialplugin.listeners;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ForgeSubscribe;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinConsole;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.NewKeyVerifier;
import com.enjin.officialplugin.threaded.ReportMakerThread;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class CommandListener extends CommandBase {

	EnjinMinecraftPlugin plugin;
	
	public CommandListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
		
	}

	@Override
	public String getCommandName() {
		return "enjin";
	}

    public List getCommandAliases()
    {
    	LinkedList<String> aliases = new LinkedList<String>();
    	aliases.add("e");
        return aliases;
    }

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		//ICommand command = event.command;
		Set<String> ops = MinecraftServer.getServer().getConfigurationManager().getOps();
		String[] args = var2;
		//ops only please.
		if(var1 instanceof EntityPlayerMP && 
				!ops.contains(var1.getCommandSenderName().toLowerCase())) {
			return;
		}
		if(args.length > 0) {
			EntityPlayerMP sender = null;
			if(var1 instanceof EntityPlayerMP) {
				sender = (EntityPlayerMP) var1;
			}
			if(args[0].equalsIgnoreCase("key")) {
				if(args.length != 2) {
					return;
				}
				plugin.enjinlogger.info("Checking if key is valid");
				MinecraftServer.getServer().logInfo("Checking if key is valid");
				//Make sure we don't have several verifier threads going at the same time.
				if(plugin.verifier == null || plugin.verifier.completed) {
					plugin.verifier = new NewKeyVerifier(plugin, args[1], sender, false);
					Thread verifierthread = new Thread(plugin.verifier);
					verifierthread.start();
				}else {
					sendMessage(ChatColor.RED + "Please wait until we verify the key before you try again!", sender);
				}
				return;
			}else if(args[0].equalsIgnoreCase("report")) {
				sendMessage(ChatColor.GREEN + "Please wait as we generate the report", sender);
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
				Date date = new Date();
				StringBuilder report = new StringBuilder();
				report.append("Enjin Debug Report generated on " + dateFormat.format(date) + "\n");
				report.append("Enjin plugin version: " + plugin.getVersion() + "\n");
				/*if(plugin.votifierinstalled) {
					String votiferversion = Bukkit.getPluginManager().getPlugin("Votifier").getDescription().getVersion();
					report.append("Votifier version: " + votiferversion + "\n");
				}*/
				report.append("Forge version: " + MinecraftForge.getBrandingVersion() + "\n");
				report.append("Java version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + "\n");
				report.append("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n");
				
				if(plugin.authkeyinvalid) {
					report.append("ERROR: Authkey reported by plugin as invalid!\n");
				}
				if(plugin.unabletocontactenjin) {
					report.append("WARNING: Plugin has been unable to contact Enjin for the past 5 minutes\n");
				}
				
				report.append("\nMods: \n");
				List<ModContainer> modlist = Loader.instance().getActiveModList();
				for(ModContainer p : modlist) {
					report.append(p.getName() + " version " + p.getVersion() + "\n");
				}
				report.append("\nWorlds: \n");
				WorldServer[] worldservers = MinecraftServer.getServer().worldServers;
				for(WorldServer world : worldservers) {
					report.append(world.getWorldInfo().getWorldName() + "\n");
				}
				ReportMakerThread rmthread = new ReportMakerThread(plugin, report, sender);
				Thread dispatchThread = new Thread(rmthread);
	            dispatchThread.start();
	            return;
			}else if(args[0].equalsIgnoreCase("debug")) {
				if(plugin.debug) {
					plugin.debug = false;
				}else {
					plugin.debug = true;
				}
				sendMessage(ChatColor.GREEN + "Debugging has been set to " + plugin.debug, sender);
				return;
			}/*else if(args[0].equalsIgnoreCase("push")) {
				OfflinePlayer[] allplayers = getServer().getOfflinePlayers();
				if(playerperms.size() > 3000 || playerperms.size() >= allplayers.length) {
					int minutes = playerperms.size()/3000;
					//Make sure to tack on an extra minute for the leftover players.
					if(playerperms.size()%3000 > 0) {
						minutes++;
					}
					//Add an extra 10% if it's going to take more than one synch.
					//Just in case a synch fails.
					if(playerperms.size() > 3000) {
						minutes += minutes * 0.1;
					}
					sender.sendMessage(ChatColor.RED + "A rank sync is still in progress, please wait until the current sync completes.");
					sender.sendMessage(ChatColor.RED + "Progress: + Integer.toString(playerperms.size()) + more player ranks to transmit, ETA: " + minutes + " minute" + (minutes > 1 ? "s" : "") + ".");
					return true;
				}
				for(OfflinePlayer offlineplayer : allplayers) {
					playerperms.put(offlineplayer.getName(), "");
				}
				
				//Calculate how many minutes approximately it's going to take.
				int minutes = playerperms.size()/3000;
				//Make sure to tack on an extra minute for the leftover players.
				if(playerperms.size()%3000 > 0) {
					minutes++;
				}
				//Add an extra 10% if it's going to take more than one synch.
				//Just in case a synch fails.
				if(playerperms.size() > 3000) {
					minutes += minutes * 0.1;
				}
				sender.sendMessage(ChatColor.GREEN + Integer.toString(playerperms.size()) + " players have been queued for synching. This should take approximately " + Integer.toString(minutes) + " minutes.");
				return true;
			}else if(args[0].equalsIgnoreCase("savestats")) {
				new WriteStats(plugin).write("stats.stats");
				if(sender == null) {
					MinecraftServer.logger.info("Stats saved to stats.stats.");
				}else {
					sender.sendChatToPlayer(ChatColor.GREEN + "Stats saved to stats.stats.");
				}
				return;
			}*/else if(args[0].equalsIgnoreCase("inform")) {
				if(args.length < 3) {
					sendMessage(ChatColor.RED + "To send a message do: /enjin inform playername message", sender);
					return;
				}
				EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(args[1]);
				if(player == null) {
					sendMessage(ChatColor.RED + "That player isn't on the server at the moment.", sender);
					return;
				}
				StringBuilder thestring = new StringBuilder();
				for(int i = 2; i < args.length; i++) {
					if(i > 2) {
						thestring.append(" ");
					}
					thestring.append(args[i]);
				}
				player.sendChatToPlayer(EnjinConsole.translateColorCodes(thestring.toString()));
				return;
			}else if(args[0].equalsIgnoreCase("broadcast")) {
				if(args.length < 2) {
					sendMessage(ChatColor.RED + "To broadcast a message do: /enjin broadcast message", sender);
				}
				StringBuilder thestring = new StringBuilder();
				for(int i = 1; i < args.length; i++) {
					if(i > 1) {
						thestring.append(" ");
					}
					thestring.append(args[i]);
				}
				List<EntityPlayerMP> playerlist = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
				for(EntityPlayerMP player : playerlist) {
					player.sendChatToPlayer(EnjinConsole.translateColorCodes(thestring.toString()));
				}
				return;
			}else if(args[0].equalsIgnoreCase("lag")) {
				Runtime runtime = Runtime.getRuntime();
				long memused = runtime.totalMemory()/(1024*1024);
				long maxmemory = runtime.maxMemory()/(1024*1024);
				sendMessage(ChatColor.GOLD + "Average TPS: " + ChatColor.GREEN + plugin.tpstask.getTPSAverage(), sender);
				sendMessage(ChatColor.GOLD + "Last TPS measurement: " + ChatColor.GREEN + plugin.tpstask.getLastTPSMeasurement(), sender);
				sendMessage(ChatColor.GOLD + "Memory Used: " + ChatColor.GREEN + memused + "MB/" + maxmemory + "MB", sender);
				return;
			}else {
				/*
				 * Display detailed Enjin help in console
				 */
				sendMessage(EnjinConsole.header(), sender);

				sendMessage(ChatColor.GOLD + "/enjin key <KEY>: "
						+ ChatColor.RESET + "Enter the secret key from your " + ChatColor.GRAY + "Admin - Games - Minecraft - Enjin Plugin " + ChatColor.RESET + "page.", sender);
				sendMessage(ChatColor.GOLD + "/enjin broadcast <MESSAGE>: "
						+ ChatColor.RESET + "Broadcast a message to all players.", sender);
				sendMessage(ChatColor.GOLD + "/enjin push: "
						+ ChatColor.RESET + "Sync your website tags with the current ranks.", sender);
				/*if(sender.hasPermission("enjin.playerstats"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin playerstats <NAME>: "
                            + ChatColor.RESET + "Display player statistics.");
                if(sender.hasPermission("enjin.serverstats"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin serverstats: "
                            + ChatColor.RESET + "Display server statistics.");*/
				sendMessage(ChatColor.GOLD + "/enjin lag: "
						+ ChatColor.RESET + "Display TPS average and memory usage.", sender);
				sendMessage(ChatColor.GOLD + "/enjin debug: "
						+ ChatColor.RESET + "Enable debug mode and display extra information in console.", sender);
				sendMessage(ChatColor.GOLD + "/enjin report: "
						+ ChatColor.RESET + "Generate a report file that you can send to Enjin Support for troubleshooting.", sender);

				/*
                // Shop buy commands
                sender.sendMessage(ChatColor.GOLD + "/buy: "
                        + ChatColor.RESET + "Display items available for purchase.");
                sender.sendMessage(ChatColor.GOLD + "/buy page <#>: "
                        + ChatColor.RESET + "View the next page of results.");
                sender.sendMessage(ChatColor.GOLD + "/buy <ID>: "
                        + ChatColor.RESET + "Purchase the specified item ID in the server shop.");
				 */
				return;
            }
		}
	
		return;
	}
	
	private void sendMessage(String message, ICommandSender sender) {
		if(sender == null) {
			MinecraftServer.getServer().logInfo(EnjinConsole.stripColor(message));
		}else {
			sender.sendChatToPlayer(message);
		}
	}
	
	private void sendMessage(String[] messages, ICommandSender sender) {
		if(sender == null) {
			for(String message : messages) {
				MinecraftServer.getServer().logInfo(EnjinConsole.stripColor(message));
			}
		}else {
			for(String message : messages) {
				sender.sendChatToPlayer(message);
			}
		}
	}
}
