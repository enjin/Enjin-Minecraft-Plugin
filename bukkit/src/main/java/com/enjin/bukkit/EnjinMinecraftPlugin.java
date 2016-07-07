package com.enjin.bukkit;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.enjin.bukkit.command.CommandBank;
import com.enjin.bukkit.command.commands.*;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.config.ExecutedCommandsConfig;
import com.enjin.bukkit.config.RankUpdatesConfig;
import com.enjin.bukkit.listeners.perm.PermissionListener;
import com.enjin.bukkit.listeners.perm.processors.*;
import com.enjin.bukkit.modules.ModuleManager;
import com.enjin.bukkit.modules.impl.*;
import com.enjin.bukkit.util.Log;
import com.enjin.bukkit.util.Plugins;
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
import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.enjin.bukkit.tasks.BanLister;
import com.enjin.bukkit.tasks.CurseUpdater;

public class EnjinMinecraftPlugin extends JavaPlugin implements EnjinPlugin {
    @Getter
    private static EnjinMinecraftPlugin instance;
    private static ExecutedCommandsConfig executedCommandsConfiguration;
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

    @Getter @Setter
    private EnjinErrorReport lastError = null;

	@Getter
	private ModuleManager moduleManager = null;

    @Override
    public void onEnable() {
        instance = this;
        Enjin.setPlugin(instance);
        init();
        Enjin.getLogger().debug("Enabled Enjin Minecraft Plugin w/ Object Reference: " + ObjectUtils.identityToString(this.toString()));
    }

    @Override
    public void onDisable() {
        disableTasks();
        disableManagers();
        Enjin.getLogger().debug("Disabled Enjin Minecraft Plugin w/ Object Reference: " + ObjectUtils.identityToString(this.toString()));
    }

