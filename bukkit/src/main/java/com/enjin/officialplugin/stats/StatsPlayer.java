package com.enjin.officialplugin.stats;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.ShopUtils;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;

/**
 * This is the class behind all stats for each player. Example to get the StatsPlayer and set a custom variable:<br>
 * <code>
 * if(enjinplugin.collectstats) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;StatsPlayer statplayer = enjinplugin.getPlayerStats(player);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;statplayer.addCustomStat(getName(),"CustomStatName",value,true/false);<br>
 * }
 * </code>
 *
 * @author Enjin.com
 */
public class StatsPlayer {

    private String name;
    private String uuid = "";
    private boolean firsttimeplayer = false;
    private int deaths = 0;
    private int killed = 0;
    private int pvpkills = 0;
    private int pvekills = 0;
    ConcurrentHashMap<EntityType, Integer> creaturekills = new ConcurrentHashMap<EntityType, Integer>();

    private double footdistance = 0;
    private double boatdistance = 0;
    private double pigdistance = 0;
    private double minecartdistance = 0;
    private double horsedistance = 0;
    private int brokenblocks = 0;
    private int placedblocks = 0;
    private ConcurrentHashMap<String, Integer> brokenblocktypes = new ConcurrentHashMap<String, Integer>();
    private ConcurrentHashMap<String, Integer> placedblocktypes = new ConcurrentHashMap<String, Integer>();

    private ConcurrentHashMap<String, ConcurrentHashMap<String, StatValue>> customstats = new ConcurrentHashMap<String, ConcurrentHashMap<String, StatValue>>();

    private int totalxp = 0;
    private int xplevel = 0;

    private int chats = 0;

    public StatsPlayer(OfflinePlayer player) {
        name = player.getName();
        if (EnjinMinecraftPlugin.supportsUUID()) {
            uuid = player.getUniqueId().toString();
        }
    }

    public void addDeath() {
        deaths++;
    }

    public void addKilled() {
        killed++;
    }

    public int getPvpkills() {
        return pvpkills;
    }

    public void addPvpkill() {
        pvpkills++;
    }

    public void setPvpkills(int pvpkills) {
        this.pvpkills = pvpkills;
    }

    public int getTotalPvekills() {
        return pvekills;
    }

    public int getPveEntitykills(EntityType entity) {
        int entitykills = 0;
        if (creaturekills.containsKey(entity)) {
            entitykills = creaturekills.get(entity);
        }
        return entitykills;
    }

    public void addPvekill(EntityType entity) {
        pvekills++;
        int entitykills = 0;
        if (creaturekills.containsKey(entity)) {
            entitykills = creaturekills.get(entity);
        }
        entitykills++;
        creaturekills.put(entity, new Integer(entitykills));
    }

    public void setTotalPvekills(int pvekills) {
        this.pvekills = pvekills;
    }

    public void setPveEntitykills(EntityType entity, int killed) {
        creaturekills.put(entity, new Integer(pvekills));
    }

    public void addBrokenBlock(org.bukkit.block.Block block) {
        brokenblocks++;
        String blockid = block.getType().toString() + "-" + block.getData();
        int blocksbroken = 0;
        if (brokenblocktypes.containsKey(blockid)) {
            blocksbroken = brokenblocktypes.get(blockid).intValue();
        }
        blocksbroken++;
        brokenblocktypes.put(blockid, new Integer(blocksbroken));
    }

    public void addPlacedBlock(org.bukkit.block.Block block) {
        placedblocks++;
        String blockid = block.getType().toString() + "-" + block.getData();
        int blocksplaced = 0;
        if (placedblocktypes.containsKey(blockid)) {
            blocksplaced = placedblocktypes.get(blockid).intValue();
        }
        blocksplaced++;
        placedblocktypes.put(blockid, new Integer(blocksplaced));
    }

    public String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized boolean isFirsttimeplayer() {
        return firsttimeplayer;
    }

    public synchronized void setFirsttimeplayer(boolean firsttimeplayer) {
        this.firsttimeplayer = firsttimeplayer;
    }

    public synchronized int getDeaths() {
        return deaths;
    }

    public synchronized void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public synchronized int getKilled() {
        return killed;
    }

    public synchronized void setKilled(int killed) {
        this.killed = killed;
    }

    public synchronized double getFootdistance() {
        return footdistance;
    }

    public synchronized void addFootdistance(double footdistance) {
        this.footdistance += footdistance;
    }

    public synchronized void setFootdistance(double footdistance) {
        this.footdistance = footdistance;
    }

    public synchronized double getBoatdistance() {
        return boatdistance;
    }

    public synchronized void addBoatdistance(double boatdistance) {
        this.boatdistance += boatdistance;
    }

    public synchronized void setBoatdistance(double boatdistance) {
        this.boatdistance = boatdistance;
    }

