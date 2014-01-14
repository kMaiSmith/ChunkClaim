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

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class ChunkPlot {
    private String ownerName;
    private int modifiedBlocks;
    private HashSet<String> builderNames;
    private Date claimDate;
    private boolean inDataStore;
    private Chunk chunk;
    private boolean marked;
    private boolean inspected;

    ChunkPlot(Chunk chunk) {
        this.chunk = chunk;
        this.builderNames = new HashSet<String>();
        this.marked = false;
        this.inspected = false;
        this.claimDate = new Date();
    }

    ChunkPlot(Chunk chunk, String owner, ArrayList<String> builders) {
        this(chunk);
        this.ownerName = owner;
        for (String builder : builders) {
            this.builderNames.add(builder);
        }
    }

    ChunkPlot(Chunk chunk, String owner, ArrayList<String> builders, Date claimDate) {
        this(chunk, owner, builders);
        this.claimDate = claimDate;
    }

    public boolean contains(Location location) {
        return this.chunk == location.getChunk();
    }

    public void modify() {
        if (this.modifiedBlocks > 0) {
            this.modifiedBlocks++;
            if (this.modifiedBlocks >= ChunkClaim.plugin.config_minModBlocks) {
                this.modifiedBlocks = -1;
            }
            ChunkClaim.plugin.dataStore.writeChunkToStorage(this);
        }
    }

    public void mark() {
        this.modifiedBlocks = 0;
        this.marked = true;
        ChunkClaim.plugin.dataStore.writeChunkToStorage(this);
    }

    public void unmark() {
        this.modifiedBlocks = -1;
        this.marked = false;
        ChunkClaim.plugin.dataStore.writeChunkToStorage(this);
    }

    public boolean isTrusted(String playerName) {
        return this.builderNames.contains(playerName) || this.ownerName.equals(playerName) || ChunkClaim.plugin.dataStore.getPlayerData(playerName).ignoreChunks;
    }

    public Chunk getChunk() {
        return this.chunk;
    }

    public boolean isInspected() {
        return inspected;
    }

    public void setInspected() {
        this.inspected = true;
    }

    public boolean isMarked() {
        return marked;
    }

    public int getModifiedBlocks() {
        return modifiedBlocks;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public HashSet<String> getBuilderNames() {
        return this.builderNames;
    }

    public void addBuilder(String name) {
        this.builderNames.add(name);
    }

    public void removeBuilder(String name) {
        if (this.isTrusted(name)) {
            this.builderNames.remove(name);
        }
    }

    public Date getClaimDate() {
        return this.claimDate;
    }

    public void setInDataStore(boolean isInDataStore) {
        this.inDataStore = isInDataStore;
    }

    public boolean isInDataStore() {
        return inDataStore;
    }

    public void setModifiedBlocks(int modifiedBlocks) {
        this.modifiedBlocks = modifiedBlocks;
    }
}
