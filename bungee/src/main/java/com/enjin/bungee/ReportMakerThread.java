package com.enjin.bungee;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

public class ReportMakerThread implements Runnable {

    EnjinPlugin plugin;
    StringBuilder builder;
    CommandSender sender;

    public ReportMakerThread(EnjinPlugin plugin, StringBuilder builder, CommandSender sender) {
        this.plugin = plugin;
        this.builder = builder;
        this.sender = sender;
    }

    @Override
    public synchronized void run() {
        builder.append("\nLast Severe error message: \n");
        File serverloglocation = plugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile();
        try {
            File logfile = new File(serverloglocation.getAbsolutePath() + File.separator + "proxy.log.0");
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
            if (EnjinPlugin.debug) {
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
        if (plugin.lasterror != null) {
            builder.append("\nLast Enjin Plugin Severe error message: \n");
            builder.append(plugin.lasterror.toString());
        }
        builder.append("\n=========================================\nEnjin HTTPS test: " + (plugin.testHTTPSconnection() ? "passed" : "FAILED!") + "\n");
        builder.append("Enjin HTTP test: " + (plugin.testHTTPconnection() ? "passed" : "FAILED!") + "\n");
        builder.append("Enjin web connectivity test: " + (plugin.testWebConnection() ? "passed" : "FAILED!") + "\n");
        builder.append("Is mineshafter present: " + (EnjinPlugin.isMineshafterPresent() ? "yes" : "no") + "\n=========================================\n");
        //let's make sure to hide the apikey, wherever it may occurr in the file.
        String fullreport = builder.toString().replaceAll(EnjinPlugin.getHash(), "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        System.out.println(fullreport);
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
