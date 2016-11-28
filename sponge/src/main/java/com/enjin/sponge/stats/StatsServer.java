package com.enjin.sponge.stats;

import com.enjin.core.Enjin;
import com.enjin.sponge.utils.PrimitiveUtils;
import org.json.simple.JSONObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StatsServer {
    private long lastserverstarttime = System.currentTimeMillis();
    private int totalkicks = 0;
    private ConcurrentHashMap<String, Integer> playerkicks = new ConcurrentHashMap<String, Integer>();
    private int creeperexplosions = 0;

    public StatsServer() {
        //
    }

    public StatsServer(JSONObject serverstats) {
        totalkicks = PrimitiveUtils.getInt(serverstats.get("totalkicks"));
        creeperexplosions = PrimitiveUtils.getInt(serverstats.get("creeperexplosions"));
        Object okicks = serverstats.get("playerskickedlist");
        if (okicks instanceof JSONObject) {
            JSONObject kicks = (JSONObject) okicks;
            Set<Entry<String, Object>> skicks = kicks.entrySet();
            for (Entry kick : skicks) {
                playerkicks.put(kick.getKey().toString(), PrimitiveUtils.getInt(kick.getValue()));
            }
        }
    }

    public long getLastserverstarttime() {
        return lastserverstarttime;
    }


    public void setLastserverstarttime(long lastserverstarttime) {
        this.lastserverstarttime = lastserverstarttime;
    }


    public int getTotalkicks() {
        return totalkicks;
    }

    public void addKick(String playername) {
        totalkicks++;
        int playerkick = 0;
        if (playerkicks.containsKey(playername)) {
            playerkick = playerkicks.get(playername);
        }
        playerkick++;
        playerkicks.put(playername, new Integer(playerkick));
    }


    public void setTotalkicks(int totalkicks) {
        this.totalkicks = totalkicks;
    }


    public int getCreeperexplosions() {
        return creeperexplosions;
    }

    public void addCreeperExplosion() {
        creeperexplosions += 1;
    }

    public void setCreeperexplosions(int creeperexplosions) {
        this.creeperexplosions = creeperexplosions;
    }

    @SuppressWarnings("unchecked")
    public JSONObject getSerialized() {
        JSONObject serverbuilder = new JSONObject();
        serverbuilder.put("creeperexplosions", new Integer(creeperexplosions));
        int totalentities = 0;
        try {
            Collection<World> worlds = Sponge.getServer().getWorlds();
            for (World world : worlds) {
                totalentities += world.getEntities().size();
            }
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }
        serverbuilder.put("totalentities", new Integer(totalentities));
        Runtime runtime = Runtime.getRuntime();
        long memused = runtime.totalMemory() / (1024 * 1024);
        long maxmemory = runtime.maxMemory() / (1024 * 1024);
        serverbuilder.put("maxmemory", new Integer((int) maxmemory));
        serverbuilder.put("memoryused", new Integer((int) memused));
        serverbuilder.put("javaversion", System.getProperty("java.version") + " " + System.getProperty("java.vendor"));
        serverbuilder.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        serverbuilder.put("corecount", new Integer(runtime.availableProcessors()));
        serverbuilder.put("serverversion", Sponge.getPlatform().getApi().getVersion().get());
        serverbuilder.put("laststarttime", new Integer((int) (lastserverstarttime / 1000)));
        JSONObject kickedplayers = new JSONObject();
        Set<Entry<String, Integer>> kicks = playerkicks.entrySet();
        for (Entry<String, Integer> kick : kicks) {
            kickedplayers.put(kick.getKey(), kick.getValue());
        }
        serverbuilder.put("playerskickedlist", kickedplayers);
        serverbuilder.put("totalkicks", new Integer(totalkicks));
        return serverbuilder;
    }

    public ConcurrentHashMap<String, Integer> getPlayerkicks() {
        return playerkicks;
    }

    public void reset() {
        totalkicks = 0;
        playerkicks.clear();
        creeperexplosions = 0;
    }
}