    public synchronized double getPigdistance() {
        return pigdistance;
    }

    public synchronized void addPigdistance(double pigdistance) {
        this.pigdistance += pigdistance;
    }

    public synchronized void setPigdistance(double pigdistance) {
        this.pigdistance = pigdistance;
    }

    public synchronized double getMinecartdistance() {
        return minecartdistance;
    }

    public synchronized void addMinecartdistance(double minecartdistance) {
        this.minecartdistance += minecartdistance;
    }

    public synchronized void setMinecartdistance(double minecartdistance) {
        this.minecartdistance = minecartdistance;
    }

    public synchronized int getBrokenblocks() {
        return brokenblocks;
    }

    public synchronized void setBrokenblocks(int brokenblocks) {
        this.brokenblocks = brokenblocks;
    }

    public synchronized int getPlacedblocks() {
        return placedblocks;
    }

    public synchronized void setPlacedblocks(int placedblocks) {
        this.placedblocks = placedblocks;
    }

    public synchronized ConcurrentHashMap<String, Integer> getBrokenblocktypes() {
        return brokenblocktypes;
    }

    public synchronized void setBrokenblocktypes(
            ConcurrentHashMap<String, Integer> brokenblocktypes) {
        this.brokenblocktypes = brokenblocktypes;
    }

    public synchronized ConcurrentHashMap<String, Integer> getPlacedblocktypes() {
        return placedblocktypes;
    }

    public synchronized void setPlacedblocktypes(
            ConcurrentHashMap<String, Integer> placedblocktypes) {
        this.placedblocktypes = placedblocktypes;
    }

    public StatsPlayer(JSONObject playerstats) {
        name = playerstats.get("username").toString();
        uuid = playerstats.get("uuid").toString();
        firsttimeplayer = ShopUtils.getBoolean(playerstats.get("firsttimeplayer"));
        deaths = StatsUtils.getInt(playerstats.get("deaths"));
        killed = StatsUtils.getInt(playerstats.get("killed"));
        pvekills = StatsUtils.getInt(playerstats.get("pvekills"));
        pvpkills = StatsUtils.getInt(playerstats.get("pvpkills"));
        totalxp = StatsUtils.getInt(playerstats.get("totalxp"));
        xplevel = StatsUtils.getInt(playerstats.get("xplevel"));
        Object odistance = playerstats.get("distance");
        if (odistance instanceof JSONObject) {
            JSONObject distance = (JSONObject) odistance;
            footdistance = StatsUtils.getDouble(distance.get("foot"));
            footdistance *= footdistance;
            boatdistance = StatsUtils.getDouble(distance.get("boat"));
            boatdistance *= boatdistance;
            pigdistance = StatsUtils.getDouble(distance.get("pig"));
            pigdistance *= pigdistance;
            minecartdistance = StatsUtils.getDouble(distance.get("minecart"));
            minecartdistance *= minecartdistance;
            horsedistance = StatsUtils.getDouble(distance.get("horse"));
            horsedistance *= horsedistance;
        }
        Object oblocks = playerstats.get("blocks");
        if (oblocks instanceof JSONObject) {
            JSONObject blocks = (JSONObject) oblocks;
            brokenblocks = StatsUtils.getInt(blocks.get("broken"));
            placedblocks = StatsUtils.getInt(blocks.get("placed"));
            Object obrokenblocks = blocks.get("brokenblocklist");
            Object oplacedblocks = blocks.get("placedblocklist");
            if (obrokenblocks instanceof JSONObject) {
                JSONObject jbrokenblocks = (JSONObject) obrokenblocks;
                Set<Map.Entry> eblocks = jbrokenblocks.entrySet();
                for (Map.Entry block : eblocks) {
                    String id = block.getKey().toString();
                    int count = StatsUtils.getInt(block.getValue());
                    brokenblocktypes.put(id, count);
                }
            }
            if (oplacedblocks instanceof JSONObject) {
                JSONObject jplacedblocks = (JSONObject) oplacedblocks;
                Set<Map.Entry> eblocks = jplacedblocks.entrySet();
                for (Map.Entry block : eblocks) {
                    String id = block.getKey().toString();
                    int count = StatsUtils.getInt(block.getValue());
                    placedblocktypes.put(id, count);
                }
            }
        }
        Object ocustom = playerstats.get("customstats");
        if (ocustom instanceof JSONObject) {
            JSONObject jcustom = (JSONObject) ocustom;
            Set<Map.Entry> scustom = jcustom.entrySet();
            for (Map.Entry mplugin : scustom) {
                String pluginname = mplugin.getKey().toString();
                if (mplugin.getValue() instanceof JSONArray) {
                    JSONArray jstats = (JSONArray) mplugin.getValue();
                    ConcurrentHashMap<String, StatValue> cmap = new ConcurrentHashMap<String, StatValue>();
                    for (Object stat : jstats) {
                        if (stat instanceof JSONObject) {
                            JSONObject jstat = (JSONObject) stat;
                            String name = jstat.get("name").toString();
                            String value = jstat.get("value").toString();
                            boolean relative = (Boolean) jstat.get("relative");
                            StatValue svalue;
                            if (value.indexOf(".") > -1) {
                                double dvalue = Double.parseDouble(value);
                                svalue = new StatValue(dvalue, relative);
                            } else {
                                int ivalue = Integer.parseInt(value);
                                svalue = new StatValue(ivalue, relative);
                            }
                            cmap.put(name, svalue);
                        }
                    }
                    if (cmap.size() > 0) {
                        customstats.put(pluginname, cmap);
                    }
                }
                Object ostats = mplugin.getValue();
            }
        }
    }

