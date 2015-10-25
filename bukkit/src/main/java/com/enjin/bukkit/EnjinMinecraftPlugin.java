package com.enjin.bukkit;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import com.enjin.bukkit.compatibility.NewPlayerGetter;
import com.enjin.bukkit.compatibility.OldPlayerGetter;
import com.enjin.bukkit.config.EnjinConfig;
import com.enjin.bukkit.permlisteners.*;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.stats.StatsPlayer;
import com.enjin.bukkit.stats.StatsServer;
import com.enjin.bukkit.stats.StatsUtils;
import com.enjin.bukkit.stats.WriteStats;
import com.enjin.bukkit.sync.BukkitInstructionHandler;
import com.enjin.bukkit.sync.RPCPacketManager;
import com.enjin.bukkit.tickets.TicketCreationSession;
import com.enjin.bukkit.tickets.TicketListener;
import com.enjin.bukkit.tickets.TicketViewBuilder;
import com.enjin.bukkit.tpsmeter.MonitorTPS;
import com.enjin.bukkit.util.PacketUtilities;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.bukkit.listeners.CommandListener;
import com.enjin.officialplugin.permlisteners.*;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.general.RPCSuccess;
import com.enjin.rpc.mappings.mappings.tickets.Module;
import com.enjin.rpc.mappings.mappings.tickets.Reply;
import com.enjin.rpc.mappings.mappings.tickets.Ticket;
import com.enjin.rpc.mappings.mappings.tickets.TicketStatus;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.rpc.mappings.services.TicketService;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.permission.plugins.Permission_GroupManager;

import org.anjocaido.groupmanager.GroupManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.kitteh.vanish.VanishManager;
import org.kitteh.vanish.VanishPlugin;

import Tux2.TuxTwoLib.TuxTwoPlayer;

import com.enjin.bukkit.compatibility.OnlinePlayerGetter;
import com.enjin.bukkit.listeners.EnjinStatsListener;
import com.enjin.bukkit.listeners.NewPlayerChatListener;
import com.enjin.bukkit.listeners.VotifierListener;
import com.enjin.bukkit.threaded.AsyncToSyncEventThrower;
import com.enjin.bukkit.threaded.BanLister;
import com.enjin.bukkit.threaded.CommandExecuter;
import com.enjin.bukkit.threaded.ConfigSender;
import com.enjin.bukkit.threaded.DelayedCommandExecuter;
import com.enjin.bukkit.threaded.NewKeyVerifier;
import com.enjin.bukkit.threaded.PeriodicVoteTask;
import com.enjin.bukkit.threaded.ReportMakerThread;
import com.enjin.bukkit.threaded.Updater;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import com.vexsoftware.votifier.Votifier;

import de.bananaco.bpermissions.imp.Permissions;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsPlugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2013.
 */

public class EnjinMinecraftPlugin extends JavaPlugin implements EnjinPlugin {
    public static EnjinMinecraftPlugin instance;
    public static EnjinConfig config;
    private InstructionHandler instructionHandler = new BukkitInstructionHandler();
    public static boolean usingGroupManager = false;
    public Server s;
    public Logger logger;
    public static Permission permission = null;
    public static Economy economy = null;
    public PermissionsEx permissionsex;
    public GroupManager groupmanager;
    public Permissions bpermissions;
    public PermissionsPlugin permissionsbukkit;
    public ZPermissionsPlugin zPermissions;
    private VanishManager vanishmanager = null;
    private static boolean isMcMMOloaded = false;
    private boolean mcMMOSupported = false;
    public boolean supportsglobalgroups = true;
    public boolean votifierinstalled = false;
    protected boolean votifiererrored = false;
    public int xpversion = 0;
    static int logversion = 1;
    static boolean supportsuuid = false;
    static boolean mcmmoOutdated = false;
    public String mcversion = "";
    boolean listenforbans = true;
    boolean tuxtwolibinstalled = false;
    public final static Logger enjinlogger = Logger.getLogger(EnjinMinecraftPlugin.class.getName());
    public CommandExecuter commandqueue = new CommandExecuter(this);
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
    private EnjinLogInterface mcloglistener = null;
    public String newversion = "";
    public boolean hasupdate = false;
    public boolean updatefailed = false;
    public boolean authkeyinvalid = false;
    public boolean unabletocontactenjin = false;
    public boolean permissionsnotworking = false;
    public static boolean vaultneedsupdating = false;
    public boolean gmneedsupdating = false;
    public static boolean econcompatmode = false;
    static public boolean bukkitversion = false;
    private boolean isglowstone = false;
    private LinkedList<String> keywords = new LinkedList<String>();
    public OnlinePlayerGetter playergetter = null;
    public AsyncToSyncEventThrower eventthrower = new AsyncToSyncEventThrower(this);
    public final EMPListener listener = new EMPListener(this);

    //------------Threaded tasks---------------
    public Runnable task;
    public PeriodicVoteTask votetask;
    public BanLister banlistertask;
    public DelayedCommandExecuter commexecuter = new DelayedCommandExecuter(this);
    //Initialize in the onEnable
    public MonitorTPS tpstask;

    //-------------Thread IDS-------------------
    private int synctaskid = -1;
    private int votetaskid = -1;
    private int banlisttask = -1;
    private int tpstaskid = -1;
    private int commandexecutertask = -1;
    private int headsupdateid = -1;
    private int updatethread = -1;
    public static final ExecutorService exec = Executors.newCachedThreadPool();
    public static String minecraftport;
    public static boolean usingSSL = true;
    private NewKeyVerifier verifier = null;
    public Map<String, String> playerperms = new ConcurrentHashMap<String, String>();
    //Player, lists voted on.
    public Map<String, List<String>> playervotes = new ConcurrentHashMap<String, List<String>>();
    private Map<String, CommandWrapper> commandids = new ConcurrentHashMap<String, CommandWrapper>();
    public EnjinErrorReport lasterror = null;
    public EnjinStatsListener esl = null;
    public DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

    //-------------Ticket Service---------------
    @Getter
    private static Map<Integer, Module> modules = new HashMap<Integer, Module>();
    @Getter
    private static long modulesLastPolled = 0;
    @Getter
    private static TicketListener ticketListener;

