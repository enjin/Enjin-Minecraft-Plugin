package com.enjin.bukkit;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLHandshakeException;

import com.enjin.bukkit.command.CommandBank;
import com.enjin.bukkit.command.commands.*;
import com.enjin.bukkit.config.EnjinConfig;
import com.enjin.bukkit.util.io.EnjinErrorReport;
import com.enjin.bukkit.util.io.EnjinLogAppender;
import com.enjin.bukkit.util.io.EnjinLogFormatter;
import com.enjin.bukkit.util.io.EnjinLogInterface;
import com.enjin.bukkit.listeners.*;
import com.enjin.bukkit.listeners.perm.*;
import com.enjin.bukkit.managers.VaultManager;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.stats.StatsPlayer;
import com.enjin.bukkit.stats.StatsServer;
import com.enjin.bukkit.stats.StatsUtils;
import com.enjin.bukkit.stats.WriteStats;
import com.enjin.bukkit.sync.BukkitInstructionHandler;
import com.enjin.bukkit.sync.RPCPacketManager;
import com.enjin.bukkit.tickets.TicketListener;
import com.enjin.bukkit.tpsmeter.MonitorTPS;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.tickets.Module;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.rpc.mappings.services.TicketService;
import lombok.Getter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.vanish.VanishManager;
import org.kitteh.vanish.VanishPlugin;

import com.enjin.bukkit.threaded.AsyncToSyncEventThrower;
import com.enjin.bukkit.threaded.BanLister;
import com.enjin.bukkit.threaded.PeriodicVoteTask;
import com.enjin.bukkit.threaded.Updater;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.vexsoftware.votifier.Votifier;

public class EnjinMinecraftPlugin extends JavaPlugin implements EnjinPlugin {
    @Getter
    private static EnjinMinecraftPlugin instance;
    @Getter
    private static EnjinConfig configuration;
    @Getter
    private InstructionHandler instructionHandler = new BukkitInstructionHandler();

    private VanishManager vanishmanager = null;
    private static boolean isMcMMOloaded = false;
    private boolean mcMMOSupported = false;
    public boolean supportsglobalgroups = true;
    public boolean votifierinstalled = false;
    public boolean votifiererrored = false;
    public int xpversion = 0;
    private static boolean supportsuuid = false;
    @Getter
    private static boolean mcmmoOutdated = false;
    @Getter
    private boolean tuxtwolibinstalled = false;
    public final static Logger enjinLogger = Logger.getLogger(EnjinMinecraftPlugin.class.getName());
    private EnjinLogInterface mcLogListener = new EnjinLogAppender();
    public StatsServer serverstats = new StatsServer(this);
    public Map<String, StatsPlayer> playerstats = new ConcurrentHashMap<String, StatsPlayer>();
    /**
     * Key is banned player, value is admin that banned the player or blank if the console banned
     */
    public Map<String, String> bannedplayers = new ConcurrentHashMap<String, String>();
    /**
     * Key is banned player, value is admin that pardoned the player or blank if the console pardoned
     */
    public Map<String, String> pardonedplayers = new ConcurrentHashMap<String, String>();
    public String newversion = "";
    public boolean hasupdate = false;
    public boolean updatefailed = false;
    public boolean authkeyinvalid = false;
    public boolean unabletocontactenjin = false;
    public boolean permissionsnotworking = false;
    public static boolean vaultneedsupdating = false;
    public boolean gmneedsupdating = false;
    private LinkedList<String> keywords = new LinkedList<String>();
    public AsyncToSyncEventThrower eventthrower = new AsyncToSyncEventThrower(this);

    //------------Threaded tasks---------------
    public Runnable task;
    public PeriodicVoteTask votetask;
    public BanLister banlistertask;
    //Initialize in the onEnable
    public MonitorTPS tpstask;