    public synchronized int getTotalxp() {
        return totalxp;
    }

    public synchronized void setTotalxp(int totalxp) {
        this.totalxp = totalxp;
    }

    public synchronized int getXplevel() {
        return xplevel;
    }

    public synchronized void setXplevel(int xplevel) {
        this.xplevel = xplevel;
    }

    public synchronized void addChatLine() {
        chats++;
    }

    public synchronized int getChatLines() {
        return chats;
    }

    public synchronized void setChatLines(int chats) {
        this.chats = chats;
    }

    @SuppressWarnings("unchecked")
    public synchronized JSONObject getSerialized() {
        JSONObject player = new JSONObject();
        JSONObject blocks = new JSONObject();
        JSONObject jbrokenblocks = new JSONObject();
        for (Entry<String, Integer> blockdata : brokenblocktypes.entrySet()) {
            jbrokenblocks.put(blockdata.getKey(), blockdata.getValue());
        }
        blocks.put("brokenblocklist", jbrokenblocks);
        JSONObject jplacedblocks = new JSONObject();
        for (Entry<String, Integer> blockdata : placedblocktypes.entrySet()) {
            jplacedblocks.put(blockdata.getKey(), blockdata.getValue());
        }
        blocks.put("placedblocklist", jplacedblocks);
        blocks.put("broken", new Integer(brokenblocks));
        blocks.put("placed", new Integer(placedblocks));
        player.put("blocks", blocks);
        player.put("username", name);
        player.put("uuid", uuid);
        player.put("firsttimeplayer", new Boolean(firsttimeplayer));
        player.put("deaths", new Integer(deaths));
        player.put("killed", new Integer(killed));
        JSONObject jdistance = new JSONObject();
        jdistance.put("foot", new Double(getRealDistance(footdistance)));
        jdistance.put("boat", new Double(getRealDistance(boatdistance)));
        jdistance.put("pig", new Double(getRealDistance(pigdistance)));
        jdistance.put("minecart", new Double(getRealDistance(minecartdistance)));
        jdistance.put("horse", new Double(getRealDistance(horsedistance)));
        player.put("distance", jdistance);
        player.put("pvekills", new Integer(pvekills));
        player.put("pvpkills", new Integer(pvpkills));
        player.put("totalxp", new Integer(totalxp));
        player.put("xplevel", new Integer(xplevel));
        if (EnjinMinecraftPlugin.economy != null) {
            if (EnjinMinecraftPlugin.supportsUUID() && EnjinMinecraftPlugin.econUpdated()) {
                OfflinePlayer oplayer = null;
                try {
                    oplayer = Bukkit.getOfflinePlayer(UUID.fromString(getUUID()));
                } catch (IllegalArgumentException e) {

                }
                if (oplayer == null || oplayer.getName() == null || oplayer.getName().equals("")) {
                    oplayer = Bukkit.getOfflinePlayer(getName());
                }
                try {
                    if (EnjinMinecraftPlugin.economy.hasAccount(oplayer)) {
                        player.put("moneyamount", EnjinMinecraftPlugin.economy.getBalance(oplayer));
                    }
                } catch (Exception e) {

                }
            } else {
                try {
                    if (EnjinMinecraftPlugin.economy.hasAccount(getName())) {
                        player.put("moneyamount", EnjinMinecraftPlugin.economy.getBalance(getName()));
                    }
                } catch (Exception e) {

                }
            }
        }
        JSONObject pveentitykills = new JSONObject();
        for (Entry<EntityType, Integer> ent : creaturekills.entrySet()) {
            try {
                pveentitykills.put(ent.getKey().name(), new Integer(ent.getValue()));
            } catch (Exception e) {
                //Somehow we are getting an NPE sometimes? Ignore it.
            }
        }
        player.put("pveentitykills", pveentitykills);
        player.put("chatlines", new Integer(chats));
        if (EnjinMinecraftPlugin.isMcMMOEnabled()) {
            Player bplayer = Bukkit.getPlayerExact(name);
            JSONObject mcmmoskills = new JSONObject();
            List<SkillType> skills = SkillType.NON_CHILD_SKILLS;
            for (SkillType type : skills) {
                try {
                    int level = 0;
                    if (bplayer != null) {
                        level = ExperienceAPI.getLevel(bplayer, type.toString());
                    } else {
                        level = ExperienceAPI.getLevelOffline(name, type.toString());
                    }
                    mcmmoskills.put(type.toString(), new Integer(level));
                } catch (Exception e) {

                }
            }
            player.put("mcmmo", mcmmoskills);
        }
        if (customstats.size() > 0) {
            JSONObject jcustomstats = new JSONObject();
            Iterator<Entry<String, ConcurrentHashMap<String, StatValue>>> statsiterator = customstats.entrySet().iterator();
            while (statsiterator.hasNext()) {
                Entry<String, ConcurrentHashMap<String, StatValue>> statentry = statsiterator.next();
                String pluginname = statentry.getKey();
                Iterator<Entry<String, StatValue>> pluginstats = statentry.getValue().entrySet().iterator();
                JSONArray statsarray = new JSONArray();
                while (pluginstats.hasNext()) {
                    JSONObject jstat = new JSONObject();
                    Entry<String, StatValue> stat = pluginstats.next();
                    jstat.put("name", stat.getKey());
                    jstat.put("value", stat.getValue().getStat());
                    jstat.put("relative", stat.getValue().isRelative());
                    statsarray.add(jstat);
                }
                jcustomstats.put(pluginname, statsarray);
            }
            player.put("customstats", jcustomstats);
        }
        return player;
    }

