package com.enjin.bungee.tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.bungee.util.io.ReverseFileReader;
import com.enjin.common.utils.ConnectionUtil;
import com.enjin.core.Enjin;
import com.enjin.core.config.EnjinConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

public class ReportPublisher implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private StringBuilder builder;
    private CommandSender sender;

    public ReportPublisher(EnjinMinecraftPlugin plugin, StringBuilder builder, CommandSender sender) {
        this.plugin = plugin;
        this.builder = builder;
        this.sender = sender;
    }

    @Override
    public synchronized void run() {
        EnjinConfig config = Enjin.getConfiguration();
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
                        builder.append(errormessages.get(i - 1)).append("\n");
                    }
                    errorfound = true;
                }
            }
            rfr.close();
        } catch (Exception e) {
            if (config.isDebug()) {
                Enjin.getLogger().catching(e);
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
                builder.append(enjinlogstuff.get(i - 1)).append("\n");
            }
            rfrlog.close();
        } catch (Exception ignored) {
        }
        if (plugin.getLastError() != null) {
            builder.append("\nLast Enjin Plugin Severe error message: \n");
            builder.append(plugin.getLastError().toString());
        }
        builder.append("\n=========================================\nEnjin HTTPS test: ").append(ConnectionUtil.testHTTPSconnection() ? "passed" : "FAILED!").append("\n");
        builder.append("Enjin HTTP test: ").append(ConnectionUtil.testHTTPconnection() ? "passed" : "FAILED!").append("\n");
        builder.append("Enjin web connectivity test: ").append(ConnectionUtil.testWebConnection() ? "passed" : "FAILED!").append("\n");
        builder.append("Is mineshafter present: ").append(ConnectionUtil.isMineshafterPresent() ? "yes" : "no").append("\n=========================================\n");
        //let's make sure to hide the apikey, wherever it may occurr in the file.
        String fullreport = builder.toString().replaceAll(config.getApiUrl(), "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
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
                } catch (Exception ignored) {
                }
            }
            sender.sendMessage(ChatColor.DARK_RED + "Unable to write enjin debug report!");
            Enjin.getLogger().catching(e);
        }
    }

}
