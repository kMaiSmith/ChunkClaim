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

import java.util.ArrayList;
import java.util.HashMap;

interface IDataStore {

    // part of data store functionality
    abstract void loadWorldData(String worldName);

    abstract void unloadWorldData(String worldName);

    abstract void writeChunkToStorage(ChunkPlot chunk);

    abstract void addChunk(ChunkPlot newChunk);

    abstract void deleteChunk(ChunkPlot chunk);

    abstract PlayerData getPlayerData(String playerName);

    abstract void savePlayerData(String playerName, PlayerData playerData);

    abstract void clearCachedPlayerData(String playerName);

    abstract ArrayList<ChunkPlot> getAllChunksForPlayer(String playerName);

    abstract int deleteChunksForPlayer(String playerName);

    // @TODO: ownsNear does not belong here
    abstract boolean ownsNear(Location location, String playerName);

    // @TODO: the getters are not part of data storage and needs to be put where the chunks and worlds are stored
    abstract ChunkPlot getChunkAt(Location location, ChunkPlot cachedChunk);

    abstract ChunkPlot getChunkAt(int x, int z, String worldName);

    abstract int getNextChunkId();

    abstract ArrayList<ChunkPlot> getAllChunks();

    abstract HashMap<String, ChunkWorld> getAllWorlds();

    // @TODO: Figure out where to put cleanUp()
    abstract void cleanUp();
}
