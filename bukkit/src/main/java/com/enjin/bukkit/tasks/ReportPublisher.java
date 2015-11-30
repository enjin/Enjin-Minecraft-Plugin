package com.enjin.bukkit.tasks;

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

import com.enjin.common.utils.ConnectionUtil;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.util.io.ReverseFileReader;

public class ReportPublisher implements Runnable {

    EnjinMinecraftPlugin plugin;
    StringBuilder builder;
    CommandSender sender;

    public ReportPublisher(EnjinMinecraftPlugin plugin, StringBuilder builder, CommandSender sender) {
        this.plugin = plugin;
        this.builder = builder;
        this.sender = sender;
    }

    @Override
    public synchronized void run() {
        builder.append("\nLast Severe error message: \n");
        File serverloglocation = plugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile();
        try {
            //Test to see if we are using the new logs location in 1.7.2
            File logfile = new File(serverloglocation.getAbsolutePath() + File.separator + "logs" + File.separator + "latest.log");
            if (!logfile.exists()) {
                logfile = new File(serverloglocation.getAbsolutePath() + File.separator + "server.log");
            }
            ReverseFileReader rfr = new ReverseFileReader(logfile.getAbsolutePath());
            LinkedList<String> errormessages = new LinkedList<String>();
            String line = "";
            boolean errorfound = false;
            while ((line = rfr.readLine()) != null && !errorfound) {
                if (errormessages.size() >= 40) {
                    errormessages.removeFirst();
                }
                errormessages.add(line);
                if (line.contains("[SEVERE]") || line.contains("ERROR]")) {
                    //let's catch the entire severe error message.
                    boolean severeended = false;
                    while ((line = rfr.readLine()) != null && !severeended) {
                        if (line.contains("[SEVERE]") || line.contains("ERROR]")) {
                            if (errormessages.size() >= 40) {
                                errormessages.removeFirst();
                            }
                            errormessages.add(line);
                        } else {
                            severeended = true;
                        }
                    }
                    for (int i = errormessages.size(); i > 0; i--) {
                        builder.append(errormessages.get(i - 1) + "\n");
                    }
                    errorfound = true;
                }
            }
            rfr.close();
        } catch (Exception e) {
            if (EnjinMinecraftPlugin.getConfiguration().isDebug()) {
                e.printStackTrace();
            }
        }
        try {
            ReverseFileReader rfrlog = new ReverseFileReader(plugin.getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log");
            builder.append("\nLast 100 lines of enjin.log: \n");
            LinkedList<String> enjinlogstuff = new LinkedList<String>();
            String line = "";
            for (int i = 0; i < 100 && (line = rfrlog.readLine()) != null; i++) {
                enjinlogstuff.add(line);
            }
            for (int i = enjinlogstuff.size(); i > 0; i--) {
                builder.append(enjinlogstuff.get(i - 1) + "\n");
            }
            rfrlog.close();
        } catch (Exception e2) {
        }
        if (plugin.getLastError() != null) {
            builder.append("\nLast Enjin Plugin Severe error message: \n");
            builder.append(plugin.getLastError().toString());
        }
        builder.append("\n=========================================\nEnjin HTTPS test: " + (ConnectionUtil.testHTTPSconnection() ? "passed" : "FAILED!") + "\n");
        builder.append("Enjin HTTP test: " + (ConnectionUtil.testHTTPconnection() ? "passed" : "FAILED!") + "\n");
        builder.append("Enjin web connectivity test: " + (ConnectionUtil.testWebConnection() ? "passed" : "FAILED!") + "\n");
        builder.append("Is mineshafter present: " + (ConnectionUtil.isMineshafterPresent() ? "yes" : "no") + "\n=========================================\n");
        File bukkityml = new File(serverloglocation + File.separator + "bukkit.yml");
        YamlConfiguration ymlbukkit = new YamlConfiguration();
        if (bukkityml.exists()) {
            try {
                ymlbukkit.load(bukkityml);
                if (ymlbukkit.getBoolean("settings.plugin-profiling", false)) {
                    plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "timings merged");
                    try {
                        Enjin.getPlugin().debug("Waiting for timings file to be totally written...");
                        //Make sure the timings file is written before we continue!
                        wait(2000);
                    } catch (InterruptedException e) {
                        // Do nothing.
                    }
                    boolean foundtimings = false;
                    File timingsfile;
                    Enjin.getPlugin().debug("Searching for timings file");
                    //If the server owner has over 99 timings files, I don't know what to say...
                    for (int i = 99; i >= 0 && !foundtimings; i--) {
                        if (i == 0) {
                            timingsfile = new File(serverloglocation + File.separator + "timings" + File.separator + "timings.txt");
                        } else {
                            timingsfile = new File(serverloglocation + File.separator + "timings" + File.separator + "timings" + i + ".txt");
                        }
                        if (timingsfile.exists()) {
                            Enjin.getPlugin().debug("Found timings file at: " + timingsfile.getAbsolutePath());
                            foundtimings = true;
                            builder.append("\nTimings file output:\n");
                            FileInputStream fstream = new FileInputStream(timingsfile);
                            DataInputStream in = new DataInputStream(fstream);
                            BufferedReader br = new BufferedReader(new InputStreamReader(in));
                            String strLine;
                            while ((strLine = br.readLine()) != null) {
                                builder.append(strLine + "\n");
                            }
                            in.close();
                        }
                    }
                } else {
                    builder.append("\nTimings file output not enabled!\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //let's make sure to hide the apikey, wherever it may occurr in the file.
        String fullreport = builder.toString().replaceAll("authkey=\\w{50}", "authkey=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        Date date = new Date();
        BufferedWriter outChannel = null;
        try {
            outChannel = new BufferedWriter(new FileWriter(serverloglocation + File.separator + "enjinreport_" + dateFormat.format(date) + ".txt"));
            outChannel.write(fullreport);
            outChannel.close();
            sender.sendMessage(ChatColor.GOLD + "Enjin debug report created in " + serverloglocation + File.separator + "enjinreport_" + dateFormat.format(date) + ".txt successfully!");
        } catch (IOException e) {
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (Exception e1) {
                }
            }
            sender.sendMessage(ChatColor.DARK_RED + "Unable to write enjin debug report!");
            e.printStackTrace();
        }
    }

}
