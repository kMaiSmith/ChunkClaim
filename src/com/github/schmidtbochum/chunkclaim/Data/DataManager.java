package com.github.schmidtbochum.chunkclaim.Data;

import com.github.schmidtbochum.chunkclaim.ChunkClaim;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kyle
 * Date: 1/15/14
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataManager {

    private HashMap<String, WorldData> worlds = new HashMap<String, WorldData>();

    private HashMap<String, PlayerData> playerNameToPlayerDataMap = new HashMap<String, PlayerData>();

    private IDataStore dataStore;

    public DataManager() {
        this.dataStore = new FlatFileDataStore();

        try {
            initialize();
        } catch (Exception e) {
            ChunkClaim.addLogEntry("Fatal exception " + e);
        }
    }

    void initialize() throws Exception {

        //load worlds
        List<String> worldNameList = ChunkClaim.plugin.config_worlds;

        for (String worldName : worldNameList) {
            if (Bukkit.getServer().getWorld(worldName) != null) {
                dataStore.loadWorldData(worldName);
            }
        }

        System.gc();
    }

    public void cleanUp() {

        Date now = new Date();
        double deletionTime = (1000 * 60 * 60 * 24) * ChunkClaim.plugin.config_autoDeleteDays;
        int r = 0;
        for (WorldData world : worlds.values()) {
            for (ChunkData chunkPlot : world.getAllChunks()) {
                long diff = now.getTime() - chunkPlot.getClaimDate().getTime();
                if (diff > deletionTime) {
                    PlayerData playerData = this.readPlayerData(chunkPlot.getOwnerName());
                    playerData.credits++;
                    dataStore.savePlayerData(chunkPlot.getOwnerName(), playerData);
                    this.playerNameToPlayerDataMap.remove(chunkPlot.getOwnerName());
                    ChunkClaim.addLogEntry("Auto-deleted chunk by " + chunkPlot.getOwnerName() + " at " + (chunkPlot.getChunk().getX() * 16) + " | " + (chunkPlot.getChunk().getZ() * 16));
                    this.deleteChunk(chunkPlot);
                    r++;
                }
            }
        }
    }

    public ChunkData getChunkAt(int x, int z, String worldName) {

        if (!worlds.containsKey(worldName)) return null;

        return worlds.get(worldName).getChunk(x, z);
    }

    public ChunkData getChunkAt(Location location) {

        int x = location.getChunk().getX();
        int z = location.getChunk().getZ();
        String world = location.getWorld().getName();

        return getChunkAt(x, z, world);
    }

    private ArrayList<ChunkData> getAllChunks() {
        ArrayList<ChunkData> allChunks = new ArrayList<ChunkData>();
        for (WorldData world : worlds.values()) {
            allChunks.addAll(world.getAllChunks());
        }
        return allChunks;
    }

    public void clearCachedPlayerData(String playerName) {
        this.playerNameToPlayerDataMap.remove(playerName);
    }

    public boolean ownsNear(Location location, String playerName) {
        int x = location.getChunk().getX();
        int z = location.getChunk().getZ();
        String worldName = location.getWorld().getName();

        ChunkData a = getChunkAt(x - 1, z, worldName);
        ChunkData c = getChunkAt(x + 1, z, worldName);
        ChunkData b = getChunkAt(x, z - 1, worldName);
        ChunkData d = getChunkAt(x, z + 1, worldName);

        return a != null && a.isTrusted(playerName) ||
                b != null && b.isTrusted(playerName) ||
                c != null && c.isTrusted(playerName) ||
                d != null && d.isTrusted(playerName);
    }

    public ArrayList<ChunkData> getChunksForPlayer(String playerName) {
        ArrayList<ChunkData> playerChunks = new ArrayList<ChunkData>();
        for (ChunkData chunk : getAllChunks()) {
            if (chunk.getOwnerName().equals(playerName)) {
                playerChunks.add(chunk);
            }
        }
        return playerChunks;
    }

    public PlayerData readPlayerData(String playerName) {

        PlayerData playerData = this.playerNameToPlayerDataMap.get(playerName);
        if (playerData == null) {
            playerData = dataStore.getPlayerDataFromStorage(playerName);
            this.playerNameToPlayerDataMap.put(playerName, playerData);
        }

        return playerNameToPlayerDataMap.get(playerName);
    }

    public int deleteChunksForPlayer(String playerName) {
        ArrayList<ChunkData> playerChunks = getChunksForPlayer(playerName);
        PlayerData playerData = this.readPlayerData(playerName);
        for (ChunkData playerChunk : playerChunks) {
            this.deleteChunk(playerChunk);
            playerData.credits++;
        }
        return playerChunks.size();
    }

    public void deleteChunk(ChunkData chunk) {

        this.worlds.get(chunk.getChunk().getWorld().getName()).removeChunk(chunk);
        dataStore.deleteChunkFromStorage(chunk);
        chunk.setInDataStore(false);

        // @TODO: figure out if regenerating of the chunk is necessary
        //ChunkClaim.plugin.regenerateChunk(chunk);
    }

    public void addChunk(ChunkData newChunk) {
        if (this.worlds.containsKey(newChunk.getChunk().getWorld().getName())) {
            this.worlds.get(newChunk.getChunk().getWorld().getName()).addChunk(newChunk);
            newChunk.setInDataStore(true);
            dataStore.writeChunkToStorage(newChunk);
        }
    }

    public void unloadWorldData(String worldName) {
        this.worlds.remove(worldName);
    }

    // @TODO: modify functionality of dataManager to not need clients to manually write to dataStore
    public void savePlayerData(String playerName, PlayerData playerData) {
        dataStore.savePlayerData(playerName, playerData);
    }

    // @TODO: modify functionality of dataManager to not need clients to manually write to dataStore
    public void writeChunkToStorage(ChunkData chunkPlot) {
        dataStore.writeChunkToStorage(chunkPlot);
    }

    public void loadWorldData(String world) {
        this.worlds.put(world, dataStore.loadWorldData(world));
    }

}