    @Override
    public void debug(String s) {
        if (config.isDebug()) {
            System.out.println("Enjin Debug: " + s);
        }
        if (config.isLoggingEnabled()) {
            enjinlogger.fine(s);
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
            File oldnewdatafolder = new File(getDataFolder().getParent(), "Enjin_Minecraft_Plugin");
            File olddatafolder = new File(getDataFolder().getParent(), "Enjin Minecraft Plugin");
            if (oldnewdatafolder.exists()) {
                try {
                    oldnewdatafolder.renameTo(getDataFolder());
                    reloadConfig();
                } catch (Exception e) {

                }
            } else if (olddatafolder.exists()) {
                try {
                    olddatafolder.renameTo(getDataFolder());
                    reloadConfig();
                } catch (Exception e) {

                }
            }

            initConfig();

            EnjinRPC.setLogger(logger);
            EnjinRPC.setDebug(config.isDebug());

            task = new RPCPacketManager(this);
            votetask = new PeriodicVoteTask(this);

            debug("Initializing internal logger");
            enjinlogger.setLevel(Level.FINEST);
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
            enjinlogger.addHandler(fileTxt);
            enjinlogger.setUseParentHandlers(false);
            debug("Logger initialized.");
            debug("Begin init");
            initVariables();
            debug("Init vars done.");

            //Let's get the minecraft version.
            debug("Minecraft version: " + getServer().getVersion());
            Matcher mcmatch;
            if (isglowstone) {
                Pattern pmcversion = Pattern.compile("(\\d+)\\.(\\d+)\\.?(\\d*)");
                mcmatch = pmcversion.matcher(getServer().getVersion());
            } else {
                String[] cbversionstring = getServer().getVersion().split(":");
                Pattern pmcversion = Pattern.compile("(\\d+)\\.(\\d+)\\.?(\\d*)");
                mcmatch = pmcversion.matcher(cbversionstring[1]);
            }
            if (mcmatch.find()) {
                try {
                    int majorversion = Integer.parseInt(mcmatch.group(1));
                    int minorversion = Integer.parseInt(mcmatch.group(2));
                    int buildnumber = 0;
                    if (mcmatch.group(3) != null && !mcmatch.group(3).equals("")) {
                        try {
                            buildnumber = Integer.parseInt(mcmatch.group(3));
                        } catch (NumberFormatException e) {

                        }
                    }
                    boolean newplayergetterversion = false;
                    mcversion = majorversion + "." + minorversion + "." + buildnumber;
                    debug("MC Version string: " + mcversion);
                    if (majorversion == 1) {
                        if (minorversion > 2) {
                            xpversion = 1;
                            logger.info("[EnjinMinecraftPlugin] MC 1.3 or above found, enabling version 2 XP handling.");
                        } else {
                            logger.info("[EnjinMinecraftPlugin] MC 1.2 or below found, enabling version 1 XP handling.");
                        }
                        if (minorversion > 5) {
                            mcMMOSupported = true;
                        }
                        if (minorversion > 6) {
                            logversion = 2;
                            if (minorversion == 7) {
                                if (buildnumber > 8) {
                                    supportsuuid = true;
                                }
                                if (buildnumber > 9) {
                                    newplayergetterversion = true;
                                }
                            } else if (minorversion > 7) {
                                supportsuuid = true;
                                newplayergetterversion = true;
                            }
                            logger.info("[EnjinMinecraftPlugin] MC 1.7.2 or above found, enabling version 2 log handling.");
                        } else {
                            logger.info("[EnjinMinecraftPlugin] MC 1.6.4 or below found, enabling version 1 log handling.");
                        }
                    } else if (majorversion > 1) {
                        xpversion = 1;
                        logger.info("[EnjinMinecraftPlugin] MC 1.3 or above found, enabling version 2 XP handling.");
                        logversion = 2;
                        logger.info("[EnjinMinecraftPlugin] MC 1.7.2 or above found, enabling version 2 log handling.");
                        supportsuuid = true;
                        mcMMOSupported = true;
                        newplayergetterversion = true;
                    }
                    if (newplayergetterversion) {
                        logger.info("[EnjinMinecraftPlugin] MC 1.7.10 or above found, enabling version 2 player handling.");
                        playergetter = new NewPlayerGetter();
                    } else {
                        logger.info("[EnjinMinecraftPlugin] MC 1.7.9 or below found, enabling version 1 player handling.");
                        playergetter = new OldPlayerGetter();
                    }
                } catch (Exception e) {
                    logger.severe("[EnjinMinecraftPlugin] Unable to get server version! Inaccurate XP and log handling may occurr!");
                    logger.severe("[EnjinMinecraftPlugin] Server Version String: " + getServer().getVersion());
                }
            } else {
                logger.severe("[EnjinMinecraftPlugin] Unable to get server version! Inaccurate XP and log handling may occurr!");
                logger.severe("[EnjinMinecraftPlugin] Server Version String: " + getServer().getVersion());
            }


            if (logversion == 2 && !isglowstone) {
                org.apache.logging.log4j.core.Logger jlogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
                mcloglistener = new EnjinLogAppender();
                jlogger.addAppender((Appender) mcloglistener);
            } else {
                Logger logger2 = getLogger().getParent();
                debug("Top logger: " + logger2.getName());
                mcloglistener = new EnjinLogHandler();
                logger2.addHandler((Handler) mcloglistener);
            }

            debug("Get the ban list");
            banlistertask = new BanLister(this);
            debug("Ban list loaded");
            loadCommandIDs();
            debug("Init files done.");
            initPlugins();
            debug("Init plugins done.");
            setupPermissions();
            debug("Setup permissions integration");
            setupVotifierListener();
            debug("Setup Votifier integration");

            //------We should do TPS even if we have an invalid auth key
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, tpstask = new MonitorTPS(this), 40, 40);

            Thread configthread = new Thread(new ConfigSender(this));
            configthread.start();

            if (config.isCollectPlayerStats()) {
                startStatsCollecting();
                File stats = new File("enjin-stats.json");
                if (stats.exists()) {
                    //Let's not error when we fail to parse the stats.
                    try {
                        String content = readFile(stats, Charset.forName("UTF-8"));
                        StatsUtils.parseStats(content, this);
                    } catch (Exception e) {

                    }
                }
                //XP handling and chat event handling changed at 1.3, so we can use the same variable. :D
                if (xpversion < 1) {
                    //We only keep this around for backwards compatibility with tekkit as it is still on 1.2.5
                    getLogger().severe("This version of the Enjin Minecraft Plugin does not support Tekkit Classic! Please downgrade to version 2.4.0.");
                } else {
                    Bukkit.getPluginManager().registerEvents(new NewPlayerChatListener(this), this);
                }
            }
            usingGroupManager = (permission instanceof Permission_GroupManager);
            //debug("Checking key valid.");
            //Bypass key checking, but only if the key looks valid
            registerEvents();
            if (config.getAuthKey().length() == 50) {
                RPCData<Boolean> data = EnjinServices.getService(PluginService.class).auth(config.getAuthKey(), Bukkit.getPort(), true);
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
                    startTask();
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
            enjinlogger.warning("Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
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

    public OnlinePlayerGetter getPlayerGetter() {
        return playergetter;
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

        if (!config.getStatsCollected().getPlayer().isTravel()) {
            PlayerMoveEvent.getHandlerList().unregister(esl);
        }
        if (!config.getStatsCollected().getPlayer().isBlocksBroken()) {
            BlockBreakEvent.getHandlerList().unregister(esl);
        }
        if (!config.getStatsCollected().getPlayer().isBlocksPlaced()) {
            BlockPlaceEvent.getHandlerList().unregister(esl);
        }
        if (!config.getStatsCollected().getPlayer().isKills()) {
            EntityDeathEvent.getHandlerList().unregister(esl);
        }
        if (!config.getStatsCollected().getPlayer().isDeaths()) {
            PlayerDeathEvent.getHandlerList().unregister(esl);
        }
        if (!config.getStatsCollected().getPlayer().isXp()) {
            PlayerExpChangeEvent.getHandlerList().unregister(esl);
        }
        if (!config.getStatsCollected().getServer().isCreeperExplosions()) {
            EntityExplodeEvent.getHandlerList().unregister(esl);
        }
        if (!config.getStatsCollected().getServer().isPlayerKicks()) {
            PlayerKickEvent.getHandlerList().unregister(esl);
        }
    }

    @Override
    public void onDisable() {
        stopTask();
        if (config.isCollectPlayerStats()) {
            new WriteStats(this).write("enjin-stats.json");
            debug("Stats saved to enjin-stats.json.");
        }
    }

    private void initVariables() throws Throwable {
        s = Bukkit.getServer();
        logger = Bukkit.getLogger();
        File bukkitproperties = new File("server.properties");
        File glowstoneproperties = new File("config/glowstone.yml");
        if (bukkitproperties.exists()) {
            try {
                Properties serverProperties = new Properties();
                FileInputStream in = new FileInputStream(new File("server.properties"));
                serverProperties.load(in);
                in.close();
                minecraftport = serverProperties.getProperty("server-port");
            } catch (Throwable t) {
                t.printStackTrace();
                enjinlogger.severe("Couldn't find a localhost ip! Please report this problem!");
                throw new Exception("[Enjin Minecraft Plugin] Couldn't find a localhost ip! Please report this problem!");
            }
        } else if (glowstoneproperties.exists()) {
            isglowstone = true;
            YamlConfiguration glowstoneconfig = new YamlConfiguration();
            try {
                glowstoneconfig.load(glowstoneproperties);
                minecraftport = glowstoneconfig.getString("server.port", "25565");
            } catch (Exception e) {
                e.printStackTrace();
                enjinlogger.severe("Couldn't find a localhost ip! Please report this problem!");
                throw new Exception("[Enjin Minecraft Plugin] Couldn't find a localhost ip! Please report this problem!");
            }
        } else {
            enjinlogger.severe("Couldn't find the server configuration file! Please report this problem!");
            throw new Exception("[Enjin Minecraft Plugin] Couldn't find the server configuration file! Please report this problem!");
        }
    }

    public void initConfig() {
        File configFile = new File(getDataFolder(), "config.json");
        EnjinMinecraftPlugin.config = JsonConfig.load(configFile, EnjinConfig.class);

        if (!configFile.exists()) {
            config.save(configFile);
        }

        if (!config.getApiUrl().endsWith("/")) {
            config.setApiUrl(config.getApiUrl().concat("/"));
        }

        String rpcApiUrl = (config.isHttps() ? "https" : "http") + config.getApiUrl() + "v1/";
        EnjinRPC.setHttps(config.isHttps());
        EnjinRPC.setApiUrl(rpcApiUrl);
        debug("RPC API Url: " + rpcApiUrl);
    }

    @Override
    public void saveConfig() {
        config.save(new File(getDataFolder(), "config.json"));
    }

    public void startTask() {
        debug("Starting tasks.");
        BukkitScheduler scheduler = Bukkit.getScheduler();
        synctaskid = scheduler.runTaskTimerAsynchronously(this, task, 1200L, 1200L).getTaskId();
        banlisttask = scheduler.runTaskTimerAsynchronously(this, banlistertask, 40L, 1800L).getTaskId();
        //execute the command executer task every 10 ticks, which should vary between .5 and 1 second on servers.
        commandexecutertask = scheduler.runTaskTimer(this, commexecuter, 20L, 10L).getTaskId();
        commexecuter.loadCommands(Bukkit.getConsoleSender());
        //Only start the vote task if votifier is installed.
        if (votifierinstalled) {
            debug("Starting votifier task.");
            votetaskid = scheduler.runTaskTimerAsynchronously(this, votetask, 80L, 80L).getTaskId();
        }
        if (config.isAutoUpdate() && bukkitversion) {
            updatethread = scheduler.runTaskTimerAsynchronously(this, new Updater(this, 44560, this.getFile(), Updater.UpdateType.DEFAULT, true), 20 * 60 * 5, 20 * 60 * 5).getTaskId();
        }
    }

    public void registerEvents() {
        debug("Registering events.");
        Bukkit.getPluginManager().registerEvents(listener, this);

        if (listenforbans) {
            Bukkit.getPluginManager().registerEvents(new BanListeners(this), this);
        }

        if (config.getBuyCommand() != null) {
            Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
            Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
        }
    }

    public void stopTask() {
        debug("Stopping tasks.");
        if (synctaskid != -1) {
            Bukkit.getScheduler().cancelTask(synctaskid);
        }
        if (commandexecutertask != -1) {
            Bukkit.getScheduler().cancelTask(commandexecutertask);
            commexecuter.saveCommands();
        }
        if (votetaskid != -1) {
            Bukkit.getScheduler().cancelTask(votetaskid);
        }
        if (banlisttask != -1) {
            Bukkit.getScheduler().cancelTask(banlisttask);
        }
        if (headsupdateid != -1) {
            Bukkit.getScheduler().cancelTask(headsupdateid);
        }
        if (updatethread != -1) {
            Bukkit.getScheduler().cancelTask(updatethread);
        }
        //Bukkit.getScheduler().cancelTasks(this);
    }

    public void stopUpdateTask() {
        debug("Stopping update task.");
        if (updatethread != -1) {
            Bukkit.getScheduler().cancelTask(updatethread);
            updatethread = -1;
        }
        //Bukkit.getScheduler().cancelTasks(this);
    }

    public void unregisterEvents() {
        debug("Unregistering events.");
        HandlerList.unregisterAll(listener);
    }

    private void setupVotifierListener() {
        if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            System.out.println("[Enjin Minecraft Plugin] Votifier plugin found, enabling Votifier support.");
            enjinlogger.info("Votifier plugin found, enabling Votifier support.");
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
        if (!Bukkit.getPluginManager().isPluginEnabled("TuxTwoLib")) {
            tuxtwolibinstalled = false;
            enjinlogger.info("Couldn't find the TuxTwoLib plugin. Only able to give items to online players only.");
            getLogger().info("Couldn't find the TuxTwoLib plugin. Only able to give items to online players only.");
        } else {
            tuxtwolibinstalled = true;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            enjinlogger.warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
            getLogger().warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
            return;
        }
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (supportsUUID()) {
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
                enjinlogger.severe("This version of vault doesn't support UUID! Please update to the latest version here: http://dev.bukkit.org/bukkit-plugins/vault/files/");
                enjinlogger.severe("Disabling vault integration until Vault is updated.");
                getLogger().severe("This version of vault doesn't support UUID! Please update to the latest version here: http://dev.bukkit.org/bukkit-plugins/vault/files/");
                getLogger().severe("Disabling vault integration until Vault is updated.");
                return;
            }
        }
        debug("Initializing permissions.");
        initPermissions();
        setupEconomy();
        setupVanishNoPacket();
    }

    private void setupVanishNoPacket() {
        Plugin vanish = Bukkit.getPluginManager().getPlugin("VanishNoPacket");
        if (vanish != null && vanish instanceof VanishPlugin) {
            vanishmanager = ((VanishPlugin) vanish).getManager();
        }
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

                @Override
                public void run() {
                    if (supportsUUID() && !vaultneedsupdating) {
                        try {
                            economy.hasAccount(Bukkit.getOfflinePlayer("Tux2"));
                        } catch (AbstractMethodError e) {
                            econcompatmode = true;
                            enjinlogger.warning("Your economy plugin does not support UUID, using vault legacy compatibility mode.");
                            getLogger().warning("Your economy plugin does not support UUID, using vault legacy compatibility mode.");
                        }
                    }
                }
            }, 20 * 20);
        }
    }

    private void initPermissions() throws Throwable {
        RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (provider == null) {
            enjinlogger.warning("Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
            Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
            return;
        }
        permission = provider.getProvider();
        if (permission == null) {
            enjinlogger.warning("Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
            Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
            return;
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
        //This command is now depreciated in favor of /enjin key
        if (command.getName().equals("enjinkey") || command.getName().equalsIgnoreCase("ek")) {
            if (!sender.hasPermission("enjin.setkey")) {
                sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.setkey\" permission or OP to run that command!");
                return true;
            }
            if (args.length != 1) {
                return false;
            }
            enjinlogger.info("Checking if key is valid");
            Bukkit.getLogger().info("Checking if key is valid");
            //Make sure we don't have several verifier threads going at the same time.
            if (verifier == null || verifier.completed) {
                verifier = new NewKeyVerifier(this, args[0], sender, false);
                Thread verifierthread = new Thread(verifier);
                verifierthread.start();
            } else {
                sender.sendMessage(ChatColor.RED + "Please wait until we verify the key before you try again!");
            }
            return true;
            //We have the main enjin command, and the alias e command.
        }
        if (command.getName().equalsIgnoreCase("enjin") || command.getName().equalsIgnoreCase("e")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("key")) {
                    if (!sender.hasPermission("enjin.setkey")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.setkey\" permission or OP to run that command!");
                        return true;
                    }
                    if (args.length != 2) {
                        return false;
                    }
                    enjinlogger.info("Checking if key is valid");
                    Bukkit.getLogger().info("Checking if key is valid");
                    //Make sure we don't have several verifier threads going at the same time.
                    if (verifier == null || verifier.completed) {
                        verifier = new NewKeyVerifier(this, args[1], sender, false);
                        Thread verifierthread = new Thread(verifier);
                        verifierthread.start();
                    } else {
                        sender.sendMessage(ChatColor.RED + "Please wait until we verify the key before you try again!");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("report")) {
                    if (!sender.hasPermission("enjin.report")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.report\" permission or OP to run that command!");
                        return true;
                    }
                    sender.sendMessage(ChatColor.GREEN + "Please wait as we generate the report");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
                    Date date = new Date();
                    StringBuilder report = new StringBuilder();
                    report.append("Enjin Debug Report generated on " + dateFormat.format(date) + "\n");
                    report.append("Enjin plugin version: " + getDescription().getVersion() + "\n");
                    String permsmanager = "Generic";
                    String permsversion = "Unknown";
                    if (permissionsex != null) {
                        permsmanager = "PermissionsEx";
                        permsversion = permissionsex.getDescription().getVersion();
                    } else if (bpermissions != null) {
                        permsmanager = "bPermissions";
                        permsversion = bpermissions.getDescription().getVersion();
                    } else if (groupmanager != null) {
                        permsmanager = "GroupManager";
                        permsversion = groupmanager.getDescription().getVersion();
                    } else if (permissionsbukkit != null) {
                        permsmanager = "PermissionsBukkit";
                        permsversion = permissionsbukkit.getDescription().getVersion();
                    }
                    report.append("Permissions plugin used: " + permsmanager + " version " + permsversion + "\n");
                    if (permission != null) {
                        report.append("Vault permissions system reported: " + permission.getName() + "\n");
                    }
                    if (economy != null) {
                        report.append("Vault economy system reported: " + economy.getName() + "\n");
                    }
                    if (econcompatmode) {
                        report.append("WARNING! Economy plugin doesn't support UUID, needs update.\n");
                    }
                    if (votifierinstalled) {
                        String votiferversion = Bukkit.getPluginManager().getPlugin("Votifier").getDescription().getVersion();
                        report.append("Votifier version: " + votiferversion + "\n");
                        Plugin vobject = Bukkit.getPluginManager().getPlugin("Votifier");
                        if (vobject != null && vobject instanceof Votifier) {
                            Votifier votifier = (Votifier) vobject;
                            boolean votifiererrored = false;
                            if (votifier.getVoteReceiver() == null) {
                                votifiererrored = true;
                            }
                            FileConfiguration voteconfig = votifier.getConfig();
                            String port = voteconfig.getString("port", "");
                            String host = voteconfig.getString("host", "");
                            report.append("Votifier is enabled properly: " + !votifiererrored + "\n");
                            report.append("Votifier is listening on: " + host + ":" + port + "\n");
                        }
                    }
                    report.append("Bukkit version: " + getServer().getVersion() + "\n");
                    report.append("Java version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + "\n");
                    report.append("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n");

                    if (authkeyinvalid) {
                        report.append("ERROR: Authkey reported by plugin as invalid!\n");
                    }
                    if (unabletocontactenjin) {
                        report.append("WARNING: Plugin has been unable to contact Enjin for the past 5 minutes\n");
                    }
                    if (permissionsnotworking) {
                        report.append("WARNING: Permissions plugin is not configured properly and is disabled. Check the server.log for more details.\n");
                    }

                    report.append("\nPlugins: \n");
                    for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                        report.append(p.getName() + " version " + p.getDescription().getVersion() + "\n");
                    }
                    report.append("\nWorlds: \n");
                    for (World world : getServer().getWorlds()) {
                        report.append(world.getName() + "\n");
                    }
                    ReportMakerThread rmthread = new ReportMakerThread(this, report, sender);
                    Thread dispatchThread = new Thread(rmthread);
                    dispatchThread.start();
                    return true;
                } else if (args[0].equalsIgnoreCase("debug")) {
                    if (!sender.hasPermission("enjin.debug")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.debug\" permission or OP to run that command!");
                        return true;
                    }

                    config.setDebug(!config.isDebug());
                    saveConfig();

                    sender.sendMessage(ChatColor.GREEN + "Debugging has been set to " + config.isDebug());
                    return true;
                } else if (args[0].equalsIgnoreCase("push")) {
                    if (!sender.hasPermission("enjin.push")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.push\" permission or OP to run that command!");
                        return true;
                    }
                    OfflinePlayer[] allplayers = getServer().getOfflinePlayers();
                    if (playerperms.size() > 3000 || playerperms.size() >= allplayers.length) {
                        int minutes = playerperms.size() / 3000;
                        //Make sure to tack on an extra minute for the leftover players.
                        if (playerperms.size() % 3000 > 0) {
                            minutes++;
                        }
                        //Add an extra 10% if it's going to take more than one synch.
                        //Just in case a synch fails.
                        if (playerperms.size() > 3000) {
                            minutes += minutes * 0.1;
                        }
                        sender.sendMessage(ChatColor.RED + "A rank sync is still in progress, please wait until the current sync completes.");
                        sender.sendMessage(ChatColor.RED + "Progress:" + Integer.toString(playerperms.size()) + " more player ranks to transmit, ETA: " + minutes + " minute" + (minutes > 1 ? "s" : "") + ".");
                        return true;
                    }
                    for (OfflinePlayer offlineplayer : allplayers) {
                        if (offlineplayer == null || offlineplayer.getName() == null || offlineplayer.getName().isEmpty()) {
                            continue;
                        }

                        if (supportsUUID() && offlineplayer.getUniqueId() != null) {
                            playerperms.put(offlineplayer.getName(), offlineplayer.getUniqueId().toString());
                        } else {
                            playerperms.put(offlineplayer.getName(), "");
                        }
                    }

                    //Calculate how many minutes approximately it's going to take.
                    int minutes = playerperms.size() / 3000;
                    //Make sure to tack on an extra minute for the leftover players.
                    if (playerperms.size() % 3000 > 0) {
                        minutes++;
                    }
                    //Add an extra 10% if it's going to take more than one synch.
                    //Just in case a synch fails.
                    if (playerperms.size() > 3000) {
                        minutes += minutes * 0.1;
                    }
                    if (minutes == 1) {
                        sender.sendMessage(ChatColor.GREEN + Integer.toString(playerperms.size()) + " players have been queued for synching. This should take approximately " + Integer.toString(minutes) + " minute.");
                    } else {
                        sender.sendMessage(ChatColor.GREEN + Integer.toString(playerperms.size()) + " players have been queued for synching. This should take approximately " + Integer.toString(minutes) + " minutes.");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("savestats")) {
                    if (!sender.hasPermission("enjin.savestats")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.savestats\" permission or OP to run that command!");
                        return true;
                    }
                    new WriteStats(this).write("stats.stats");
                    sender.sendMessage(ChatColor.GREEN + "Stats saved to stats.stats.");
                    return true;
                } else if (args[0].equalsIgnoreCase("playerstats")) {
                    if (!sender.hasPermission("enjin.playerstats")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.playerstats\" permission or OP to run that command!");
                        return true;
                    }
                    if (args.length > 1) {
                        StatsPlayer player = null;
                        if (supportsUUID()) {
                            OfflinePlayer statplayer = Bukkit.getOfflinePlayer(args[1]);
                            String uuid = statplayer.getUniqueId().toString().toLowerCase();
                            if (playerstats.containsKey(uuid)) {
                                player = playerstats.get(uuid);

                            }
                        } else {
                            if (playerstats.containsKey(args[1].toLowerCase())) {
                                player = playerstats.get(args[1].toLowerCase());
                            }
                        }
                        if (player != null) {
                            sender.sendMessage(ChatColor.DARK_GREEN + "Player stats for player: " + ChatColor.GOLD + player.getName());
                            sender.sendMessage(ChatColor.DARK_GREEN + "Deaths: " + ChatColor.GOLD + player.getDeaths());
                            sender.sendMessage(ChatColor.DARK_GREEN + "Kills: " + ChatColor.GOLD + player.getKilled());
                            sender.sendMessage(ChatColor.DARK_GREEN + "Blocks broken: " + ChatColor.GOLD + player.getBrokenblocks());
                            sender.sendMessage(ChatColor.DARK_GREEN + "Blocks placed: " + ChatColor.GOLD + player.getPlacedblocks());
                            sender.sendMessage(ChatColor.DARK_GREEN + "Block types broken: " + ChatColor.GOLD + player.getBrokenblocktypes().toString());
                            sender.sendMessage(ChatColor.DARK_GREEN + "Block types placed: " + ChatColor.GOLD + player.getPlacedblocktypes().toString());
                            sender.sendMessage(ChatColor.DARK_GREEN + "Foot distance traveled: " + ChatColor.GOLD + player.getFootdistance());
                            sender.sendMessage(ChatColor.DARK_GREEN + "Boat distance traveled: " + ChatColor.GOLD + player.getBoatdistance());
                            sender.sendMessage(ChatColor.DARK_GREEN + "Minecart distance traveled: " + ChatColor.GOLD + player.getMinecartdistance());
                            sender.sendMessage(ChatColor.DARK_GREEN + "Pig distance traveled: " + ChatColor.GOLD + player.getPigdistance());
                        } else {
                            sender.sendMessage("I'm sorry, but I couldn't find a player with stats with that name.");
                        }
                    } else {
                        return false;
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("serverstats")) {
                    if (!sender.hasPermission("enjin.serverstats")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.serverstats\" permission or OP to run that command!");
                        return true;
                    }
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
                    Date date = new Date(serverstats.getLastserverstarttime());
                    sender.sendMessage(ChatColor.DARK_GREEN + "Server Stats");
                    sender.sendMessage(ChatColor.DARK_GREEN + "Server Start time: " + ChatColor.GOLD + dateFormat.format(date));
                    sender.sendMessage(ChatColor.DARK_GREEN + "Total number of creeper explosions: " + ChatColor.GOLD + serverstats.getCreeperexplosions());
                    sender.sendMessage(ChatColor.DARK_GREEN + "Total number of kicks: " + ChatColor.GOLD + serverstats.getTotalkicks());
                    sender.sendMessage(ChatColor.DARK_GREEN + "Kicks per player: " + ChatColor.GOLD + serverstats.getPlayerkicks().toString());
                    return true;
                } else if (args[0].equalsIgnoreCase("vote")) {
                    if (!sender.hasPermission("enjin.test.vote")) {
                        return false;
                    }

                    if (args.length != 3) {
                        sender.sendMessage("Usage: /enjin vote <username> <listname>");
                        return true;
                    }

                    String username = args[1];
                    String listname = args[2];

                    if (supportsUUID()) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(username);
                        if (player != null) {
                            username = username.concat("|" + player.getUniqueId().toString());
                        }
                    }

                    if (playervotes.containsKey(username)) {
                        playervotes.get(username).add(listname.replaceAll("[^0-9A-Za-z.\\-]", ""));
                    } else {
                        playervotes.put(username, new ArrayList<String>());
                        playervotes.get(username).add(listname.replaceAll("[^0-9A-Za-z.\\-]", ""));
                    }

                    sender.sendMessage(ChatColor.GREEN + "You just added a vote for player " + username + " on list " + listname);
                    return true;
                } else if (args[0].equalsIgnoreCase("inform")) {
                    if (!sender.hasPermission("enjin.inform")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.inform\" permission or OP to run that command!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "To send a message do: /enjin inform playername message");
                        return true;
                    }
                    Player player = getServer().getPlayerExact(args[1]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "That player isn't on the server at the moment.");
                        return true;
                    }
                    StringBuilder thestring = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        if (i > 2) {
                            thestring.append(" ");
                        }
                        thestring.append(args[i]);
                    }
                    player.sendMessage(EnjinConsole.translateColorCodes(thestring.toString()));
                    return true;
                } else if (args[0].equalsIgnoreCase("broadcast")) {
                    if (!sender.hasPermission("enjin.broadcast")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.broadcast\" permission or OP to run that command!");
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "To broadcast a message do: /enjin broadcast message");
                    }
                    StringBuilder thestring = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        if (i > 1) {
                            thestring.append(" ");
                        }
                        thestring.append(args[i]);
                    }
                    getServer().broadcastMessage(EnjinConsole.translateColorCodes(thestring.toString()));
                    return true;
                } else if (args[0].equalsIgnoreCase("lag")) {
                    if (!sender.hasPermission("enjin.lag")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.lag\" permission or OP to run that command!");
                        return true;
                    }
                    sender.sendMessage(ChatColor.GOLD + "Average TPS: " + ChatColor.GREEN + tpstask.getTPSAverage());
                    sender.sendMessage(ChatColor.GOLD + "Last TPS measurement: " + ChatColor.GREEN + tpstask.getLastTPSMeasurement());
                    Runtime runtime = Runtime.getRuntime();
                    long memused = (runtime.maxMemory() - runtime.freeMemory()) / (1024 * 1024);
                    long maxmemory = runtime.maxMemory() / (1024 * 1024);
                    sender.sendMessage(ChatColor.GOLD + "Memory Used: " + ChatColor.GREEN + memused + "MB/" + maxmemory + "MB");
                    return true;
                } else if (args[0].equalsIgnoreCase("customstat")) {
                    if (!sender.hasPermission("enjin.customstat")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.customstat\" permission or OP to run that command!");
                        return true;
                    }
                    if (args.length > 5) {
                        String playername = args[1].trim();
                        String pluginname = args[2].trim();
                        String customname = args[3].trim();
                        String customvalue = args[4].trim();
                        String cumulative = args[5].trim();
                        boolean existing = cumulative.equalsIgnoreCase("true");
                        OfflinePlayer oplayer = Bukkit.getOfflinePlayer(playername);
                        StatsPlayer splayer = getPlayerStats(oplayer);
                        if (customvalue.indexOf(".") > -1) {
                            try {
                                double dvalue = Double.parseDouble(customvalue);
                                splayer.addCustomStat(pluginname, customname, dvalue, existing);
                                sender.sendMessage(ChatColor.GREEN + "Successfully set the custom value!");
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.RED + "I'm sorry, custom values can only be numerical.");
                            }
                        } else {
                            try {
                                int ivalue = Integer.parseInt(customvalue);
                                splayer.addCustomStat(pluginname, customname, ivalue, existing);
                                sender.sendMessage(ChatColor.GREEN + "Successfully set the custom value!");
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.RED + "I'm sorry, custom values can only be numerical.");
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "Usage: /enjin customstat <player> <plugin> <statname> <value> <cumulative>");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("head") || args[0].equalsIgnoreCase("heads")) {
                    if (!sender.hasPermission("enjin.sign.set")) {
                        sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.sign.set\" permission or OP to run that command!");
                        return true;
                    }
                    /*
					 * Display detailed Enjin help for heads in console
					 */
                    sender.sendMessage(EnjinConsole.header());

                    sender.sendMessage(ChatColor.AQUA + "To set a sign with a head, just place the head, then place the sign either above or below it.");
                    sender.sendMessage(ChatColor.AQUA + "To create a sign of a specific type just put the code on the first line. # denotes the number.");
                    sender.sendMessage(ChatColor.AQUA + " Example: [donation2] would show the second most recent donation.");
                    sender.sendMessage(ChatColor.AQUA + "If there are sub-types, those go on the second line of the sign.");
                    sender.sendMessage(ChatColor.GOLD + "[donation#] " + ChatColor.RESET + " - Most recent donation.");
                    sender.sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " Place the item id on the second line to only get donations for that package.");
                    sender.sendMessage(ChatColor.GOLD + "[topvoter#] " + ChatColor.RESET + " - Top voter of the month.");
                    sender.sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " day, week, month. Changes it to the top voter of the day/week/month.");
                    sender.sendMessage(ChatColor.GOLD + "[voter#] " + ChatColor.RESET + " - Most recent voter.");
                    sender.sendMessage(ChatColor.GOLD + "[topplayer#] " + ChatColor.RESET + " - Top player (gets data from module on website).");
                    sender.sendMessage(ChatColor.GOLD + "[topposter#] " + ChatColor.RESET + " - Top poster on the forum.");
                    sender.sendMessage(ChatColor.GOLD + "[toplikes#] " + ChatColor.RESET + " - Top forum likes.");
                    sender.sendMessage(ChatColor.GOLD + "[newmember#] " + ChatColor.RESET + " - Latest player to sign up on the website.");
                    sender.sendMessage(ChatColor.GOLD + "[toppoints#] " + ChatColor.RESET + " - Which player has the most unspent points.");
                    sender.sendMessage(ChatColor.GOLD + "[pointsspent#] " + ChatColor.RESET + " - Player which has spent the most points overall.");
                    sender.sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " day, week, month. Changes the range to day/week/month.");
                    sender.sendMessage(ChatColor.GOLD + "[moneyspent#] " + ChatColor.RESET + " - Player which has spent the most money on the server overall.");
                    sender.sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " day, week, month. Changes the range to day/week/month.");
                    return true;
                } else if (args[0].equalsIgnoreCase("give")) {
                    //If this is a player let's see if they have permission.
                    if (!sender.hasPermission("enjin.give")) {
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Syntax: /enjin give [playername or UUID] MaterialName");
                        return true;
                    }

                    Player giveplayer;
                    String suuid = args[1].trim();
                    UUID uuid = null;
                    if (suuid.length() > 20) {
                        if (suuid.length() == 32) {
                            suuid = suuid.substring(0, 8) + "-" + suuid.substring(8, 12) + "-" + suuid.substring(12, 16) + "-" + suuid.substring(16, 20) + "-" + suuid.substring(20, 32);
                        } else if (suuid.length() != 36) {
                            sender.sendMessage(ChatColor.RED + "Invalid UUID");
                            return true;
                        }
                        try {
                            uuid = UUID.fromString(suuid);
                            giveplayer = getServer().getPlayer(uuid);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(ChatColor.RED + "Invalid UUID");
                            return true;
                        }
                    } else {
                        //Must be a player name
                        giveplayer = getServer().getPlayer(suuid);
                    }
                    boolean playeronline = true;
                    //see if the player is online
                    if (giveplayer == null || !giveplayer.isOnline()) {
                        //Let's do the TuxTwoLib check to see if we can give items to players not online at the moment.
                        if (!tuxtwolibinstalled) {
                            sender.sendMessage(ChatColor.RED + "This player is not online. In order to give items to players not online at the moment please install TuxTwoLib");
                            return true;
                        }
                        OfflinePlayer oplayer;
                        if (uuid != null) {
                            oplayer = getServer().getOfflinePlayer(uuid);
                        } else {
                            oplayer = getServer().getOfflinePlayer(args[1]);
                        }
                        Player target = TuxTwoPlayer.getOfflinePlayer(oplayer);
                        if (target != null) {
                            target.loadData();
                            playeronline = false;
                            giveplayer = target;
                        } else {
                            sender.sendMessage(ChatColor.DARK_RED + "[Enjin] Player not found. Item not given.");
                            return true;
                        }
                    }
                    try {
                        int extradatastart = 3;
                        Pattern digits = Pattern.compile("\\d+");
                        if (args[2].contains(":")) {
                            String[] split = args[2].split(":");
                            ItemStack is;
                            Pattern pattern = Pattern.compile("\\d+:\\d+");
                            Matcher match = pattern.matcher(args[2]);
                            if (match.find()) {
                                try {
                                    int itemid = Integer.parseInt(split[0]);
                                    int damage = Integer.parseInt(split[1]);
                                    int quantity = 1;
                                    if (args.length > 3 && digits.matcher(args[3]).find()) {
                                        quantity = Integer.parseInt(args[3]);
                                        extradatastart = 4;
                                    }
                                    is = new ItemStack(itemid, quantity, (short) damage);
                                    sender.sendMessage(ChatColor.RED + "Using IDs is depreciated. Please switch to using material name: http://jd.bukkit.org/beta/apidocs/org/bukkit/Material.html");
                                } catch (NumberFormatException e) {
                                    sender.sendMessage(ChatColor.DARK_RED + "Ooops, something went wrong. Did you specify the item correctly?");
                                    return false;
                                }
                            } else {
                                try {
                                    Material itemid = Material.getMaterial(split[0].trim().toUpperCase());
                                    if (itemid == null) {
                                        sender.sendMessage(ChatColor.DARK_RED + "Ooops, I couldn't find a material with that name. Did you spell it correctly?");
                                        return false;
                                    }
                                    int damage = Integer.parseInt(split[1]);
                                    int quantity = 1;
                                    if (args.length > 3 && digits.matcher(args[3]).find()) {
                                        quantity = Integer.parseInt(args[3]);
                                        extradatastart = 4;
                                    }
                                    is = new ItemStack(itemid, quantity, (short) damage);
                                } catch (NumberFormatException ex) {
                                    sender.sendMessage(ChatColor.DARK_RED + "Ooops, something went wrong. Did you specify the item correctly?");
                                    return false;
                                }
                            }
                            if (args.length > extradatastart) {
                                addCustomData(is, args, giveplayer, extradatastart);
                            }
                            giveplayer.getInventory().addItem(is);
                            if (!playeronline) {
                                giveplayer.saveData();
                            }

                            String itemname = is.getType().toString().toLowerCase();
                            sender.sendMessage(ChatColor.DARK_AQUA + "You just gave " + args[1] + " " + is.getAmount() + " " + itemname.replace("_", " ") + "!");
                            return true;
                        } else {
                            ItemStack is;
                            try {
                                int itemid = Integer.parseInt(args[2]);
                                int quantity = 1;
                                if (args.length > 3 && digits.matcher(args[3]).find()) {
                                    quantity = Integer.parseInt(args[3]);
                                    extradatastart = 4;
                                }
                                is = new ItemStack(itemid, quantity);
                                sender.sendMessage(ChatColor.RED + "Using IDs is depreciated. Please switch to using material name: http://jd.bukkit.org/beta/apidocs/org/bukkit/Material.html");
                            } catch (NumberFormatException e) {
                                Material material = Material.getMaterial(args[2].trim().toUpperCase());
                                if (material == null) {
                                    sender.sendMessage(ChatColor.DARK_RED + "Ooops, I couldn't find a material with that name. Did you spell it correctly?");
                                    return false;
                                }
                                int quantity = 1;
                                if (args.length > 3 && digits.matcher(args[3]).find()) {
                                    quantity = Integer.parseInt(args[3]);
                                    extradatastart = 4;
                                }
                                is = new ItemStack(material, quantity);
                            }
                            if (args.length > extradatastart) {
                                addCustomData(is, args, giveplayer, extradatastart);
                            }
                            giveplayer.getInventory().addItem(is);
                            if (!playeronline) {
                                giveplayer.saveData();
                            }

                            String itemname = is.getType().toString().toLowerCase();
                            sender.sendMessage(ChatColor.DARK_AQUA + "You just gave " + args[1] + " " + is.getAmount() + " " + itemname.replace("_", " ") + "!");
                            return true;
                        }
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.DARK_RED + "Ooops, something went wrong. Did you specify the item correctly?");
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("support")) {
                    if (!sender.hasPermission("enjin.support")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to have the \"enjin.support\" permission or OP to run that command!"));
                        return true;
                    }

                    if (getAuthKey() == null) {
                        sender.sendMessage("Cannot use this command without setting your key.");
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Only players can submit tickets.");
                        return true;
                    }

                    Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                        @Override
                        public void run() {
                            pollModules();

                            if (sender == null) {
                                return;
                            } else if (sender instanceof Player) {
                                Player player = (Player) sender;
                                if (!player.isOnline()) {
                                    return;
                                }
                            }

                            if (modules.size() == 0) {
                                sender.sendMessage("Support tickets are not available on this server.");
                                return;
                            }

                            if (args.length > 1) {
                                int moduleId;

                                try {
                                    moduleId = Integer.parseInt(args[1]);
                                } catch (NumberFormatException e) {
                                    sender.sendMessage("You must enter the numeric id of the module!");
                                    return;
                                }

                                debug("Checking if module with id \"" + moduleId + "\" exists.");
                                final Module module = modules.get(moduleId);
                                if (module != null) {
                                    new TicketCreationSession((Player) sender, moduleId, module);
                                } else {
                                    sender.sendMessage("No module with id \"" + moduleId + "\" exists.");
                                    debug("Existing modules:");
                                    for (Integer id : modules.keySet()) {
                                        debug(String.valueOf(id));
                                    }
                                }
                            } else {
                                if (modules.size() == 1) {
                                    final Entry<Integer, Module> entry = modules.entrySet().iterator().next();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.this, new Runnable() {
                                        @Override
                                        public void run() {
                                            new TicketCreationSession((Player) sender, entry.getKey(), entry.getValue());
                                        }
                                    });
                                } else {
                                    debug(String.valueOf(modules.size()));
                                    for (Entry<Integer, Module> entry : modules.entrySet()) {
                                        int id = entry.getKey();
                                        Module module = entry.getValue();
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (module.getHelp() != null && !module.getHelp().isEmpty()) ? module.getHelp() : "Type /e support " + id + " to create a support ticket for " + module.getName().replaceAll("\\s+", " ")));
                                    }
                                }
                            }
                        }
                    });

                    return true;
                } else if (args[0].equalsIgnoreCase("ticket")) {
                    if (!sender.hasPermission("enjin.ticket")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to have the \"enjin.ticket\" permission or OP to run that command!"));
                        return true;
                    }

                    if (getAuthKey() == null) {
                        sender.sendMessage("Cannot use this command without setting your key.");
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Only players can view their own tickets.");
                        return true;
                    }

                    if (args.length == 1) {
                        final Player player = (Player) sender;
                        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                            @Override
                            public void run() {
                                TicketService service = EnjinServices.getService(TicketService.class);
                                RPCData<List<Ticket>> data = service.getPlayerTickets(getAuthKey(), -1, player.getName());

                                if (data != null) {
                                    if (data.getError() != null) {
                                        player.sendMessage(data.getError().getMessage());
                                    } else {
                                        List<Ticket> tickets = data.getResult();
                                        if (tickets.size() > 0) {
                                            player.spigot().sendMessage(TicketViewBuilder.buildTicketList(tickets));
                                        } else {
                                            player.sendMessage("You do not have any tickets at this time!");
                                        }
                                    }
                                } else {
                                    player.sendMessage("Could not fetch your tickets.");
                                }
                            }
                        });
                    } else {
                        final Player player = (Player) sender;
                        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                            @Override
                            public void run() {
                                TicketService service = EnjinServices.getService(TicketService.class);
                                RPCData<List<Reply>> data = service.getReplies(getAuthKey(), -1, args[1], player.getName());

                                if (data != null) {
                                    if (data.getError() != null) {
                                        player.sendMessage(data.getError().getMessage());
                                    } else {
                                        List<Reply> replies = data.getResult();
                                        if (replies.size() > 0) {
                                            player.spigot().sendMessage(TicketViewBuilder.buildTicket(args[1], replies, player.hasPermission("enjin.ticket.private")));
                                        } else {
                                            player.sendMessage("You entered an invalid ticket code!");
                                        }
                                    }
                                } else {
                                    player.sendMessage("Could not fetch ticket replies.");
                                }
                            }
                        });
                    }

                    return true;
                } else if (args[0].equalsIgnoreCase("openticket")) {
                    if (!sender.hasPermission("enjin.ticket.open")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to have the \"enjin.ticket.open\" permission or OP to run that command!"));
                        return true;
                    }

                    if (getAuthKey() == null) {
                        sender.sendMessage("Cannot use this command without setting your key.");
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Only players can view their own tickets.");
                        return true;
                    }

                    if (args.length == 1) {
                        final Player player = (Player) sender;
                        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                            @Override
                            public void run() {
                                TicketService service = EnjinServices.getService(TicketService.class);
                                RPCData<List<Ticket>> data = service.getTickets(getAuthKey(), -1, TicketStatus.open);

                                if (data != null) {
                                    if (data.getError() != null) {
                                        player.sendMessage(data.getError().getMessage());
                                    } else {
                                        List<Ticket> tickets = data.getResult();
                                        if (tickets.size() > 0) {
                                            player.spigot().sendMessage(TicketViewBuilder.buildTicketList(tickets));
                                        } else {
                                            player.sendMessage("There are no open tickets at this time.");
                                        }
                                    }
                                } else {
                                    player.sendMessage("Could not fetch open tickets.");
                                }
                            }
                        });
                    } else {
                        final Player player = (Player) sender;
                        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                            @Override
                            public void run() {
                                TicketService service = EnjinServices.getService(TicketService.class);
                                RPCData<List<Reply>> data = service.getReplies(getAuthKey(), -1, args[1], player.getName());

                                if (data != null) {
                                    if (data.getError() != null) {
                                        player.sendMessage(data.getError().getMessage());
                                    } else {
                                        List<Reply> replies = data.getResult();
                                        if (replies.size() > 0) {
                                            player.spigot().sendMessage(TicketViewBuilder.buildTicket(args[1], replies, player.hasPermission("enjin.ticket.private")));
                                        } else {
                                            player.sendMessage("You entered an invalid ticket code!");
                                        }
                                    }
                                } else {
                                    player.sendMessage("Could not fetch ticket replies.");
                                }
                            }
                        });
                    }

                    return true;
                } else if (args[0].equalsIgnoreCase("reply")) {
                    if (!sender.hasPermission("enjin.ticket.reply")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to have the \"enjin.ticket.reply\" permission or OP to run that command!"));
                        return true;
                    }

                    if (getAuthKey() == null) {
                        sender.sendMessage("Cannot use this command without setting your key.");
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Only players can reply to tickets.");
                        return true;
                    }

                    if (args.length < 4) {
                        sender.sendMessage("Usage: /e reply <#> <message>");
                        return true;
                    } else {
                        final int preset;
                        try {
                            preset = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Usage: /e reply <preset_id> <ticket_code> <open,pending,closed>");
                            return true;
                        }
                        final String ticket = args[2];
                        String message = "";
                        for (String arg : Arrays.copyOfRange(args, 3, args.length)) {
                            message = message.concat(arg + " ");
                        }
                        message.trim();
                        final String finalMessage = message;

                        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                            @Override
                            public void run() {
                                RPCData<RPCSuccess> result = EnjinServices.getService(TicketService.class).sendReply(getAuthKey(), preset, ticket, finalMessage, "public", TicketStatus.open, ((Player) sender).getName());
                                if (result != null) {
                                    if (result.getError() == null) {
                                        sender.sendMessage("You replied to the ticket successfully.");
                                    } else {
                                        sender.sendMessage(result.getError().getMessage());
                                    }
                                } else {
                                    sender.sendMessage("Unable to submit your reply.");
                                }
                            }
                        });
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("ticketstatus")) {
                    if (!sender.hasPermission("enjin.ticket.status")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to have the \"enjin.ticket.status\" permission or OP to run that command!"));
                        return true;
                    }

                    if (getAuthKey() == null) {
                        sender.sendMessage("Cannot use this command without setting your key.");
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Only players can change ticket status.");
                        return true;
                    }

                    if (args.length != 4) {
                        sender.sendMessage("Usage: /e reply <preset_id> <ticket_code> <open,pending,closed>");
                        return true;
                    } else {
                        final int preset;
                        try {
                            preset = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Usage: /e reply <preset_id> <ticket_code> <open,pending,closed>");
                            return true;
                        }
                        final String ticket = args[2];
                        final TicketStatus status = TicketStatus.valueOf(args[3].toLowerCase());

                        if (status == null) {
                            sender.sendMessage("Usage: /e reply <preset_id> <ticket_code> <open,pending,closed>");
                            return true;
                        }

                        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                            @Override
                            public void run() {
                                RPCData<Boolean> result = EnjinServices.getService(TicketService.class).setStatus(getAuthKey(), preset, ticket, status);
                                if (result != null) {
                                    if (result.getError() == null) {
                                        if (result.getResult()) {
                                            sender.sendMessage("The tickets status was successfully changed to " + status.name());
                                        } else {
                                            sender.sendMessage("The tickets status was unable to be changed to " + status.name());
                                        }
                                    } else {
                                        sender.sendMessage(result.getError().getMessage());
                                    }
                                } else {
                                    sender.sendMessage("The tickets status was unable to be changed to " + status.name());
                                }
                            }
                        });
                        return true;
                    }
                }
            } else {
				/*
				 * Display detailed Enjin help in console
				 */
                sender.sendMessage(EnjinConsole.header());

                if (sender.hasPermission("enjin.setkey"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin key <KEY>: "
                            + ChatColor.RESET + "Enter the secret key from your " + ChatColor.GRAY + "Admin - Games - Minecraft - Enjin Plugin " + ChatColor.RESET + "page.");
                if (sender.hasPermission("enjin.broadcast"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin broadcast <MESSAGE>: "
                            + ChatColor.RESET + "Broadcast a message to all players.");
                if (sender.hasPermission("enjin.push"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin push: "
                            + ChatColor.RESET + "Sync your website tags with the current ranks.");
                if (sender.hasPermission("enjin.lag"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin lag: "
                            + ChatColor.RESET + "Display TPS average and memory usage.");
                if (sender.hasPermission("enjin.debug"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin debug: "
                            + ChatColor.RESET + "Enable debug mode and display extra information in console.");
                if (sender.hasPermission("enjin.report"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin report: "
                            + ChatColor.RESET + "Generate a report file that you can send to Enjin Support for troubleshooting.");
                if (sender.hasPermission("enjin.sign.set"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin heads: "
                            + ChatColor.RESET + "Shows in game help for the heads and sign stats part of the plugin.");
                if (sender.hasPermission("enjin.tags.view"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin tags <player>: "
                            + ChatColor.RESET + "Shows the tags on the website for the player.");

                // Points commands
                if (sender.hasPermission("enjin.points.getself"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin points: "
                            + ChatColor.RESET + "Shows your current website points.");
                if (sender.hasPermission("enjin.points.getothers"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin points <NAME>: "
                            + ChatColor.RESET + "Shows another player's current website points.");
                if (sender.hasPermission("enjin.points.add"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin addpoints <NAME> <AMOUNT>: "
                            + ChatColor.RESET + "Add points to a player.");
                if (sender.hasPermission("enjin.points.remove"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin removepoints <NAME> <AMOUNT>: "
                            + ChatColor.RESET + "Remove points from a player.");
                if (sender.hasPermission("enjin.points.set"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin setpoints <NAME> <AMOUNT>: "
                            + ChatColor.RESET + "Set a player's total points.");
                if (sender.hasPermission("enjin.support"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin support: "
                            + ChatColor.RESET + "Starts ticket session or informs player of available modules.");
                if (sender.hasPermission("enjin.ticket.self"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin ticket: "
                            + ChatColor.RESET + "Sends player a list of their tickets.");
                if (sender.hasPermission("enjin.ticket.open"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin openticket: "
                            + ChatColor.RESET + "Sends player a list of open tickets.");
                if (sender.hasPermission("enjin.ticket.reply"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin reply <module #> <ticket id> <message>: "
                            + ChatColor.RESET + "Sends a reply to a ticket.");
                if (sender.hasPermission("enjin.ticket.status"))
                    sender.sendMessage(ChatColor.GOLD + "/enjin ticketstatus <module #> <ticket id> <open|pending|closed>: "
                            + ChatColor.RESET + "Sets the status of a ticket.");

                // Shop buy commands
                sender.sendMessage(ChatColor.GOLD + "/buy: "
                        + ChatColor.RESET + "Display items available for purchase.");
                sender.sendMessage(ChatColor.GOLD + "/buy page <#>: "
                        + ChatColor.RESET + "View the next page of results.");
                sender.sendMessage(ChatColor.GOLD + "/buy <ID>: "
                        + ChatColor.RESET + "Purchase the specified item ID in the server shop.");
                return true;
            }
        }
        return false;
    }

    /**
     * @param urls
     * @param queryValues
     * @return 0 = Invalid key, 1 = OK, 2 = Exception encountered.
     * @throws MalformedURLException
     */
    public static int sendAPIQuery(String urls, String... queryValues) throws MalformedURLException {
        URL url = new URL((usingSSL ? "https" : "http") + config.getApiUrl() + urls);
        StringBuilder query = new StringBuilder();
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setReadTimeout(3000);
            con.setConnectTimeout(3000);
            con.setDoOutput(true);
            con.setDoInput(true);
            for (String val : queryValues) {
                query.append('&');
                query.append(val);
            }
            if (queryValues.length > 0) {
                query.deleteCharAt(0); //remove first &
            }

            con.setRequestProperty("Content-length", String.valueOf(query.length()));
            con.getOutputStream().write(query.toString().getBytes());
            String read = PacketUtilities.readString(new BufferedInputStream(con.getInputStream()));
            instance.debug("Reply from enjin on Enjin Key for url " + url.toString() + " and query " + query.toString() + ": " + read);
            if (read.charAt(0) == '1') {
                return 1;
            }
            return 0;
        } catch (SSLHandshakeException e) {
            enjinlogger.warning("SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
            Bukkit.getLogger().warning("[Enjin Minecraft Plugin] SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
            usingSSL = false;
            return sendAPIQuery(urls, queryValues);
        } catch (SocketTimeoutException e) {
            enjinlogger.warning("Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
            Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
            return 2;
        } catch (Throwable t) {
            t.printStackTrace();
            enjinlogger.warning("Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
            Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
            return 2;
        }
    }

    public static synchronized void setAuthKey(String key) {
        config.setAuthKey(key);
    }

    public static synchronized String getAuthKey() {
        return config.getAuthKey();
    }

    private void setupPermissions() {
        Plugin pex = this.getServer().getPluginManager().getPlugin("PermissionsEx");
        if (pex != null) {
            permissionsex = (PermissionsEx) pex;
            debug("PermissionsEx found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new PexChangeListener(this), this);
            return;
        }

        Plugin bperm = this.getServer().getPluginManager().getPlugin("bPermissions");
        if (bperm != null) {
            bpermissions = (Permissions) bperm;
            debug("bPermissions found, hooking custom events.");
            supportsglobalgroups = false;
            Bukkit.getPluginManager().registerEvents(new bPermsChangeListener(this), this);
            return;
        }

        Plugin zPermissions = this.getServer().getPluginManager().getPlugin("zPermissions");
        if (zPermissions != null) {
            this.zPermissions = (ZPermissionsPlugin) zPermissions;
            debug("zPermissions found, hooking custom events.");
            supportsglobalgroups = true;
            Bukkit.getPluginManager().registerEvents(new ZPermissionsListener(this), this);
            return;
        }

        Plugin groupmanager = this.getServer().getPluginManager().getPlugin("GroupManager");
        if (groupmanager != null) {
            this.groupmanager = (GroupManager) groupmanager;
            debug("GroupManager found, hooking custom events.");
            if (supportsUUID()) {
                boolean gmupdated = false;
                Pattern devbuild = Pattern.compile("\\(Dev(\\d+)\\.(\\d+)\\.(\\d+)\\)");
                Matcher devmatch = devbuild.matcher(groupmanager.getDescription().getVersion());
                if (devmatch.find()) {
                    int majorver = Integer.parseInt(devmatch.group(1));
                    int minorver = Integer.parseInt(devmatch.group(2));
                    int buildver = Integer.parseInt(devmatch.group(3));
                    if (majorver > 2) {
                        gmupdated = true;
                    } else if (majorver == 2) {
                        if (minorver == 14 && buildver > 49) {
                            gmupdated = true;
                        } else if (buildver > 14) {
                            gmupdated = true;
                        }
                    }
                    debug("GroupManager dev version: " + majorver + "." + minorver + "." + buildver);
                } else {
                    String[] version = groupmanager.getDescription().getVersion().split("\\.");
                    int majorver = Integer.parseInt(version[0]);
                    Pattern numberpattern = Pattern.compile("\\d+");
                    int minorver = 0;
                    if (version.length > 1) {
                        Matcher match = numberpattern.matcher(version[1]);
                        if (match.find()) {
                            minorver = Integer.parseInt(match.group());
                        }
                    }
                    int revver = 0;
                    if (version.length > 2) {
                        Matcher match = numberpattern.matcher(version[2]);
                        try {
                            if (match.find()) {
                                revver = Integer.parseInt(match.group());
                            }
                        } catch (Exception e) {

                        }
                    }
                    debug("GroupManager version: " + majorver + "." + minorver + "." + revver);
                    if (majorver > 2) {
                        gmupdated = true;
                    } else if (majorver == 2 && minorver > 1) {
                        gmupdated = true;
                    } else if (majorver == 2 && minorver == 1 && revver > 10) {
                        gmupdated = true;
                    }
                }

                if (!gmupdated) {
                    gmneedsupdating = true;
                    enjinlogger.severe("This version of GroupManager doesn't support UUID! Please update to the latest version here: http://tiny.cc/EssentialsGMZip");
                    enjinlogger.severe("Disabling GroupManager integration until GroupManager is updated.");
                    getLogger().severe("This version of GroupManager doesn't support UUID! Please update to the latest version here: http://tiny.cc/EssentialsGMZip");
                    getLogger().severe("Disabling GroupManager integration until GroupManager is updated.");
                    return;
                }
            }

            supportsglobalgroups = false;
            Bukkit.getPluginManager().registerEvents(new GroupManagerListener(this), this);
            return;
        }
        Plugin bukkitperms = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
        if (bukkitperms != null) {
            this.permissionsbukkit = (PermissionsPlugin) bukkitperms;
            debug("PermissionsBukkit found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new PermissionsBukkitChangeListener(this), this);
            return;
        }
        debug("No suitable permissions plugin found, falling back to synching on player disconnect.");
        debug("You might want to switch to PermissionsEx, bPermissions, or Essentials GroupManager.");

    }

    public int getTotalXP(int level, float xp) {
        int atlevel = 0;
        int totalxp = 0;
        int xpneededforlevel = 0;
        if (xpversion == 1) {
            xpneededforlevel = 17;
            while (atlevel < level) {
                atlevel++;
                totalxp += xpneededforlevel;
                if (atlevel >= 16) {
                    xpneededforlevel += 3;
                }
            }
            //We only have 2 versions at the moment
        } else {
            xpneededforlevel = 7;
            boolean odd = true;
            while (atlevel < level) {
                atlevel++;
                totalxp += xpneededforlevel;
                if (odd) {
                    xpneededforlevel += 3;
                    odd = false;
                } else {
                    xpneededforlevel += 4;
                    odd = true;
                }
            }
        }
        totalxp = (int) (totalxp + (xp * xpneededforlevel));
        return totalxp;
    }

    /**
     * Use this to get any player's stats, whether they are online or offline.
     *
     * @param player the player you want to get stats for.
     * @return the StatsPlayer object. This function always returns a StatsPlayer.
     */
    public StatsPlayer getPlayerStats(OfflinePlayer player) {
        if (supportsUUID()) {
            String uuid = player.getUniqueId().toString().toLowerCase();
            StatsPlayer stats = playerstats.get(uuid);
            if (stats == null) {
                stats = new StatsPlayer(player);
                playerstats.put(uuid, stats);
            }
            return stats;
        } else {
            StatsPlayer stats = playerstats.get(player.getName().toLowerCase());
            if (stats == null) {
                stats = new StatsPlayer(player);
                playerstats.put(player.getName().toLowerCase(), stats);
            }
            return stats;
        }
    }

    /**
     * Please don't use this for your plugins as this is only for internal use.
     *
     * @param player
     */
    public void setPlayerStats(StatsPlayer player) {
        if (supportsUUID()) {
            playerstats.put(player.getUUID().toLowerCase(), player);
        } else {
            playerstats.put(player.getName(), player);
        }
    }

    public void noEnjinConnectionEvent() {
        if (!unabletocontactenjin) {
            unabletocontactenjin = true;
            Player[] players = getPlayerGetter().getOnlinePlayers();
            for (Player player : players) {
                if (player.hasPermission("enjin.notify.connectionstatus")) {
                    player.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Unable to connect to enjin, please check your settings.");
                    player.sendMessage(ChatColor.DARK_RED + "If this problem persists please send enjin the results of the /enjin report");
                }
            }
        }
    }

    public boolean testHTTPSconnection() {
        try {
            URL url = new URL("https://api.enjin.com/ok.html");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
            if (inputLine != null && inputLine.startsWith("OK")) {
                return true;
            }
            return false;
        } catch (SSLHandshakeException e) {
            if (config.isDebug()) {
                e.printStackTrace();
            }
            return false;
        } catch (SocketTimeoutException e) {
            if (config.isDebug()) {
                e.printStackTrace();
            }
            return false;
        } catch (Throwable t) {
            if (config.isDebug()) {
                t.printStackTrace();
            }
            return false;
        }
    }

    public boolean testWebConnection() {
        try {
            URL url = new URL("http://google.com");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
            if (inputLine != null) {
                return true;
            }
            return false;
        } catch (SocketTimeoutException e) {
            if (config.isDebug()) {
                e.printStackTrace();
            }
            return false;
        } catch (Throwable t) {
            if (config.isDebug()) {
                t.printStackTrace();
            }
            return false;
        }
    }

    public boolean testHTTPconnection() {
        try {
            URL url = new URL("http://api.enjin.com/ok.html");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
            if (inputLine != null && inputLine.startsWith("OK")) {
                return true;
            }
            return false;
        } catch (SocketTimeoutException e) {
            if (config.isDebug()) {
                e.printStackTrace();
            }
            return false;
        } catch (Throwable t) {
            if (config.isDebug()) {
                t.printStackTrace();
            }
            return false;
        }
    }

    public static boolean isMineshafterPresent() {
        try {
            Class.forName("mineshafter.MineServer");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Runnable getTask() {
        return task;
    }

    public void addCommandID(CommandWrapper command) {
        if (command.getId().equals("")) {
            return;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(command.getCommand().getBytes("UTF-8"));

            BigInteger bigInt = new BigInteger(1, digest);
            String hashtext = bigInt.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            command.setHash(hashtext);
            commandids.put(command.getId(), command);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public Map<String, CommandWrapper> getCommandIDs() {
        return commandids;
    }

    public void removeCommandID(String id) {
        commandids.remove(id);
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

    public void loadCommandIDs() {
        commandids.clear();
        File dataFolder = getDataFolder();
        File headsfile = new File(dataFolder, "executedcommands.yml");
        File newheadsfile = new File(dataFolder, "newexecutedcommands.yml");
        if (headsfile.exists()) {
            YamlConfiguration commandconfig = new YamlConfiguration();
            try {
                commandconfig.load(headsfile);
                Set<String> keys = commandconfig.getValues(false).keySet();
                for (String key : keys) {
                    String hash = commandconfig.getString(key);
                    CommandWrapper comm = new CommandWrapper(Bukkit.getConsoleSender(), "", key);
                    comm.setHash(hash);
                    commandids.put(key, comm);
                }
                headsfile.delete();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (InvalidConfigurationException e) {
            }
        } else if (newheadsfile.exists()) {
            YamlConfiguration commandconfig = new YamlConfiguration();
            try {
                commandconfig.load(newheadsfile);
                Set<String> keys = commandconfig.getValues(false).keySet();
                for (String key : keys) {
                    String hash = commandconfig.getString(key + ".hash");
                    String command = commandconfig.getString(key + ".command");
                    String result = commandconfig.getString(key + ".result");
                    CommandWrapper comm = new CommandWrapper(Bukkit.getConsoleSender(), command, key);
                    comm.setHash(hash);
                    comm.setResult(result);
                    commandids.put(key, comm);
                }
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (InvalidConfigurationException e) {
            }
        }
    }

    public EnjinLogInterface getMcLogListener() {
        return mcloglistener;
    }

    public String getLastLogLine() {
        return mcloglistener.getLastLine();
    }

    public static boolean supportsUUID() {
        return supportsuuid;
    }

    public static boolean econUpdated() {
        return !econcompatmode;
    }

    public boolean isVanished(Player player) {
        if (vanishmanager != null) {
            try {
                return vanishmanager.isVanished(player);
                //Make sure old versions of Vanish don't mess up the plugin.
            } catch (Throwable e) {
                return vanishmanager.isVanished(player.getName());
            }
        } else {
            return false;
        }
    }

    private void addCustomData(ItemStack is, String[] args, OfflinePlayer reciever, int startingpos) {
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

    private void pollModules() {
        if (System.currentTimeMillis() - modulesLastPolled > 10 * 60 * 1000) {
            modulesLastPolled = System.currentTimeMillis();
            RPCData<Map<Integer, Module>> data = EnjinServices.getService(TicketService.class).getModules(getAuthKey());

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

    @Override
    public InstructionHandler getInstructionHandler() {
        return instructionHandler;
    }
}