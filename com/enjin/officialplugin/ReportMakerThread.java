package com.enjin.officialplugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.net.ssl.SSLHandshakeException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class ReportMakerThread implements Runnable {
	
	EnjinMinecraftPlugin plugin;
	StringBuilder builder;
	CommandSender sender;
	
	public ReportMakerThread(EnjinMinecraftPlugin plugin, StringBuilder builder, CommandSender sender) {
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
		builder.append("\n=========================================\nEnjin HTTPS test: " + (testHTTPSconnection() ? "passed" : "FAILED!") + "\n");
		builder.append("Enjin HTTP test: " + (testHTTPconnection() ? "passed" : "FAILED!") + "\n");
		builder.append("Enjin web connectivity test: " + (testWebConnection() ? "passed" : "FAILED!") + "\n=========================================\n");
		File bukkityml = new File(serverloglocation + File.separator + "bukkit.yml");
        YamlConfiguration ymlbukkit = new YamlConfiguration();
        if (bukkityml.exists()){
            try{
            	ymlbukkit.load(bukkityml);
            	if(ymlbukkit.getBoolean("settings.plugin-profiling", false)) {
            		plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "timings merged");
            		try {
            			plugin.debug("Waiting for timings file to be totally written...");
            			//Make sure the timings file is written before we continue!
            			wait(2000);
            		}
            		catch (InterruptedException e) {
            			// Do nothing.
            		}
            		boolean foundtimings = false;
            		File timingsfile;
        			plugin.debug("Searching for timings file");
            		//If the server owner has over 99 timings files, I don't know what to say...
                	for(int i = 99; i >= 0 && !foundtimings; i--) {
                		if(i == 0) {
	                		timingsfile = new File(serverloglocation + File.separator + "timings" + File.separator + "timings.txt");
                		}else {
                    		timingsfile = new File(serverloglocation + File.separator + "timings" + File.separator + "timings" + i + ".txt");
                		}
                		if(timingsfile.exists()) {
                			plugin.debug("Found timings file at: " + timingsfile.getAbsolutePath());
                			foundtimings = true;
                			builder.append("\nTimings file output:\n");
                			FileInputStream fstream = new FileInputStream(timingsfile);
                			DataInputStream in = new DataInputStream(fstream);
                			BufferedReader br = new BufferedReader(new InputStreamReader(in));
                			String strLine;
                			while ((strLine = br.readLine()) != null)   {
	                			builder.append(strLine + "\n");
                			}
                			in.close();
                		}
                	}
                }else {
                	builder.append("\nTimings file output not enabled!\n");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        System.out.println(builder.toString());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		Date date = new Date();
        BufferedWriter outChannel = null;
		try {
			outChannel = new BufferedWriter(new FileWriter(serverloglocation + File.separator + "enjinreport_" + dateFormat.format(date) + ".txt"));
			outChannel.write(builder.toString());
			outChannel.close();
			sender.sendMessage(ChatColor.GOLD + "Enjin debug report created in " + serverloglocation + File.separator + "enjinreport_" + dateFormat.format(date) + ".txt successfully!");
		} catch (IOException e) {
			if(outChannel != null) {
				try {
					outChannel.close();
				} catch (Exception e1) {
				}
			}
			sender.sendMessage(ChatColor.DARK_RED + "Unable to write enjin debug report!");
			e.printStackTrace();
		}
	}
	
	private boolean testHTTPSconnection() {
		try {
			URL url = new URL("https://api.enjin.com/ok.html");
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
			if(inputLine != null && inputLine.startsWith("OK")) {
				return true;
			}
			return false;
		} catch (SSLHandshakeException e) {
			if(plugin.debug) {
				e.printStackTrace();
			}
			return false;
		} catch (SocketTimeoutException e) {
			if(plugin.debug) {
				e.printStackTrace();
			}
			return false;
		} catch (Throwable t) {
			if(plugin.debug) {
				t.printStackTrace();
			}
			return false;
		}
	}
	
	private boolean testHTTPconnection() {
		try {
			URL url = new URL("http://api.enjin.com/ok.html");
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
			if(inputLine != null && inputLine.startsWith("OK")) {
				return true;
			}
			return false;
		} catch (SocketTimeoutException e) {
			if(plugin.debug) {
				e.printStackTrace();
			}
			return false;
		} catch (Throwable t) {
			if(plugin.debug) {
				t.printStackTrace();
			}
			return false;
		}
	}
	
	private boolean testWebConnection() {
		try {
			URL url = new URL("http://google.com");
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
			if(inputLine != null) {
				return true;
			}
			return false;
		} catch (SocketTimeoutException e) {
			if(plugin.debug) {
				e.printStackTrace();
			}
			return false;
		} catch (Throwable t) {
			if(plugin.debug) {
				t.printStackTrace();
			}
			return false;
		}
	}

}
