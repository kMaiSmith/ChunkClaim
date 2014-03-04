/*
    ChunkClaim Plugin for Minecraft Bukkit Servers
    Copyright (C) 2012 Felix Schmidt
    Based on code by Felix Schmidt, Copyright (C) 2014 Kyle Smith

    This file is part of ChunkClaim.

    ChunkClaim is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ChunkClaim is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ChunkClaim.  If not, see <http://www.gnu.org/licenses/>.

    Contact: Kyle Smith <kMaiSmith@gmail.com>
 */

package com.kmaismith.chunkclaim.Data;

import com.kmaismith.chunkclaim.ChunkClaimLogger;
import org.bukkit.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class DataManager {

    private HashMap<String, ChunkData> chunks = new HashMap<String, ChunkData>();
    private HashMap<String, PlayerData> playerNameToPlayerDataMap = new HashMap<String, PlayerData>();

    private final IDataStore dataStore;

    public DataManager(ChunkClaimLogger logger) {
        this.dataStore = new FlatFileDataStore(logger);
        initialize();

    }

    void initialize() {
        File[] chunkDataDir = new File(ChunkData.chunkDataFolderPath).listFiles();

        if (chunkDataDir == null) {
            return;
        }

        for (File file : chunkDataDir) {

            ChunkData loadedChunk = new ChunkData(file);

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
