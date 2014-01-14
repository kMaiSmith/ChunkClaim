/*
    ChunkClaim Plugin for Minecraft Bukkit Servers
    Copyright (C) 2012 Felix Schmidt
    
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
 */

package com.github.schmidtbochum.chunkclaim;

import org.bukkit.Location;

import java.io.File;
import java.util.*;

public abstract class DataStore {

    public int nextChunkId = 0;
    int minModifiedBlocks = 10;

    ArrayList<ChunkPlot> chunks = new ArrayList<ChunkPlot>();
    ArrayList<ChunkPlot> unusedChunks = new ArrayList<ChunkPlot>();
    HashMap<String, ChunkWorld> worlds = new HashMap<String, ChunkWorld>();

    protected HashMap<String, PlayerData> playerNameToPlayerDataMap = new HashMap<String, PlayerData>();

    protected final static String dataLayerFolderPath = "plugins" + File.separator + "ChunkClaim";

    void initialize() throws Exception {
        ChunkClaim.addLogEntry(this.chunks.size() + " total claimed chunks loaded.");

        Random rand = new Random();

        nextChunkId = (this.chunks.size() > 0 ? rand.nextInt(this.chunks.size()) : 0);

        Vector<String> playerNames = new Vector<String>();

        for (ChunkPlot chunk : chunks) {
            if (!playerNames.contains(chunk.getOwnerName())) {
                playerNames.add(chunk.getOwnerName());
            }
        }

        ChunkClaim.addLogEntry(playerNames.size() + " players have claimed chunks in loaded worlds.");

        System.gc();
    }

    abstract void loadWorldData(String worldName) throws Exception;

    public void cleanUp(int n) {
        if (this.chunks.size() < 1) return;

        Date now = new Date();
        double deletionTime = (1000 * 60 * 60 * 24) * ChunkClaim.plugin.config_autoDeleteDays;
        int r = 0;
        for (int i = 0; i < n; i++) {
            nextChunkId++;

            if (nextChunkId >= this.chunks.size()) nextChunkId = 0;

            ChunkPlot chunk = chunks.get(nextChunkId);
            if (chunk.getModifiedBlocks() >= 0) {
                long diff = now.getTime() - chunk.getClaimDate().getTime();
                if (diff > deletionTime) {
                    PlayerData playerData = this.getPlayerData(chunk.getOwnerName());
                    playerData.credits++;
                    this.savePlayerData(chunk.getOwnerName(), playerData);
                    this.playerNameToPlayerDataMap.remove(chunk.getOwnerName());
                    ChunkClaim.addLogEntry("Auto-deleted chunk by " + chunk.getOwnerName() + " at " + (chunk.getChunk().getX() * 16) + " | " + (chunk.getChunk().getZ() * 16));
                    this.deleteChunk(chunk);
                    r++;
                }
            }
            if (r > 50) {
                break;
            }
        }

    }

    synchronized void unloadWorldData(String worldName) {
        this.worlds.remove(worldName);
        for (int i = 0; i < this.chunks.size(); i++) {
            while (this.chunks.get(i).getChunk().getWorld().getName().equals(worldName)) {
                this.chunks.remove(i);
            }
        }
    }


    synchronized void clearCachedPlayerData(String playerName) {
        this.playerNameToPlayerDataMap.remove(playerName);
    }

    synchronized void addChunk(ChunkPlot newChunk) {
        this.chunks.add(newChunk);

        if (this.worlds.containsKey(newChunk.getChunk().getWorld().getName())) {
            this.worlds.get(newChunk.getChunk().getWorld().getName()).addChunk(newChunk);
            newChunk.setInDataStore(true);
            this.saveChunk(newChunk);
        }
    }

    private void saveChunk(ChunkPlot chunk) {
        this.writeChunkToStorage(chunk);

    }

    abstract void writeChunkToStorage(ChunkPlot chunk);

    synchronized public PlayerData getPlayerData(String playerName) {

        PlayerData playerData = this.playerNameToPlayerDataMap.get(playerName);
        if (playerData == null) {
            playerData = this.getPlayerDataFromStorage(playerName);
            this.playerNameToPlayerDataMap.put(playerName, playerData);
        }

        return this.playerNameToPlayerDataMap.get(playerName);
    }

    abstract PlayerData getPlayerDataFromStorage(String playerName);

    synchronized public void deleteChunk(ChunkPlot chunk) {

        for (ChunkPlot chunkPlot : this.chunks) {
            if (chunkPlot.getChunk() == chunk.getChunk()) {
                this.chunks.remove(chunkPlot);
                this.worlds.get(chunk.getChunk().getWorld().getName()).removeChunk(chunk);
                chunk.setInDataStore(false);
                break;
            }
        }
        this.deleteChunkFromSecondaryStorage(chunk);

        ChunkClaim.plugin.regenerateChunk(chunk);
    }

    abstract void deleteChunkFromSecondaryStorage(ChunkPlot chunk);

    synchronized public ChunkPlot getChunkAt(Location location, ChunkPlot cachedChunk) {
        if (cachedChunk != null && cachedChunk.isInDataStore() && cachedChunk.contains(location)) return cachedChunk;

        if (!worlds.containsKey(location.getWorld().getName())) return null;

        int x = location.getChunk().getX();
        int z = location.getChunk().getZ();

        return worlds.get(location.getWorld().getName()).getChunk(x, z);
    }

    synchronized public ChunkPlot getChunkAtPos(int x, int z, String worldName) {

        if (!worlds.containsKey(worldName)) return null;

        return worlds.get(worldName).getChunk(x, z);
    }

    public abstract void savePlayerData(String playerName, PlayerData playerData);

    synchronized public ArrayList<ChunkPlot> getAllChunksForPlayer(String playerName) {
        ArrayList<ChunkPlot> playerChunks = new ArrayList<ChunkPlot>();
        for (ChunkPlot chunk : chunks) {
            if (chunk.getOwnerName().equals(playerName)) {
                playerChunks.add(chunk);
            }
        }
        return playerChunks;
    }

    synchronized public int deleteChunksForPlayer(String playerName) {
        ArrayList<ChunkPlot> playerChunks = getAllChunksForPlayer(playerName);
        PlayerData playerData = this.getPlayerData(playerName);
        for (ChunkPlot playerChunk : playerChunks) {
            this.deleteChunk(playerChunk);
            playerData.credits++;
        }
        return playerChunks.size();
    }

    abstract void close();

    public boolean ownsNear(Location location, String playerName) {
        int x = location.getChunk().getX();
        int z = location.getChunk().getZ();
        String worldName = location.getWorld().getName();

        ChunkPlot a = getChunkAtPos(x - 1, z, worldName);
        ChunkPlot c = getChunkAtPos(x + 1, z, worldName);
        ChunkPlot b = getChunkAtPos(x, z - 1, worldName);
        ChunkPlot d = getChunkAtPos(x, z + 1, worldName);

        if (a != null && a.isTrusted(playerName)) {
            return true;
        } else if (b != null && b.isTrusted(playerName)) {
            return true;
        } else if (c != null && c.isTrusted(playerName)) {
            return true;
        } else if (d != null && d.isTrusted(playerName)) {
            return true;
        }
        return false;
    }
}
