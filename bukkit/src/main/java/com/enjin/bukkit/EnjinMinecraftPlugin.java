package com.enjin.bukkit;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.enjin.bukkit.command.CommandBank;
import com.enjin.bukkit.command.commands.*;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.config.ExecutedCommandsConfig;
import com.enjin.bukkit.config.RankUpdatesConfig;
import com.enjin.bukkit.listeners.perm.PermissionListener;
import com.enjin.bukkit.listeners.perm.processors.*;
import com.enjin.bukkit.managers.*;
import com.enjin.bukkit.util.Log;
import com.enjin.bukkit.util.VersionUtil;
import com.enjin.bukkit.util.io.EnjinErrorReport;
import com.enjin.bukkit.listeners.*;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.stats.StatsPlayer;
import com.enjin.bukkit.stats.StatsServer;
import com.enjin.bukkit.sync.BukkitInstructionHandler;
import com.enjin.bukkit.sync.RPCPacketManager;
import com.enjin.bukkit.tasks.TPSMonitor;
import com.enjin.bukkit.util.ui.MenuAPI;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PluginService;
import com.google.common.base.Optional;
import lombok.Getter;

import lombok.Setter;
import org.bukkit.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.enjin.bukkit.tasks.BanLister;
import com.enjin.bukkit.tasks.CurseUpdater;

public class EnjinMinecraftPlugin extends JavaPlugin implements EnjinPlugin {
    @Getter
    private static EnjinMinecraftPlugin instance;
    @Getter
    private static ExecutedCommandsConfig executedCommandsConfiguration;
    @Getter
    private static RankUpdatesConfig rankUpdatesConfiguration;
    @Getter
    private InstructionHandler instructionHandler = new BukkitInstructionHandler();
    @Getter
    private boolean firstRun = true;
    @Getter
    private MenuAPI menuAPI;

    @Getter
    private String mcVersion;

    @Getter
    private boolean tuxTwoLibInstalled = false;
    @Getter
    private boolean globalGroupsSupported = true;
    @Getter
    private StatsServer serverStats = new StatsServer(this);
    @Getter
    private Map<String, StatsPlayer> playerStats = new ConcurrentHashMap<>();
    /**
     * Key is banned player, value is admin that banned the player or blank if the console banned
     */
    @Getter
    private Map<String, String> bannedPlayers = new ConcurrentHashMap<>();
    /**
     * Key is banned player, value is admin that pardoned the player or blank if the console pardoned
     */
    @Getter
    private Map<String, String> pardonedPlayers = new ConcurrentHashMap<>();
    @Getter @Setter
    private String newVersion = "";
    @Getter @Setter
    private boolean hasUpdate = false;
    @Getter @Setter
    private boolean updateFailed = false;
    @Getter @Setter
    private boolean authKeyInvalid = false;
    @Getter @Setter
    private boolean unableToContactEnjin = false;
    @Getter @Setter
    private boolean permissionsNotWorking = false;

    @Getter
    private PermissionListener permissionListener;

    @Getter
    private Map<String, List<Object[]>> playerVotes = new ConcurrentHashMap<>();
    @Getter @Setter
    private EnjinErrorReport lastError = null;

    @Override
    public void debug(String s) {
        Enjin.getLogger().debug(s);
    }

    @Override
    public void onEnable() {
        instance = this;
        Enjin.setPlugin(instance);
        init();
    }

    @Override
    public void onDisable() {
        disableTasks();
        disableManagers();
    }

    public boolean initVersion() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        String[] versionParts = bukkitVersion.split("-");
        mcVersion = versionParts.length >= 1 ? versionParts[0] : "UNKNOWN";