    //-------------Thread IDS-------------------
    private int synctaskid = -1;
    private int votetaskid = -1;
    private int banlisttask = -1;
    private int headsupdateid = -1;
    private int updatethread = -1;
    public static boolean usingSSL = true;
    public Map<String, String> playerperms = new ConcurrentHashMap<String, String>();
    //Player, lists voted on.
    public Map<String, List<String>> playervotes = new ConcurrentHashMap<String, List<String>>();
    private Map<String, CommandWrapper> commandids = new ConcurrentHashMap<String, CommandWrapper>();
    public EnjinErrorReport lasterror = null;
    public EnjinStatsListener esl = null;

    //-------------Ticket Service---------------
    @Getter
    private static Map<Integer, Module> modules = new HashMap<Integer, Module>();
    @Getter
    private static long modulesLastPolled = 0;
    @Getter
    private static TicketListener ticketListener;

    @Override
    public void debug(String s) {
        if (configuration.isDebug()) {
            System.out.println("Enjin Debug: " + s);
        }
        if (configuration.isLoggingEnabled()) {
            enjinLogger.fine(s);
        }
    }

    @Override
    public void onEnable() {
        Enjin.setPlugin(this);
        instance = this;

        //Add keywords for item giving
        keywords.add("-name");
        keywords.add("-color");
        keywords.add("-repairxp");
        keywords.add("--n");
        keywords.add("--c");
        keywords.add("--r");

        try {
            initConfig();

            EnjinRPC.setLogger(getLogger());
            EnjinRPC.setDebug(configuration.isDebug());

            VaultManager.init(this);

            initCommands();

            debug("Initializing internal logger");
            enjinLogger.setLevel(Level.FINEST);
            File logsfolder = new File(getDataFolder().getAbsolutePath() + File.separator + "logs");
            if (!logsfolder.exists()) {
                logsfolder.mkdirs();
            }
            File logsfile = new File(getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log");
            if (logsfile.exists()) {
                //Max file size of the enjin log should be less than 5MB.
                if (logsfile.length() > 1024 * 1024 * 5) {
                    logsfile.delete();
                }
            }
            FileHandler fileTxt = new FileHandler(getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log", true);
            EnjinLogFormatter formatterTxt = new EnjinLogFormatter();
            fileTxt.setFormatter(formatterTxt);
            enjinLogger.addHandler(fileTxt);
            enjinLogger.setUseParentHandlers(false);
            debug("Logger initialized.");
            /*
            Append MC log listener to the root logger.
             */
            org.apache.logging.log4j.core.Logger log = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
            log.addAppender((Appender) mcLogListener);

            debug("Get the ban list");
            banlistertask = new BanLister(this);
            debug("Ban list loaded");
            initPlugins();
            debug("Init plugins done.");
            setupPermissions();
            debug("Setup permissions integration");
            setupVotifierListener();
            debug("Setup Votifier integration");

            if (configuration.isCollectPlayerStats()) {
                startStatsCollecting();
                File stats = new File("enjin-stats.json");
                if (stats.exists()) {
                    try {
                        String content = readFile(stats, Charset.forName("UTF-8"));
                        StatsUtils.parseStats(content, this);
                    } catch (Exception e) {
                        getLogger().warning(e.getMessage());
                    }
                }

                Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
            }

            // Bypass key checking, but only if the key looks valid
            initListeners();
            if (configuration.getAuthKey().length() == 50) {
                RPCData<Boolean> data = EnjinServices.getService(PluginService.class).auth(configuration.getAuthKey(), Bukkit.getPort(), true);
                if (data == null) {
                    authkeyinvalid = true;
                    debug("Auth key is invalid. Data could not be retrieved.");
                } else if (data.getError() != null) {
                    authkeyinvalid = true;
                    debug("Auth key is invalid. " + data.getError().getMessage());
                } else if (!data.getResult()) {
                    authkeyinvalid = true;
                    debug("Auth key is invalid. Failed to authenticate.");
                } else {
                    debug("Starting periodic tasks.");
                    initTasks();
                }
            } else {
                authkeyinvalid = true;
                debug("Auth key is invalid. Must be 50 characters in length.");
            }

            // Adding in metrics
            try {
                MetricsLite metrics = new MetricsLite(this);
                metrics.start();
            } catch (IOException e) {
                // Failed to submit the stats :-(
            }
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
            enjinLogger.warning("Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
            t.printStackTrace();
            this.setEnabled(false);
        }

        prepareTicketService();
    }

    private void prepareTicketService() {
        ticketListener = new TicketListener();
        Bukkit.getPluginManager().registerEvents(ticketListener, this);

        pollModules();
    }

    public void stopStatsCollecting() {
        HandlerList.unregisterAll(esl);
    }

    static String readFile(File path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path.getAbsolutePath()));
        return new String(encoded, encoding);
    }

    public static boolean isMcMMOEnabled() {
        return isMcMMOloaded;
    }

    public void startStatsCollecting() {
        if (mcMMOSupported) {
            Plugin mcmmo = this.getServer().getPluginManager().getPlugin("mcMMO");
            if (mcmmo != null) {
                try {
                    //Let's try to load the method, if it fails, catch it.
                    List<SkillType> skills = SkillType.NON_CHILD_SKILLS;
                    isMcMMOloaded = true;
                    debug("mcMMO found, hooking custom stats.");
                } catch (NoSuchFieldError e) {
                    mcmmoOutdated = true;
                    isMcMMOloaded = false;
                    getLogger().warning("Your version of mcMMO is outdated! Please update here: http://dev.bukkit.org/bukkit-plugins/mcmmo/");
                } catch (NoClassDefFoundError e) {
                    mcmmoOutdated = true;
                    isMcMMOloaded = false;
                    getLogger().warning("Your version of mcMMO is outdated! Please update here: http://dev.bukkit.org/bukkit-plugins/mcmmo/");
                } catch (Error e) {
                    mcmmoOutdated = true;
                    isMcMMOloaded = false;
                    getLogger().warning("Your version of mcMMO is outdated! Please update here: http://dev.bukkit.org/bukkit-plugins/mcmmo/");
                }
            } else {
                isMcMMOloaded = false;
            }
        }

        if (esl == null) {
            esl = new EnjinStatsListener(this);
        }

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(esl, this);

        if (!configuration.getStatsCollected().getPlayer().isTravel()) {
            PlayerMoveEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getPlayer().isBlocksBroken()) {
            BlockBreakEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getPlayer().isBlocksPlaced()) {
            BlockPlaceEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getPlayer().isKills()) {
            EntityDeathEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getPlayer().isDeaths()) {
            PlayerDeathEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getPlayer().isXp()) {
            PlayerExpChangeEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getServer().isCreeperExplosions()) {
            EntityExplodeEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getServer().isPlayerKicks()) {
            PlayerKickEvent.getHandlerList().unregister(esl);
        }
    }

    @Override
    public void onDisable() {
        stopTask();
        if (configuration.isCollectPlayerStats()) {
            new WriteStats(this).write("enjin-stats.json");
            debug("Stats saved to enjin-stats.json.");
        }
    }

    public void initConfig() {
        File configFile = new File(getDataFolder(), "config.json");
        EnjinMinecraftPlugin.configuration = JsonConfig.load(configFile, EnjinConfig.class);

        if (!configFile.exists()) {
            configuration.save(configFile);
        }

        if (!configuration.getApiUrl().endsWith("/")) {
            configuration.setApiUrl(configuration.getApiUrl().concat("/"));
        }

        EnjinRPC.setHttps(configuration.isHttps());
        EnjinRPC.setApiUrl(configuration.getApiUrl());
        debug("RPC API Url: " + configuration.getApiUrl());
    }

    private void initCommands() {
        CommandBank.setup(this);
        CommandBank.register(BuyCommand.class, CoreCommands.class, StatCommands.class, HeadCommands.class, SupportCommands.class);

        if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            CommandBank.register(VoteCommands.class);
        }

        if (configuration.getBuyCommand() != null && !configuration.getBuyCommand().isEmpty()) {
            CommandBank.registerCommandAlias("buy", configuration.getBuyCommand());
        }
    }

