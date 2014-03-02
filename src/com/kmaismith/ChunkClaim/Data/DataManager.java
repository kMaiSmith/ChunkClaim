package com.kmaismith.ChunkClaim.Data;

import com.kmaismith.ChunkClaim.ChunkClaimLogger;
import org.bukkit.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class DataManager {

    private HashMap<String, ChunkData> chunks = new HashMap<String, ChunkData>();
    private HashMap<String, PlayerData> playerNameToPlayerDataMap = new HashMap<String, PlayerData>();

    private final IDataStore dataStore;
    private final ChunkClaimLogger logger;

    public DataManager(ChunkClaimLogger logger) {
        this.dataStore = new FlatFileDataStore(logger);
        this.logger = logger;
        initialize();

    }

    void initialize() {
        File[] chunkDataDir = new File(ChunkData.chunkDataFolderPath).listFiles();

        if (chunkDataDir == null) {
            return;
        }

        for (File file : chunkDataDir) {

            ChunkData loadedChunk = new ChunkData(file);

            logger.addLogEntry("found chunk at " + file.getName());

            dataStore.loadDataFromFile(loadedChunk);

            String chunkAddress = loadedChunk.getChunkWorld() + ":" +
                    Integer.toString(loadedChunk.getChunkX()) + ":" +
                    Integer.toString(loadedChunk.getChunkZ());

            chunks.put(chunkAddress, loadedChunk);
        }

        System.gc();
    }

    public ChunkData getChunkAt(Location location) {

        int x = location.getChunk().getX();
        int z = location.getChunk().getZ();
        String world = location.getWorld().getName();

        String addressString = world + ":" + Integer.toString(x) + ":" + Integer.toString(z);

        return chunks.get(addressString);
    }

    private Collection<ChunkData> getAllChunks() {
        return chunks.values();
    }

    public void clearCachedPlayerData(String playerName) {
        this.playerNameToPlayerDataMap.remove(playerName);
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
            playerData = new PlayerData(playerName);
            if (!dataStore.loadDataFromFile(playerData)) {
                dataStore.writeDataToFile(playerData);
            }


            this.playerNameToPlayerDataMap.put(playerName, playerData);
        }

        return playerNameToPlayerDataMap.get(playerName);
    }

    public int deleteChunksForPlayer(String playerName) {
        ArrayList<ChunkData> playerChunks = getChunksForPlayer(playerName);
        PlayerData playerData = this.readPlayerData(playerName);
        for (ChunkData playerChunk : playerChunks) {
            this.deleteChunk(playerChunk);
            playerData.addCredit();
        }
        return playerChunks.size();
    }

    public void deleteChunk(ChunkData chunk) {

        dataStore.deleteData(chunk);

        String chunkAddress = chunk.getChunkWorld() + ":" +
                Integer.toString(chunk.getChunkX()) + ":" +
                Integer.toString(chunk.getChunkZ());

        this.chunks.remove(chunkAddress);

        // @TODO: figure out if regenerating of the chunk is necessary
        //ChunkClaim.plugin.regenerateChunk(chunk);
    }

    public ChunkData addChunk(ChunkData newChunk) {
        String addressString = newChunk.getChunkWorld() + ":" +
                Integer.toString(newChunk.getChunkX()) + ":" +
                Integer.toString(newChunk.getChunkZ());

        if (!this.chunks.containsKey(addressString)) {
            newChunk = new ChunkData(new File(ChunkData.chunkDataFolderPath + File.separator + addressString + ".dat"), newChunk);
            dataStore.writeDataToFile(newChunk);
            chunks.put(addressString, newChunk);
        }
        return newChunk;
    }

    // @TODO: modify functionality of dataManager to not need clients to manually write to dataStore
    public void savePlayerData(PlayerData playerData) {
        dataStore.writeDataToFile(playerData);
    }

    // @TODO: modify functionality of dataManager to not need clients to manually write to dataStore
    public void writeChunkToStorage(ChunkData chunkPlot) {
        dataStore.writeDataToFile(chunkPlot);
    }
}
