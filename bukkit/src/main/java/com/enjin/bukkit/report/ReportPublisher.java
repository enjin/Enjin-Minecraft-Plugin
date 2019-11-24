package com.enjin.bukkit.report;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.modules.impl.VaultModule;
import com.enjin.bukkit.modules.impl.VotifierModule;
import com.enjin.bukkit.util.text.TextBuilder;
import com.enjin.bukkit.util.text.TextBuilder.BorderOptions;
import com.enjin.core.Enjin;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ReportPublisher extends BukkitRunnable {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss a z")
            .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")
            .withZone(ZoneOffset.UTC);
    private static final int BORDER_WIDTH = 80;
    private static final String REPORT_TITLE = "Enjin Plugin Report";
    private static final String ENVIRONMENT_TITLE = "Environment";
    private static final String EMP_TITLE = "Enjin Minecraft Plugin";
    private static final String PERMISSIONS_TITLE = "Permissions";
    private static final String ECONOMY_TITLE = "Economy";
    private static final String VOTING_TITLE = "Voting";
    private static final String PLUGINS_TITLE = "Plugins";
    private static final String WORLDS_TITLE = "Worlds";

    private final EnjinMinecraftPlugin plugin;
    private final CommandSender sender;
    private final TextBuilder report;
    private final File logFolder;
    private final File logFile;
    private final File errorFolder;

    public ReportPublisher(EnjinMinecraftPlugin plugin, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
        this.report = new TextBuilder();
        this.logFolder = new File(plugin.getDataFolder(), "logs");
        this.logFile = new File(logFolder, "enjin.log");
        this.errorFolder = new File(plugin.getDataFolder(), "errors");
        this.report.setBorderWidth(BORDER_WIDTH);
    }

    @Override
    public void run() {
        report.header(BorderOptions.TOP, REPORT_TITLE, getEmpVersion());
        addEnvironment();
        addEnjinPlugin();
        addIntegrations();
        addPlugins();
        addWorlds();
        report.border();
        zip();
    }

    private void addEnvironment() {
        report.header(ENVIRONMENT_TITLE);
        addDate();
        addJavaVersion();
        addOperatingSystem();
        addServerVersion();
        addMinecraftVersion();
    }

    private void addEnjinPlugin() {
        report.header(EMP_TITLE);
        addAuthenticationStatus();
        addServerId();
        addConnectionStatus();
    }

    private void addIntegrations() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
            addVaultIntegrations();

        if (Bukkit.getPluginManager().isPluginEnabled("Votifier"))
            addVotePlugin();
    }

    private void addVaultIntegrations() {
        VaultModule module = plugin.getModuleManager().getModule(VaultModule.class);
        if (module == null)
            return;
        if (module.isPermissionsAvailable())
            addPermissionsPlugin(module.getPermission());
        if (module.isEconomyAvailable())
            addEconomyPlugin(module.getEconomy());
    }

    private void addPermissionsPlugin(Permission permission) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(permission.getName());
        report.header(PERMISSIONS_TITLE);
        addPluginVersionAndStatus(plugin);
    }

    private void addEconomyPlugin(Economy economy) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(economy.getName());
        report.header(ECONOMY_TITLE);
        addPluginVersionAndStatus(plugin);
    }

    private void addVotePlugin() {
        VotifierModule module = plugin.getModuleManager().getModule(VotifierModule.class);
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Votifier");
        FileConfiguration config = plugin.getConfig();

        report.header(VOTING_TITLE);
        addPluginVersionAndStatus(plugin);
        report.append("Host: ").append(config.getString("host", "n/a")).newLine();
        report.append("Port: ").append(config.getString("port", "n/a")).newLine();
        report.append("Session Votes: ").append(module.getSessionVotes()).newLine();
        report.append("Last Vote: ").append(module.getLastVote().orElse("n/a")).newLine();
    }

    private void addPluginVersionAndStatus(Plugin plugin) {
        report.append("Plugin: ").append(plugin.getName())
                .indent(1).append(plugin.getDescription().getVersion()).newLine();
        report.append("Enabled: ").append(plugin.isEnabled()).newLine();
    }

    private void addPlugins() {
        report.header(PLUGINS_TITLE);
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            report.append(plugin.getName()).indent(1).append(plugin.getDescription().getVersion()).newLine();
    }

    private void addWorlds() {
        report.header(WORLDS_TITLE);
        for (World world : Bukkit.getWorlds())
            report.append(world.getName()).newLine();
    }

    private void addDate() {
        report.append("Time: ").append(TIME_FORMAT.format(Instant.now())).newLine();
    }

    private void addJavaVersion() {
        report.append("Java Version: ").append(System.getProperty("java.version"))
                .indent(1).append(System.getProperty("java.vendor")).newLine();
    }

    private void addOperatingSystem() {
        report.append("Operating System: ").append(System.getProperty("os.name"))
                .indent(1).append(System.getProperty("os.version"))
                .indent(1).append(System.getProperty("os.arch")).newLine();
    }

    private void addServerVersion() {
        report.append("Server Version: ").append(Bukkit.getVersion()).newLine();
    }

    private void addMinecraftVersion() {
        report.append("Minecraft Version: ").append(getMinecraftVersion()).newLine();
    }

    private void addAuthenticationStatus() {
        report.append("Authenticated: " + !plugin.isAuthKeyInvalid()).newLine();
    }

    private void addServerId() {
        if (plugin.isAuthKeyInvalid())
            return;
        report.append("Server ID: " + plugin.getServerId()).newLine();
    }

    private void addConnectionStatus() {
        report.append("Connection Lost: " + plugin.isUnableToContactEnjin()).newLine();
    }

    private String getEmpVersion() {
        return "v" + plugin.getDescription().getVersion().split("-")[0];
    }

    private String getMinecraftVersion() {
        return Bukkit.getBukkitVersion().split("-")[0];
    }

    private void zip() {
        try (InputStream is = new ByteArrayInputStream(report.toString().getBytes())) {
            String reportName = "report_" + FILE_FORMAT.format(Instant.now());
            File reportFolder = new File(plugin.getDataFolder(), "/reports/");
            File reportFile = new File(reportFolder, reportName + ".zip");

            if (!reportFolder.exists())
                reportFolder.mkdirs();

            ZipFile zip = new ZipFile(reportFile);
            ZipParameters parameters = new ZipParameters();

            zip.addFile(logFile, parameters);
            zip.addFolder(errorFolder, parameters);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
            parameters.setFileNameInZip(reportName + ".txt");
            parameters.setSourceExternalStream(true);
            zip.addStream(is, parameters);

            Translation.Command_Report_Generated.send(sender, reportFile.getPath());
        } catch (IOException | ZipException ex) {
            Enjin.getLogger().log(ex);
        }
    }

}