    public static void saveConfiguration() {
        configuration.save(new File(instance.getDataFolder(), "config.json"));
    }

    public void initTasks() {
        debug("Starting tasks.");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new RPCPacketManager(this), 20L * 60L, 20L * 60L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new BanLister(this), 20L * 2L, 20L * 90L).getTaskId();

        if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            debug("Starting votifier task.");
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, new PeriodicVoteTask(this), 20L * 4L, 20L * 4L).getTaskId();
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new MonitorTPS(), 20L * 2L, 20L * 4L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Updater(this, 44560, this.getFile(), Updater.UpdateType.DEFAULT, true), 20L * 60L * 5L, 20L * 60L * 5L).getTaskId();
    }

    public void initListeners() {
        debug("Initializing Listeners");
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BanListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
    }

    public void stopTask() {
        debug("Stopping tasks.");
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void setupVotifierListener() {
        if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            System.out.println("[Enjin Minecraft Plugin] Votifier plugin found, enabling Votifier support.");
            enjinLogger.info("Votifier plugin found, enabling Votifier support.");
            Bukkit.getPluginManager().registerEvents(new VotifierListener(this), this);
            votifierinstalled = true;

            Plugin vobject = Bukkit.getPluginManager().getPlugin("Votifier");
            if (vobject != null && vobject instanceof Votifier) {
                Votifier votifier = (Votifier) vobject;
                votifiererrored = false;
                if (votifier.getVoteReceiver() == null) {
                    votifiererrored = true;
                }
            }

        }


    }

    private void initPlugins() throws Throwable {
        if (Bukkit.getPluginManager().isPluginEnabled("TuxTwoLib")) {
            tuxtwolibinstalled = true;
            enjinLogger.info("TuxTwoLib is installed. Offline players can be given items.");
            getLogger().info("TuxTwoLib is installed. Offline players can be given items.");
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            enjinLogger.warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
            getLogger().warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
            return;
        }

        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        boolean vaultupdated = false;

        if (vault != null) {
            String[] version = vault.getDescription().getVersion().split("\\.");
            int majorver = Integer.parseInt(version[0]);
            int minorver = Integer.parseInt(version[1]);
            if (majorver > 1) {
                vaultupdated = true;
            } else if (minorver > 3) {
                vaultupdated = true;
            }
        }

        if (!vaultupdated) {
            vaultneedsupdating = true;
            enjinLogger.severe("This version of vault doesn't support UUID! Please update to the latest version here: http://dev.bukkit.org/bukkit-plugins/vault/files/");
            enjinLogger.severe("Disabling vault integration until Vault is updated.");
            getLogger().severe("This version of vault doesn't support UUID! Please update to the latest version here: http://dev.bukkit.org/bukkit-plugins/vault/files/");
            getLogger().severe("Disabling vault integration until Vault is updated.");
            return;
        }

        setupVanishNoPacket();
    }

    private void setupVanishNoPacket() {
        Plugin vanish = Bukkit.getPluginManager().getPlugin("VanishNoPacket");
        if (vanish != null && vanish instanceof VanishPlugin) {
            vanishmanager = ((VanishPlugin) vanish).getManager();
        }
    }

    private void setupPermissions() {
        if (Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")) {
            debug("PermissionsEx found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new PexChangeListener(this), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("bPermissions")) {
            debug("bPermissions found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new BPermissionsListener(this), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("zPermissions")) {
            debug("zPermissions found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new ZPermissionsListener(this), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("PermissionsBukkit")) {
            debug("PermissionsBukkit found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new PermissionsBukkitChangeListener(this), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("GroupManager")) {
            debug("GroupManager found, hooking custom events.");
            supportsglobalgroups = false;
            Bukkit.getPluginManager().registerEvents(new GroupManagerListener(), this);
            return;
        } else {
            debug("No suitable permissions plugin found, falling back to synching on player disconnect.");
            debug("You might want to switch to PermissionsEx, bPermissions, or Essentials GroupManager.");
        }
    }

    public void saveCommandIDs() {
        File dataFolder = getDataFolder();
        File headsfile = new File(dataFolder, "newexecutedcommands.yml");
        YamlConfiguration headsconfig = new YamlConfiguration();
        Iterator<Entry<String, CommandWrapper>> thecodes = commandids.entrySet().iterator();
        while (thecodes.hasNext()) {
            Entry<String, CommandWrapper> code = thecodes.next();
            headsconfig.set(code.getKey() + ".hash", code.getValue().getHash());
            headsconfig.set(code.getKey() + ".result", code.getValue().getResult());
            headsconfig.set(code.getKey() + ".command", code.getValue().getCommand());
        }
        try {
            headsconfig.save(headsfile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getLastLogLine() {
        return mcLogListener.getLine();
    }

    public void addCustomData(ItemStack is, String[] args, OfflinePlayer reciever, int startingpos) {
        for (int i = startingpos; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-name") || args[i].equalsIgnoreCase("--n")) {
                boolean noflags = true;
                i++;
                StringBuilder name = new StringBuilder();
                while (noflags && i < args.length) {
                    if (keywords.contains(args[i].toLowerCase())) {
                        noflags = false;
                        i--;
                    } else {
                        name.append(args[i] + " ");
                        i++;
                    }
                }
                addName(is, ChatColor.translateAlternateColorCodes('&', name.toString().trim()));
            } else if (args[i].equalsIgnoreCase("-color") || args[i].equalsIgnoreCase("--c")) {
                i++;
                if (args.length > i) {
                    try {
                        String[] rgb = args[i].split(",");
                        int r = 0;
                        int g = 0;
                        int b = 0;
                        for (String col : rgb) {
                            col = col.toLowerCase();
                            if (col.startsWith("r")) {
                                r = Integer.parseInt(col.substring(1));
                            } else if (col.startsWith("g")) {
                                g = Integer.parseInt(col.substring(1));
                            } else if (col.startsWith("b")) {
                                b = Integer.parseInt(col.substring(1));
                            }
                        }
                        ItemMeta meta = is.getItemMeta();
                        if (meta instanceof LeatherArmorMeta) {
                            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(r, g, b));
                            is.setItemMeta(meta);
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            } else if (args[i].equalsIgnoreCase("-repairxp") || args[i].equalsIgnoreCase("--r")) {
                i++;
                if (args.length > i) {
                    try {
                        int repaircost = Integer.parseInt(args[i]);
                        ItemMeta meta = is.getItemMeta();
                        if (meta instanceof Repairable) {
                            ((Repairable) meta).setRepairCost(repaircost);
                            is.setItemMeta(meta);
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            }
        }
    }

    public ItemStack addName(ItemStack is, String name) {
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        is.setItemMeta(meta);
        return is;
    }

    public void pollModules() {
        if (System.currentTimeMillis() - modulesLastPolled > 10 * 60 * 1000) {
            modulesLastPolled = System.currentTimeMillis();
            RPCData<Map<Integer, Module>> data = EnjinServices.getService(TicketService.class).getModules(EnjinMinecraftPlugin.getConfiguration().getAuthKey());

            if (data == null || data.getError() != null) {
                debug(data == null ? "Could not retrieve support modules." : data.getError().getMessage());
                modules.clear();
                return;
            }

            if (data.getResult().size() == 0) {
                modules.clear();
            }

            for (Entry<Integer, Module> entry : data.getResult().entrySet()) {
                modules.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
