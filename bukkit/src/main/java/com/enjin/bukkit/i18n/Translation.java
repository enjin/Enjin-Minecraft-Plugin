package com.enjin.bukkit.i18n;

import com.enjin.bukkit.cmd.CommandContext;
import com.enjin.bukkit.cmd.SenderType;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.util.text.MessageUtil;
import com.enjin.core.Enjin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Translation {

    _locale("en_US"),
    _language("English"),
    _version(1),

    CommandApi_InvalidUsage("&cInvalid command usage!"),
    CommandApi_Usage("USAGE: %s"),
    CommandApi_Requirements_InvalidSenderTypePlayer("&cThis command cannot be used by players."),
    CommandApi_Requirements_InvalidSenderTypeConsole("&cThis command cannot be used by the console."),
    CommandApi_Requirements_InvalidSenderTypeRemote("&cThis command cannot be used by RCON."),
    CommandApi_Requirements_InvalidSenderTypeBlock("&cThis command cannot be used by command blocks."),
    CommandApi_Requirements_KeyRequiredAndInvalid("&cThis command can only be used upon successful authentication with Enjin CMS."),
    CommandApi_Requirements_NoPermission("&cYou do not have the permission required for this command: &6%s"),

    Command_Broadcast_Description("Broadcast a message to all players."),

    Command_Buy_Description("Show shop packages available for purchase."),
    Command_Buy_InvalidIdFormat("The id you specified is not valid. Must be a number."),
    Command_Buy_NoShopsDetected("&cNo shops detected at this time."),
    Command_Buy_NoItemsDetected("&cNo items detected in this category."),

    Command_Debug_Description("Enable debug mode and log additional information to console."),
    Command_Debug_Set("&aDebugging has been set to &6%s"),

    Command_Enjin_Description("Show information about the plugin and authors."),
    Command_Enjin_Info("&7Running &6%s &cv%s&7.<br>&7Use &6%s &7to view available commands."),

    Command_Help_Description("Show a list of commands with their usage."),

    Command_Key_Description("Set the secret key found at the \"Admin - Games - Minecraft - Enjin Plugin\" page."),
    Command_Key_CheckingValidity("Checking if the key is valid..."),
    Command_Key_AlreadyAuthenticated("&aKey already validated."),
    Command_Key_SuccessfulValidation("&aKey successfully validated."),
    Command_Key_UnsuccessfulValidation("&cCould not validate provided key."),

    Command_Lag_Description("Display average TPS and memory usage."),
    Command_Lag_AverageTps("&6Average TPS: &a%s"),
    Command_Lag_LastTps("&6Last TPS Measurement: &a%s"),
    Command_Lag_MemoryUsed("&6Memory Used: &a%sMB/%sMB"),

    Command_Message_Description("Send a message to a player."),

    Command_Push_Description("Sync player ranks with website tags."),
    Command_Push_Updating("&aUpdating player ranks next sync."),

    Command_Report_Description("Generate a report required by support for troubleshooting."),
    Command_Report_Generating("&aGenerating report, please wait..."),
    Command_Report_Generated("&aEnjin report saved to %s."),

    Command_Sign_Set_Description(""),
    Command_Sign_Update_Description(""),

    Command_Tags_Description("View a player's website tags."),
    Command_Tags_Empty("&c%s has no tags."),
    Command_Tags_List("&6%s's Tags: %s"),

    Errors_Error("&cError: &7%s"),
    Errors_Exception("&cException: &7%s"),
    Errors_Network_Connection("&cEncountered a connection error communicating with Enjin CMS servers."),

    Misc_NewLine("\n"),
    Misc_EmptyLine(""),

    Player_NotOnline("&c%s is not online."),

    Support_ContactIfPersists("&cContact support if the issue persists.");

    private static final Logger LOGGER = Logger.getLogger("EnjinCraft");
    public static final Locale DEFAULT_LOCALE = Locale.en_US;

    private static final Map<Locale, YamlConfiguration> LOCALE_CONFIGS = new EnumMap<>(Locale.class);
    private static final Map<Locale, String> LOCALE_NAMES = new EnumMap<>(Locale.class);
    private static Locale serverLocale = DEFAULT_LOCALE;

    private String path;
    private Object def;
    private int argCount;

    Translation(Object def) {
        this.path = this.name().replace('_', '.');
        if (this.path.startsWith("."))
            this.path = "internal" + path;
        this.def = def;
        this.argCount = getArgCount(String.valueOf(def));
    }

    public String path() {
        return path;
    }

    public String defaultTranslation() {
        return String.valueOf(def);
    }

    public String translation() {
        return translation(serverLocale);
    }

    public String translation(CommandSender sender) {
        if ((sender instanceof ConsoleCommandSender && conf().isTranslateConsoleMessages()) || sender instanceof Player)
            return translation();

        return defaultTranslation();
    }

    public String translation(SenderType type) {
        if ((type == SenderType.CONSOLE && conf().isTranslateConsoleMessages()) || type == SenderType.PLAYER)
            return translation();

        return defaultTranslation();
    }

    public String translation(Locale locale) {
        YamlConfiguration lang = LOCALE_CONFIGS.getOrDefault(locale, LOCALE_CONFIGS.get(DEFAULT_LOCALE));
        return lang.getString(path(), defaultTranslation());
    }

    public int version() {
        return Integer.parseInt(_version.translation());
    }

    public String locale() {
        return _locale.translation();
    }

    public void send(CommandSender sender, Object... args) {
        String formatted = String.format(translation(sender instanceof Player ? serverLocale : DEFAULT_LOCALE), args);
        String[] lines = formatted.split("<br>");
        for (String line : lines)
            MessageUtil.sendString(sender, line);
    }

    public void send(CommandContext context, Object... args) {
        send(context.getSender(), args);
    }

    private EMPConfig conf() {
        return (EMPConfig) Enjin.getConfiguration();
    }

    public static void setServerLocale(Locale locale) {
        serverLocale = locale;
    }

    public static Map<Locale, String> localeNames() {
        return LOCALE_NAMES;
    }

    public static void loadLocales(Plugin plugin) {
        for (Locale locale : Locale.values()) {
            YamlConfiguration lang = locale.loadLocaleResource(plugin);
            if (lang == null)
                lang = new YamlConfiguration();
            setDefaults(lang);
            LOCALE_CONFIGS.put(locale, lang);
            LOCALE_NAMES.put(locale, lang.getString(Translation._language.path()));
        }
    }

    protected static void setDefaults(YamlConfiguration lang) {
        if (lang == null)
            return;

        for (Translation translation : values()) {
            if (!lang.isSet(translation.path)) {
                lang.set(translation.path, translation.def);
            } else {
                int argCount = getArgCount(lang.getString(translation.path));
                if (argCount != translation.argCount)
                    lang.set(translation.path, translation.def);
            }
        }
    }

    protected static int getArgCount(String text) {
        int argCount = 0;
        Matcher matcher = Pattern.compile("%s").matcher(text);
        while (matcher.find())
            argCount += 1;
        return argCount;
    }

}
