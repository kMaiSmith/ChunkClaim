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

package com.github.schmidtbochum.chunkclaim.Data;

import com.google.common.collect.HashBasedTable;

import java.util.ArrayList;

class WorldData {
    private HashBasedTable<Integer, Integer, ChunkData> chunkTable = HashBasedTable.create();

    public ChunkData getChunk(int x, int z) {
        return chunkTable.get(x, z);
    }

    public void addChunk(ChunkData newChunk) {
        chunkTable.put(newChunk.getChunk().getX(), newChunk.getChunk().getZ(), newChunk);
    }

    public void removeChunk(ChunkData chunk) {
        chunkTable.remove(chunk.getChunk().getX(), chunk.getChunk().getZ());
    }

    public ArrayList<ChunkData> getAllChunks() {
        return new ArrayList<ChunkData>(chunkTable.values());
    }
}