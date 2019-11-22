package com.enjin.bukkit.tasks;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.util.io.ReverseLineInputStream;
import com.enjin.core.Enjin;
import com.enjin.rpc.util.ConnectionUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

public class ReportPublisherLegacy implements Runnable {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private EnjinMinecraftPlugin plugin;
    private StringBuilder        builder;
    private CommandSender        sender;
    private File                 logs = null;
    private File                 log  = null;

    private boolean dirty = false;

    public ReportPublisherLegacy(EnjinMinecraftPlugin plugin, StringBuilder builder, CommandSender sender) {
        this.plugin = plugin;
        this.builder = builder;
        this.sender = sender;
        this.logs = new File(plugin.getDataFolder(), "logs");
        this.log = new File(logs, "enjin.log");
    }

    @Override
    public synchronized void run() {
        builder.append('\n');

        File bukkitLog = new File("logs/latest.log");

        if (bukkitLog.exists()) {
            Stack<String> mostRecentLogs  = new Stack<>();
            Stack<String> mostRecentError = new Stack<>();

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(bukkitLog)));

                boolean errorDiscovered = false;
                boolean errorProcessed  = false;

                String line;
                while ((line = reader.readLine()) != null) {
                    if (mostRecentLogs.size() < 100) {
                        mostRecentLogs.push(line);
                    } else if (errorProcessed) {
                        break;
                    }

                    if (line.toLowerCase().startsWith("[SEVERE]") || line.toLowerCase().startsWith("[ERROR]")) {
                        if (!errorDiscovered) {
                            errorDiscovered = true;
                        }
                        mostRecentError.push(line);
                    } else if (errorDiscovered) {
                        break;
                    }
                }
            } catch (Exception e) {
                Enjin.getLogger().log(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Enjin.getLogger().log(e);
                    }
                }
            }

            if (!mostRecentError.isEmpty()) {
                builder.append("Last Severe error message:");
                setDirtyThenClean(1);
                while (!mostRecentError.empty()) {
                    builder.append(mostRecentError.pop());
                    setDirtyThenClean(1);
                }
                setDirty();
            }

            if (!mostRecentLogs.isEmpty()) {
                setClean(2);
                builder.append("Last 100 lines of enjin.log:");
                setDirtyThenClean(1);
                while (!mostRecentLogs.empty()) {
                    builder.append(mostRecentLogs.pop());
                    setDirtyThenClean(1);
                }
                setDirty();
            }
        }

        setClean(1);
        builder.append("=========================================");
        setDirtyThenClean(1);
        builder.append("Enjin HTTPS test: ").append(ConnectionUtil.testHTTPSconnection() ? "passed" : "FAILED!");
        setDirtyThenClean(1);
        builder.append("Enjin HTTP test: ").append(ConnectionUtil.testHTTPconnection() ? "passed" : "FAILED!");
        setDirtyThenClean(1);
        builder.append("Enjin web connectivity test: ")
               .append(ConnectionUtil.testWebConnection() ? "passed" : "FAILED!");
        setDirtyThenClean(1);
        builder.append("Is mineshafter present: ").append(ConnectionUtil.isMineshafterPresent() ? "yes" : "no");
        setDirtyThenClean(1);
        builder.append("=========================================");
        setDirtyThenClean(2);
        builder.append("=========================================");
        setDirtyThenClean(1);
        builder.append("Java System Time: ").append(System.currentTimeMillis() / 1000);
        setDirtyThenClean(1);
        builder.append("=========================================");
        setDirtyThenClean(2);

        File              bukkitConfigFile = new File("bukkit.yml");
        YamlConfiguration bukkitConfig     = new YamlConfiguration();

        if (bukkitConfigFile.exists()) {
            try {
                bukkitConfig.load(bukkitConfigFile);

                if (bukkitConfig.getBoolean("settings.plugin-profiling", false)) {
                    plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "timings merged");

                    try {
                        Enjin.getLogger().debug("Waiting for timings file to be totally written...");
                        //Make sure the timings file is written before we continue!
                        wait(2000);
                    } catch (InterruptedException e) {
                        // Do nothing.
                    }

                    File    timingsFile       = null;
                    boolean timingsDiscovered = false;

                    Enjin.getLogger().debug("Searching for timings file");
                    //If the server owner has over 99 timings files, I don't know what to say...
                    for (int i = 99; i >= 0 && !timingsDiscovered; i--) {
                        if (i == 0) {
                            timingsFile = new File(logs + File.separator + "timings" + File.separator + "timings.txt");
                        } else {
                            timingsFile = new File(logs + File.separator + "timings" + File.separator + "timings" + i + ".txt");
                        }

                        if (timingsFile.exists()) {
                            Enjin.getLogger().debug("Found timings file at: " + timingsFile.getAbsolutePath());


                            BufferedReader bufferedReader = null;
                            timingsDiscovered = true;

                            try {
                                bufferedReader = new BufferedReader(
                                        new InputStreamReader(
                                                new DataInputStream(
                                                        new FileInputStream(timingsFile))));

                                builder.append("Timings file output:");
                                setDirtyThenClean(1);

                                String line;
                                while ((line = bufferedReader.readLine()) != null) {
                                    builder.append(line);
                                    setDirtyThenClean(1);
                                }
                                setDirty();

                            } finally {
                                if (bufferedReader != null) {
                                    bufferedReader.close();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Enjin.getLogger().log(e);
            }
        }

        //let's make sure to hide the apikey, wherever it may occurr in the file.
        String report = builder.toString()
                               .replaceAll("authkey=\\w{50}",
                                           "authkey=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        Date   date   = new Date();

        InputStream in = null;

        try {
            in = new ByteArrayInputStream(report.getBytes());

            ZipFile archive = new ZipFile(new File("enjinreport_" + DATE_FORMAT.format(date) + ".zip"));

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);

            // Add Enjin Log
            archive.addFile(log, parameters);

            parameters.setFileNameInZip("enjinreport_" + DATE_FORMAT.format(date) + ".txt");
            parameters.setSourceExternalStream(true);

            // Add Report
            archive.addStream(in, parameters);

            sender.sendMessage(ChatColor.GOLD + "Enjin report created in " + archive.getFile()
                                                                                    .getPath() + " successfully!");
        } catch (ZipException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Unable to write enjin report!");
            Enjin.getLogger().log(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                Enjin.getLogger().log(e);
            }
        }
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty() {
        this.dirty = true;
    }

    public void setClean(int nl) {
        if (isDirty()) {
            this.dirty = false;
            if (builder != null && builder.length() > 0) {
                while (nl-- > 0) {
                    builder.append('\n');
                }
            }
        }
    }

    public void setDirtyThenClean(int nl) {
        setDirty();
        setClean(nl);
    }
}