    private double getRealDistance(double squareddistance) {
        return Math.sqrt(squareddistance);
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     * This adds a custom stat to the stats sending of the plugin.
     *
     * @param plugin        the name of the plugin submitting the stat.
     * @param statName      the name of the stat you are submitting.
     * @param value         The value of the stat.
     * @param addtoexisting whether you want to add to the existing stat (true) or replace the existing stat with this (false)
     */
    public void addCustomStat(String plugin, String statName, int value, boolean addtoexisting) {
        ConcurrentHashMap<String, StatValue> pluginstats = customstats.get(plugin);
        if (pluginstats == null) {
            pluginstats = new ConcurrentHashMap<String, StatValue>();
            customstats.put(plugin, pluginstats);
        }
        if (addtoexisting) {
            StatValue stat = pluginstats.get(statName);
            if (stat == null) {
                stat = new StatValue(value, addtoexisting);
                pluginstats.put(statName, stat);
            } else {
                stat.addStat(value);
            }
        } else {
            pluginstats.put(statName, new StatValue(value, addtoexisting));
        }
    }

    /**
     * This adds a custom stat to the stats sending of the plugin.
     *
     * @param plugin        the name of the plugin submitting the stat.
     * @param statName      the name of the stat you are submitting.
     * @param value         The value of the stat.
     * @param addtoexisting whether you want to add to the existing stat (true) or replace the existing stat with this (false)
     */
    public void addCustomStat(String plugin, String statName, double value, boolean addtoexisting) {
        ConcurrentHashMap<String, StatValue> pluginstats = customstats.get(plugin);
        if (pluginstats == null) {
            pluginstats = new ConcurrentHashMap<String, StatValue>();
            customstats.put(plugin, pluginstats);
        }
        if (addtoexisting) {
            StatValue stat = pluginstats.get(statName);
            if (stat == null) {
                stat = new StatValue(value, addtoexisting);
                pluginstats.put(statName, stat);
            } else {
                stat.addStat(value);
            }
        } else {
            pluginstats.put(statName, new StatValue(value, addtoexisting));
        }
    }

    /**
     * This adds a custom stat to the stats sending of the plugin.
     *
     * @param plugin        the name of the plugin submitting the stat.
     * @param statName      the name of the stat you are submitting.
     * @param value         The value of the stat.
     * @param addtoexisting whether you want to add to the existing stat (true) or replace the existing stat with this (false)
     */
    public void addCustomStat(String plugin, String statName, float value, boolean addtoexisting) {
        ConcurrentHashMap<String, StatValue> pluginstats = customstats.get(plugin);
        if (pluginstats == null) {
            pluginstats = new ConcurrentHashMap<String, StatValue>();
            customstats.put(plugin, pluginstats);
        }
        if (addtoexisting) {
            StatValue stat = pluginstats.get(statName);
            if (stat == null) {
                stat = new StatValue(value, addtoexisting);
                pluginstats.put(statName, stat);
            } else {
                stat.addStat(value);
            }
        } else {
            pluginstats.put(statName, new StatValue(value, addtoexisting));
        }
    }
}