    public void initVersion() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        String[] versionParts = bukkitVersion.split("-");
        mcVersion = versionParts.length >= 1 ? versionParts[0] : "UNKNOWN";
    }

    public void init() {
        if (firstRun) {
            Log log = new Log(getDataFolder());
            Enjin.setLogger(log);

            Enjin.getLogger().info("Initializing for the first time.");
            initConfigs();
            log.configure();
            log.setDebug(Enjin.getConfiguration().isDebug());

            try {
                MetricsLite metrics = new MetricsLite(this);
                metrics.start();
            } catch (IOException e) {
                Enjin.getLogger().debug("Failed to start metrics.");
            }

			menuAPI = new MenuAPI(this);

			moduleManager = new ModuleManager(this);

            if (Enjin.getConfiguration().getAuthKey().length() == 50) {
                RPCData<Boolean> data = EnjinServices.getService(PluginService.class).auth(Optional.<String>absent(), Bukkit.getPort(), true);
                if (data == null) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. Data could not be retrieved.");
                } else if (data.getError() != null) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. " + data.getError().getMessage());
                } else if (!data.getResult()) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. Failed to authenticate.");
                }
            } else {
                authKeyInvalid = true;
                Enjin.getLogger().debug("Auth key is invalid. Must be 50 characters in length.");
            }

            initVersion();
            Enjin.getLogger().debug("Init version done.");
            initCommands();
            Enjin.getLogger().debug("Init commands done.");
            initListeners();
            Enjin.getLogger().debug("Init listeners done.");

            firstRun = false;
        }

        if (authKeyInvalid) {
            Enjin.getLogger().debug("Auth key is invalid. Stopping initialization.");
            return;
        }

        Enjin.getLogger().debug("Init gui api done.");
        moduleManager.init();
        Enjin.getLogger().debug("Init modules done.");
        initPlugins();
        Enjin.getLogger().debug("Init plugins done.");

		if (Plugins.isEnabled("Vault")) {
			initPermissions();
            Enjin.getLogger().debug("Init permissions done.");
		}

        initTasks();
        Enjin.getLogger().debug("Init tasks done.");
    }

    public void initConfigs() {
        initConfig();
        initCommandsConfiguration();
        initRankUpdatesConfiguration();
    }

    public void initConfig() {
        File configFile = new File(getDataFolder(), "config.json");
        EMPConfig configuration = JsonConfig.load(configFile, EMPConfig.class);
        Enjin.setConfiguration(configuration);

        if (!configFile.exists()) {
            configuration.save(configFile);
        }
    }

    private void initCommandsConfiguration() {
        File configFile = new File(getDataFolder(), "commands.json");
        EnjinMinecraftPlugin.executedCommandsConfiguration = JsonConfig.load(configFile, ExecutedCommandsConfig.class);

        if (!configFile.exists()) {
            executedCommandsConfiguration.save(configFile);
        }
    }

    private void initRankUpdatesConfiguration() {
        File configFile = new File(getDataFolder(), "rankUpdates.json");
        EnjinMinecraftPlugin.rankUpdatesConfiguration = JsonConfig.load(configFile, RankUpdatesConfig.class);

        if (!configFile.exists()) {
            rankUpdatesConfiguration.save(configFile);
        }
    }

    public static void saveConfiguration() {
        if (Enjin.getConfiguration() == null) {
            instance.initConfig();
        }

        Enjin.getConfiguration().save(new File(instance.getDataFolder(), "config.json"));
    }

    public static void saveExecutedCommandsConfiguration() {
        if (executedCommandsConfiguration == null) {
            instance.initCommandsConfiguration();
        }

        executedCommandsConfiguration.save(new File(instance.getDataFolder(), "commands.json"));
    }

    public static void saveRankUpdatesConfiguration() {
        if (rankUpdatesConfiguration == null) {
            instance.initRankUpdatesConfiguration();
        }

        rankUpdatesConfiguration.save(new File(instance.getDataFolder(), "rankUpdates.json"));
    }

    private void initCommands() {
        CommandBank.setup(this);
        CommandBank.register(BuyCommand.class, CoreCommands.class, StatCommands.class,
                HeadCommands.class, SupportCommands.class, PointCommands.class, ConfigCommand.class);

        if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            CommandBank.register(VoteCommands.class);
        }

		String buyCommand = Enjin.getConfiguration(EMPConfig.class).getBuyCommand();
		if (buyCommand != null && !buyCommand.isEmpty() && !CommandBank.isCommandRegistered(buyCommand)) {
			CommandBank.replaceCommandWithAlias("buy", buyCommand);
		}
    }

    private void disableManagers() {
		StatsModule stats = moduleManager.getModule(StatsModule.class);
		SignStatsModule signStats = moduleManager.getModule(SignStatsModule.class);

        if (stats != null) {
			stats.disable();
		}

		if (signStats != null) {
			signStats.disable();
		}
    }

    public void initTasks() {
        Enjin.getLogger().debug("Starting tasks.");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new RPCPacketManager(this), 20L * 60L, 20L * 60L);
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new TPSMonitor(), 20L * 2L, 20L * 2L);

        if (Enjin.getConfiguration(EMPConfig.class).isListenForBans()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, new BanLister(this), 20L * 2L, 20L * 90L);
        }

        if (Enjin.getConfiguration().isAutoUpdate() && isUpdateFromCurseForge()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, new CurseUpdater(this, 44560, this.getFile(), CurseUpdater.UpdateType.DEFAULT, true), 0, 20L * 60L * 30L);
        }
    }

    public void disableTasks() {
        Enjin.getLogger().debug("Stopping tasks.");
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void initListeners() {
        Enjin.getLogger().debug("Initializing Listeners");
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
    }

    private void initPermissions() {
        if (Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")) {
            Enjin.getLogger().debug("PermissionsEx found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new PexListener(), this);
        } else if (Bukkit.getPluginManager().isPluginEnabled("bPermissions")) {
            Enjin.getLogger().debug("bPermissions found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new BPermissionsListener(), this);
        } else if (Bukkit.getPluginManager().isPluginEnabled("zPermissions")) {
            Enjin.getLogger().debug("zPermissions found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new ZPermissionsListener(), this);
        } else if (Bukkit.getPluginManager().isPluginEnabled("PermissionsBukkit")) {
            Enjin.getLogger().debug("PermissionsBukkit found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new PermissionsBukkitListener(), this);
        } else if (Bukkit.getPluginManager().isPluginEnabled("GroupManager")) {
            Enjin.getLogger().debug("GroupManager found, hooking custom events.");
            globalGroupsSupported = false;
            Bukkit.getPluginManager().registerEvents(permissionListener = new GroupManagerListener(), this);
        } else {
            Enjin.getLogger().debug("No suitable permissions plugin found, falling back to synching on player disconnect.");
            Enjin.getLogger().debug("You might want to switch to PermissionsEx, bPermissions, or Essentials GroupManager.");
        }
    }

    public static void dispatchConsoleCommand(String command) {
        if (!CommandBank.getNodes().containsKey(command.split(" ")[0].toLowerCase())) {
            Enjin.getLogger().debug("[D1] Executed Command: " + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return;
        }

        Enjin.getLogger().debug("[D2] Executed Command: " + command);
        Bukkit.getPluginManager().callEvent(new ServerCommandEvent(Bukkit.getConsoleSender(), command));
    }

    public boolean isUpdateFromCurseForge() {
        return getDescription().getVersion().endsWith("-bukkit");
    }

    public static ExecutedCommandsConfig getExecutedCommandsConfiguration() {
        if (executedCommandsConfiguration == null) {
            instance.initCommandsConfiguration();
        }

        return executedCommandsConfiguration;
    }

    public static RankUpdatesConfig getRankUpdatesConfiguration() {
        if (rankUpdatesConfiguration == null) {
            instance.initRankUpdatesConfiguration();
        }

        return rankUpdatesConfiguration;
    }
}
