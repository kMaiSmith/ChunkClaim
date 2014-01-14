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

import com.google.common.collect.HashBasedTable;

class ChunkWorld {
    public HashBasedTable<Integer, Integer, ChunkPlot> chunkTable = HashBasedTable.create();

    public ChunkPlot getChunk(int x, int z) {
        return chunkTable.get(x, z);
    }

    public void addChunk(ChunkPlot newChunk) {
        chunkTable.put(newChunk.getChunk().getX(), newChunk.getChunk().getZ(), newChunk);
    }

    public void removeChunk(ChunkPlot chunk) {
        chunkTable.remove(chunk.getChunk().getX(), chunk.getChunk().getZ());
    }
}
