package com.enjin.bungee.tasks;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.bungee.util.io.ReverseFileReader;
import com.enjin.rpc.util.ConnectionUtil;
import com.enjin.core.Enjin;
import com.enjin.core.config.EnjinConfig;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
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
                Enjin.getLogger().log(e);
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
        String report = builder.toString().replaceAll(config.getApiUrl(), "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        Date date = new Date();

        InputStream in = null;
        try () {
            in = new ByteArrayInputStream(report.getBytes())
            ZipFile zip = new ZipFile(new File("enjinreport_" + format.format(date) + ".zip"));
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
            zip.addFile(Enjin.getLogger().getLogFile(), parameters);
            parameters.setFileNameInZip("enjinreport_" + format.format(date) + ".txt");
            parameters.setSourceExternalStream(true);
            zip.addStream(in, parameters);
            sender.sendMessage(ChatColor.GOLD + "Enjin report created in " + zip.getFile().getPath() + " successfully!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.DARK_RED + "Unable to write enjin report!");
            Enjin.getLogger().log(e);
        }
    }

}
