package com.enjin.bukkit.tasks;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import com.enjin.rpc.util.ConnectionUtil;
import com.enjin.core.Enjin;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.util.io.ReverseFileReader;

public class ReportPublisher implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private StringBuilder builder;
    private CommandSender sender;
    private File logs = null;
    private File log = null;

    public ReportPublisher(EnjinMinecraftPlugin plugin, StringBuilder builder, CommandSender sender) {
        this.plugin = plugin;
        this.builder = builder;
        this.sender = sender;
        this.logs = new File(plugin.getDataFolder(), "logs");
        this.log = new File(logs, "enjin.log");
    }

    @Override
    public synchronized void run() {
        builder.append("\nLast Severe error message: \n");
        ReverseFileReader serverReader = null;
        try {
            //Test to see if we are using the new logs location in 1.7.2
            File serverLog = new File("logs/latest.log");
            if (serverLog.exists()) {
                serverReader = new ReverseFileReader(serverLog.getAbsolutePath());

                LinkedList<String> errorMessages = new LinkedList<>();
                String line;
                boolean errorFound = false;
                while ((line = serverReader.readLine()) != null && !errorFound) {
                    if (errorMessages.size() >= 40) {
                        errorMessages.removeFirst();
                    }

                    errorMessages.add(line);

                    if (line.contains("[SEVERE]") || line.contains("ERROR]")) {
                        //let's catch the entire severe error message.
                        boolean severeEnded = false;
                        while ((line = serverReader.readLine()) != null && !severeEnded) {
                            if (line.contains("[SEVERE]") || line.contains("ERROR]")) {
                                if (errorMessages.size() >= 40) {
                                    errorMessages.removeFirst();
                                }

                                errorMessages.add(line);
                            } else {
                                severeEnded = true;
                            }
                        }

                        for (int i = errorMessages.size(); i > 0; i--) {
                            builder.append(errorMessages.get(i - 1)).append("\n");
                        }

                        errorFound = true;
                    }
                }
            }
        } catch (Exception e) {
            if (Enjin.getConfiguration().isDebug()) {
                Enjin.getLogger().log(e);
            }
        } finally {
            if (serverReader != null) {
                serverReader.close();
            }
        }

        builder.append("\nLast 100 lines of enjin.log: \n");
        ReverseFileReader enjinReader = null;
        try {
            enjinReader = new ReverseFileReader(log.getAbsolutePath());
            LinkedList<String> lines = new LinkedList<String>();
            String line;
            for (int i = 0; i < 100 && (line = enjinReader.readLine()) != null; i++) {
                lines.add(line);
            }

            for (int i = lines.size(); i > 0; i--) {
                builder.append(lines.get(i - 1)).append("\n");
            }
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        } finally {
            if (enjinReader != null) {
                enjinReader.close();
            }
        }

        if (plugin.getLastError() != null) {
            builder.append("\nLast Enjin Plugin Severe error message: \n");
            builder.append(plugin.getLastError().toString());
        }

        builder.append("\n=========================================\nEnjin HTTPS test: ").append(ConnectionUtil.testHTTPSconnection() ? "passed" : "FAILED!").append("\n");
        builder.append("Enjin HTTP test: ").append(ConnectionUtil.testHTTPconnection() ? "passed" : "FAILED!").append("\n");
        builder.append("Enjin web connectivity test: ").append(ConnectionUtil.testWebConnection() ? "passed" : "FAILED!").append("\n");
        builder.append("Is mineshafter present: ").append(ConnectionUtil.isMineshafterPresent() ? "yes" : "no").append("\n=========================================\n");

        File bukkitYmlFile = new File("bukkit.yml");
        YamlConfiguration bukkitYml = new YamlConfiguration();
        if (bukkitYmlFile.exists()) {
            try {
                bukkitYml.load(bukkitYmlFile);
                if (bukkitYml.getBoolean("settings.plugin-profiling", false)) {
                    plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "timings merged");

                    try {
                        Enjin.getLogger().debug("Waiting for timings file to be totally written...");
                        //Make sure the timings file is written before we continue!
                        wait(2000);
                    } catch (InterruptedException e) {
                        // Do nothing.
                    }

                    boolean foundTimings = false;
                    File timingsFile;
                    Enjin.getLogger().debug("Searching for timings file");
                    //If the server owner has over 99 timings files, I don't know what to say...
                    for (int i = 99; i >= 0 && !foundTimings; i--) {
                        if (i == 0) {
                            timingsFile = new File(logs + File.separator + "timings" + File.separator + "timings.txt");
                        } else {
                            timingsFile = new File(logs + File.separator + "timings" + File.separator + "timings" + i + ".txt");
                        }

                        DataInputStream dataInputStream = null;
                        try {
                            if (timingsFile.exists()) {
                                Enjin.getLogger().debug("Found timings file at: " + timingsFile.getAbsolutePath());
                                foundTimings = true;
                                builder.append("\nTimings file output:\n");
                                FileInputStream fileInputStream = new FileInputStream(timingsFile);
                                dataInputStream = new DataInputStream(fileInputStream);
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
                                String line;

                                while ((line = bufferedReader.readLine()) != null) {
                                    builder.append(line).append("\n");
                                }
                            }
                        } finally {
                            if (dataInputStream != null) {
                                dataInputStream.close();
                            }
                        }
                    }
                } else {
                    builder.append("\nTimings file output not enabled!\n");
                }
            } catch (Exception e) {
                Enjin.getLogger().log(e);
            }
        }
        //let's make sure to hide the apikey, wherever it may occurr in the file.
        String finalReport = builder.toString().replaceAll("authkey=\\w{50}", "authkey=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        Date date = new Date();
        InputStream inChannel = null;

        try {
            inChannel = new ByteArrayInputStream(finalReport.getBytes());

            ZipFile zip = new ZipFile(new File("enjinreport_" + dateFormat.format(date) + ".zip"));
            ZipParameters reportParameters = new ZipParameters();
            reportParameters.setFileNameInZip("enjinreport_" + dateFormat.format(date) + ".txt");
            reportParameters.setSourceExternalStream(true);
            zip.addStream(inChannel, reportParameters);
            zip.addFile(Enjin.getLogger().getLogFile(), new ZipParameters());

            sender.sendMessage(ChatColor.GOLD + "Enjin debug report created in " + zip.getFile().getPath() + " successfully!");
        } catch (ZipException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Unable to write enjin debug report!");
            Enjin.getLogger().log(e);
        } finally {
            try {
                inChannel.close();
            } catch (IOException e) {
                Enjin.getLogger().log(e);
            }
        }
    }
}