        return true;
    }

    public void init() {
        if (authKeyInvalid) {
            return;
        }

        if (firstRun) {
            firstRun = false;
            initConfig();

            Enjin.setLogger(new Log(getDataFolder()));

            if (!initVersion()) {
                return;
            }

            initCommands();
            debug("Init commands done.");

            if (Enjin.getConfiguration().getAuthKey().length() == 50) {
                RPCData<Boolean> data = EnjinServices.getService(PluginService.class).auth(Optional.<String>absent(), Bukkit.getPort(), true);
                if (data == null) {
                    authKeyInvalid = true;
                    debug("Auth key is invalid. Data could not be retrieved.");
                    return;
                } else if (data.getError() != null) {
                    authKeyInvalid = true;
                    debug("Auth key is invalid. " + data.getError().getMessage());
                    return;
                } else if (!data.getResult()) {
                    authKeyInvalid = true;
                    debug("Auth key is invalid. Failed to authenticate.");
                    return;
                }
            } else {
                authKeyInvalid = true;
                debug("Auth key is invalid. Must be 50 characters in length.");
                return;
            }
        }

        menuAPI = new MenuAPI(this);
        debug("Init gui api done.");
        initManagers();
        debug("Init managers done.");
        initPlugins();
        debug("Init plugins done.");
        initPermissions();
        debug("Init permissions done.");
        initListeners();
        debug("Init listeners done.");
        initTasks();
        debug("Init tasks done.");

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            debug("Failed to start metrics.");
        }
    }

    public void initConfig() {
        File configFile = new File(getDataFolder(), "config.json");
        EMPConfig configuration = JsonConfig.load(configFile, EMPConfig.class);
        Enjin.setConfiguration(configuration);

        if (!configFile.exists()) {
            configuration.save(configFile);
        }

        configFile = new File(getDataFolder(), "commands.json");
        EnjinMinecraftPlugin.executedCommandsConfiguration = JsonConfig.load(configFile, ExecutedCommandsConfig.class);

        if (!configFile.exists()) {
            executedCommandsConfiguration.save(configFile);
        }

        configFile = new File(getDataFolder(), "rankUpdates.json");
        EnjinMinecraftPlugin.rankUpdatesConfiguration = JsonConfig.load(configFile, RankUpdatesConfig.class);

        if (!configFile.exists()) {
            rankUpdatesConfiguration.save(configFile);
        }
    }

    public static void saveConfiguration() {
        Enjin.getConfiguration().save(new File(instance.getDataFolder(), "config.json"));
    }

    public static void saveExecutedCommandsConfiguration() {
        executedCommandsConfiguration.save(new File(instance.getDataFolder(), "commands.json"));
    }

    public static void saveRankUpdatesConfiguration() {
        rankUpdatesConfiguration.save(new File(instance.getDataFolder(), "rankUpdates.json"));
    }

    private void initCommands() {
        CommandBank.setup(this);
        CommandBank.register(BuyCommand.class, CoreCommands.class, StatCommands.class,
                HeadCommands.class, SupportCommands.class, PointCommands.class, ConfigCommand.class);

        if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            CommandBank.register(VoteCommands.class);
        }

        if (Enjin.getConfiguration(EMPConfig.class).getBuyCommand() != null && !Enjin.getConfiguration(EMPConfig.class).getBuyCommand().isEmpty()) {
            CommandBank.registerCommandAlias("buy", Enjin.getConfiguration(EMPConfig.class).getBuyCommand());
        }
    }

    private void initManagers() {
        PurchaseManager.init();
        VaultManager.init(this);
        VotifierManager.init(this);
        TicketManager.init(this);
        StatsManager.init(this);
        StatSignManager.init(this);
    }

    private void disableManagers() {
        StatsManager.disable(this);
        StatSignManager.disable();
    }

    public void initTasks() {
        debug("Starting tasks.");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new RPCPacketManager(this), 20L * 60L, 20L * 60L);
        if (Enjin.getConfiguration(EMPConfig.class).isListenForBans()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, new BanLister(this), 20L * 2L, 20L * 90L);
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new TPSMonitor(), 20L * 2L, 20L * 2L);
        if (Enjin.getConfiguration().isAutoUpdate() && isUpdateFromCurseForge()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, new CurseUpdater(this, 44560, this.getFile(), CurseUpdater.UpdateType.DEFAULT, true), 0, 20L * 60L * 30L);
        }
    }

    public void disableTasks() {
        debug("Stopping tasks.");
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void initListeners() {
        debug("Initializing Listeners");
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BanListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
    }

    private void initPlugins() {
        if (Bukkit.getPluginManager().isPluginEnabled("TuxTwoLib")) {
            tuxTwoLibInstalled = true;
            Enjin.getLogger().info("TuxTwoLib is installed. Offline players can be given items.");
            getLogger().info("TuxTwoLib is installed. Offline players can be given items.");
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Enjin.getLogger().warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
            getLogger().warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
            return;
        }
    }

    private void initPermissions() {
        if (Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")) {
            debug("PermissionsEx found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new PexListener(), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("bPermissions")) {
            debug("bPermissions found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new BPermissionsListener(), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("zPermissions")) {
            debug("zPermissions found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new ZPermissionsListener(), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("PermissionsBukkit")) {
            debug("PermissionsBukkit found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new PermissionsBukkitListener(), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("GroupManager")) {
            debug("GroupManager found, hooking custom events.");
            globalGroupsSupported = false;
            Bukkit.getPluginManager().registerEvents(permissionListener = new GroupManagerListener(), this);
            return;
        } else {
            debug("No suitable permissions plugin found, falling back to synching on player disconnect.");
            debug("You might want to switch to PermissionsEx, bPermissions, or Essentials GroupManager.");
        }
    }

    public static void dispatchConsoleCommand(String command) {
        if (!CommandBank.getNodes().containsKey(command.split(" ")[0])) {
            Enjin.getPlugin().debug("[D1] Executed Command: " + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return;
        }

        Enjin.getPlugin().debug("[D2] Executed Command: " + command);
        Bukkit.getPluginManager().callEvent(new ServerCommandEvent(Bukkit.getConsoleSender(), command));
    }

    public boolean isUpdateFromCurseForge() {
        return getDescription().getVersion().endsWith("-bukkit");
    }
}
