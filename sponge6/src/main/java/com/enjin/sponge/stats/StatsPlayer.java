package com.enjin.sponge.stats;

import com.enjin.sponge.utils.PrimitiveUtils;
import lombok.Getter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    @Getter
    private String name;
    @Getter
    private String uuid = "";
    @Getter
    private boolean firsttimeplayer = false;
    @Getter
    private int deaths = 0;
    @Getter
    private int killed = 0;
    @Getter
    private int pvpkills = 0;
    @Getter
    private int pvekills = 0;
    @Getter
    private double footdistance = 0;
    @Getter
    private double boatdistance = 0;
    @Getter
    private double pigdistance = 0;
    @Getter
    private double minecartdistance = 0;
    @Getter
    private double horsedistance = 0;
    @Getter
    private int brokenblocks = 0;
    @Getter
    private int placedblocks = 0;
    @Getter
    private ConcurrentHashMap<EntityType, Integer> creaturekills = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentHashMap<String, Integer> brokenblocktypes = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentHashMap<String, Integer> placedblocktypes = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentHashMap<String, ConcurrentHashMap<String, StatValue>> customstats = new ConcurrentHashMap<>();
    @Getter
    private int totalxp = 0;
    @Getter
    private int xplevel = 0;
    @Getter
    private int chats = 0;

    public StatsPlayer(Player player) {
        name = player.getName();
        uuid = player.getUniqueId().toString();
    }

    public StatsPlayer(JSONObject playerstats) {
        name = playerstats.get("username").toString();
        uuid = playerstats.get("uuid").toString();
        firsttimeplayer = PrimitiveUtils.getBoolean(playerstats.get("firsttimeplayer"));
        deaths = PrimitiveUtils.getInt(playerstats.get("deaths"));
        killed = PrimitiveUtils.getInt(playerstats.get("killed"));
        pvekills = PrimitiveUtils.getInt(playerstats.get("pvekills"));
        pvpkills = PrimitiveUtils.getInt(playerstats.get("pvpkills"));
        totalxp = PrimitiveUtils.getInt(playerstats.get("totalxp"));
        xplevel = PrimitiveUtils.getInt(playerstats.get("xplevel"));
        Object odistance = playerstats.get("distance");

        if (odistance instanceof JSONObject) {
            JSONObject distance = (JSONObject) odistance;
            footdistance = PrimitiveUtils.getDouble(distance.get("foot"));
            footdistance *= footdistance;
            boatdistance = PrimitiveUtils.getDouble(distance.get("boat"));
            boatdistance *= boatdistance;
            pigdistance = PrimitiveUtils.getDouble(distance.get("pig"));
            pigdistance *= pigdistance;
            minecartdistance = PrimitiveUtils.getDouble(distance.get("minecart"));
            minecartdistance *= minecartdistance;
            horsedistance = PrimitiveUtils.getDouble(distance.get("horse"));
            horsedistance *= horsedistance;
        }

        Object oblocks = playerstats.get("blocks");
        if (oblocks instanceof JSONObject) {
            JSONObject blocks = (JSONObject) oblocks;
            brokenblocks = PrimitiveUtils.getInt(blocks.get("broken"));
            placedblocks = PrimitiveUtils.getInt(blocks.get("placed"));
            Object obrokenblocks = blocks.get("brokenblocklist");
            Object oplacedblocks = blocks.get("placedblocklist");
            if (obrokenblocks instanceof JSONObject) {
                JSONObject jbrokenblocks = (JSONObject) obrokenblocks;
                Set<Entry<String, Object>> eblocks = jbrokenblocks.entrySet();
                for (Entry block : eblocks) {
                    String id = block.getKey().toString();
                    int count = PrimitiveUtils.getInt(block.getValue());
                    brokenblocktypes.put(id, count);
                }
            }

            if (oplacedblocks instanceof JSONObject) {
                JSONObject jplacedblocks = (JSONObject) oplacedblocks;
                Set<Entry<String, Object>> eblocks = jplacedblocks.entrySet();
                for (Entry block : eblocks) {
                    String id = block.getKey().toString();
                    int count = PrimitiveUtils.getInt(block.getValue());
                    placedblocktypes.put(id, count);
                }
            }
        }

        Object ocustom = playerstats.get("customstats");
        if (ocustom instanceof JSONObject) {
            JSONObject jcustom = (JSONObject) ocustom;
            Set<Entry<String, Object>> scustom = jcustom.entrySet();
            for (Entry mplugin : scustom) {
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
            }
        }
    }

    public void addDeath() {
        deaths++;
    }

    public void addKilled() {
        killed++;
    }

    public void addPvpkill() {
        pvpkills++;
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

    public void addBrokenBlock(BlockType type) {
        brokenblocks++;
        String blockid = type.getId().replace("minecraft:", "");
        int blocksbroken = 0;
        if (brokenblocktypes.containsKey(blockid)) {
            blocksbroken = brokenblocktypes.get(blockid).intValue();
        }
        blocksbroken++;
        brokenblocktypes.put(blockid, new Integer(blocksbroken));
    }

    public void addPlacedBlock(BlockType type) {
        placedblocks++;
        String blockid = type.getId().replace("minecraft:", "");
        int blocksplaced = 0;

        if (placedblocktypes.containsKey(blockid)) {
            blocksplaced = placedblocktypes.get(blockid).intValue();
        }

        blocksplaced++;
        placedblocktypes.put(blockid, new Integer(blocksplaced));
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addFootdistance(double footdistance) {
        this.footdistance += footdistance;
    }

    public void addBoatdistance(double boatdistance) {
        this.boatdistance += boatdistance;
    }

    public void addPigdistance(double pigdistance) {
        this.pigdistance += pigdistance;
    }

    public void addMinecartdistance(double minecartdistance) {
        this.minecartdistance += minecartdistance;
    }

    public void setTotalXp(int totalxp) {
        this.totalxp = totalxp;
    }

    public void setXpLevel(int xplevel) {
        this.xplevel = xplevel;
    }

    public void addChatLine() {
        chats++;
    }

    public JSONObject getSerialized() {
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
        player.put("firsttimeplayer", Boolean.valueOf(firsttimeplayer));
        player.put("deaths", new Integer(deaths));
        player.put("killed", new Integer(killed));
        player.put("pvekills", new Integer(pvekills));
        player.put("pvpkills", new Integer(pvpkills));
        player.put("totalxp", new Integer(totalxp));
        player.put("xplevel", new Integer(xplevel));
        player.put("chatlines", new Integer(chats));

        JSONObject jdistance = new JSONObject();
        jdistance.put("foot", new Double(getRealDistance(footdistance)));
        jdistance.put("boat", new Double(getRealDistance(boatdistance)));
        jdistance.put("pig", new Double(getRealDistance(pigdistance)));
        jdistance.put("minecart", new Double(getRealDistance(minecartdistance)));
        jdistance.put("horse", new Double(getRealDistance(horsedistance)));

        player.put("distance", jdistance);

        JSONObject pveentitykills = new JSONObject();
        for (Entry<EntityType, Integer> ent : creaturekills.entrySet()) {
            try {
                pveentitykills.put(ent.getKey().getName(), ent.getValue());
            } catch (Exception e) {
                //Somehow we are getting an NPE sometimes? Ignore it.
            }
        }

        player.put("pveentitykills", pveentitykills);

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

    public void setUuid(String uuid) {
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