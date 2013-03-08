package com.enjin.officialplugin.threaded;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.ReverseFileReader;

public class ReportMakerThread implements Runnable {
	
	EnjinMinecraftPlugin plugin;
	StringBuilder builder;
	EntityPlayerMP sender;
	
	public ReportMakerThread(EnjinMinecraftPlugin plugin, StringBuilder builder, EntityPlayerMP sender) {
		this.plugin = plugin;
		this.builder = builder;
		this.sender = sender;
	}

	@Override
	public synchronized void run() {
		builder.append("\nLast Severe error message: \n");
		File serverloglocation = plugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile();
		try {
			ReverseFileReader rfr = new ReverseFileReader(serverloglocation.getAbsolutePath() + File.separator + "server.log");
			LinkedList<String> errormessages = new LinkedList<String>();
			String line = "";
			boolean errorfound = false;
			while((line = rfr.readLine()) != null && !errorfound) {
				if(errormessages.size() >= 40) {
					errormessages.removeFirst();
				}
				errormessages.add(line);
				if(line.contains("[SEVERE]")) {
					//let's catch the entire severe error message.
					boolean severeended = false;
					while((line = rfr.readLine()) != null && !severeended) {
						if(line.contains("[SEVERE]")) {
							if(errormessages.size() >= 40) {
								errormessages.removeFirst();
							}
							errormessages.add(line);
						}else {
							severeended = true;
						}
					}
					for(int i = errormessages.size(); i > 0; i--) {
						builder.append(errormessages.get(i - 1) + "\n");
					}
					errorfound = true;
				}
			}
			rfr.close();
		} catch (Exception e) {
			if(plugin.debug) {
				e.printStackTrace();
			}
		}
		if(plugin.lasterror != null) {
			builder.append("\nLast Enjin Plugin Severe error message: \n");
			builder.append(plugin.lasterror.toString());
		}
		builder.append("\n=========================================\nEnjin HTTPS test: " + (plugin.testHTTPSconnection() ? "passed" : "FAILED!") + "\n");
		builder.append("Enjin HTTP test: " + (plugin.testHTTPconnection() ? "passed" : "FAILED!") + "\n");
		builder.append("Enjin web connectivity test: " + (plugin.testWebConnection() ? "passed" : "FAILED!") + "\n");
		builder.append("Is mineshafter present: " + (EnjinMinecraftPlugin.isMineshafterPresent() ? "yes" : "no") + "\n=========================================\n");
		//let's make sure to hide the apikey, wherever it may occurr in the file.
        String fullreport = builder.toString().replaceAll(plugin.hash, "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        System.out.println(fullreport);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		Date date = new Date();
        BufferedWriter outChannel = null;
		try {
			outChannel = new BufferedWriter(new FileWriter(serverloglocation + File.separator + "enjinreport_" + dateFormat.format(date) + ".txt"));
			outChannel.write(fullreport);
			outChannel.close();
			if(sender == null) {
				MinecraftServer.logger.info("Enjin debug report created in " + serverloglocation + File.separator + "enjinreport_" + dateFormat.format(date) + ".txt successfully!");
			}else {
				sender.sendChatToPlayer(ChatColor.GOLD + "Enjin debug report created in " + serverloglocation + File.separator + "enjinreport_" + dateFormat.format(date) + ".txt successfully!");
			}
		} catch (IOException e) {
			if(outChannel != null) {
				try {
					outChannel.close();
				} catch (Exception e1) {
				}
			}
			if(sender == null) {
				MinecraftServer.logger.info("Unable to write enjin debug report!");
			}else {
				sender.sendChatToPlayer(ChatColor.DARK_RED + "Unable to write enjin debug report!");
			}
			e.printStackTrace();
		}
	}

}
