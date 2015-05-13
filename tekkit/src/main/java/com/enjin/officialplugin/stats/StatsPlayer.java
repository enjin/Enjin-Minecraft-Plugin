package com.enjin.officialplugin.stats;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.EntityType;

import com.enjin.proto.stats.EnjinStats;
import com.enjin.proto.stats.EnjinStats.Server.Player.Blocks;
import com.enjin.proto.stats.EnjinStats.Server.Player.Blocks.Block;

public class StatsPlayer {

    private String name;
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
    private int brokenblocks = 0;
    private int placedblocks = 0;
    private ConcurrentHashMap<String, Integer> brokenblocktypes = new ConcurrentHashMap<String, Integer>();
    private ConcurrentHashMap<String, Integer> placedblocktypes = new ConcurrentHashMap<String, Integer>();

    private int totalxp = 0;
    private int xplevel = 0;

    private int chats = 0;

    public StatsPlayer(String name) {
        this.name = name;
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
        String blockid = block.getTypeId() + "-" + block.getData();
        int blocksbroken = 0;
        if (brokenblocktypes.containsKey(blockid)) {
            blocksbroken = brokenblocktypes.get(blockid).intValue();
        }
        blocksbroken++;
        brokenblocktypes.put(blockid, new Integer(blocksbroken));
    }

    public void addPlacedBlock(org.bukkit.block.Block block) {
        placedblocks++;
        String blockid = block.getTypeId() + "-" + block.getData();
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

    public StatsPlayer(EnjinStats.Server.Player playerstats) {
        name = playerstats.getName();
        firsttimeplayer = playerstats.getFirstimeplayer();
        deaths = playerstats.getDeaths();
        killed = playerstats.getKilled();
        EnjinStats.Server.Player.Distance distance = playerstats.getDistance();
        footdistance = distance.getFoot();
        boatdistance = distance.getBoat();
        pigdistance = distance.getPig();
        minecartdistance = distance.getMinecart();
        Blocks blocks = playerstats.getBlocks();
        brokenblocks = blocks.getBroken();
        placedblocks = blocks.getPlaced();
        List<Block> brokenblocklist = blocks.getBrokenblocksList();
        for (Block block : brokenblocklist) {
            brokenblocktypes.put(block.getId(), new Integer(block.getCount()));
        }
        List<Block> placedblocklist = blocks.getPlacedblocksList();
        for (Block block : placedblocklist) {
            placedblocktypes.put(block.getId(), new Integer(block.getCount()));
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

    public synchronized EnjinStats.Server.Player getSerialized() {
        Blocks.Builder blocks = Blocks.newBuilder();
        for (Entry<String, Integer> blockdata : brokenblocktypes.entrySet()) {
            blocks.addBrokenblocks(Block.newBuilder().setId(blockdata.getKey()).setCount(blockdata.getValue().intValue()).build());
        }
        for (Entry<String, Integer> blockdata : placedblocktypes.entrySet()) {
            blocks.addPlacedblocks(Block.newBuilder().setId(blockdata.getKey()).setCount(blockdata.getValue().intValue()).build());
        }
        EnjinStats.Server.Player.Builder stats = EnjinStats.Server.Player.newBuilder().setName(name).
                setFirstimeplayer(firsttimeplayer).setDeaths(deaths).setKilled(killed).setDistance(
                EnjinStats.Server.Player.Distance.newBuilder().setFoot(getRealDistance(footdistance)).
                        setBoat(getRealDistance(boatdistance)).setPig(getRealDistance(pigdistance)).
                        setMinecart(getRealDistance(minecartdistance)).build()).setBlocks(blocks.setBroken(brokenblocks).setPlaced(placedblocks));
        stats.setPvekills(pvekills).setPvpkills(pvpkills).setXp(totalxp).setXplevel(xplevel);
        for (Entry<EntityType, Integer> ent : creaturekills.entrySet()) {
            try {
                stats.addPveentitykills(EnjinStats.Server.Player.PveKills.newBuilder().setMob(ent.getKey().getName()).setKills(ent.getValue()).build());
            } catch (Exception e) {
                //Somehow we are getting an NPE sometimes? Ignore it.
            }
        }
        stats.setChatlines(chats);
        return stats.build();
    }

    private double getRealDistance(double squareddistance) {
        return Math.sqrt(squareddistance);
    }
}
