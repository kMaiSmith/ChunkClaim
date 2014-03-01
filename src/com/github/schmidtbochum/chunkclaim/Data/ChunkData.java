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

import com.github.schmidtbochum.chunkclaim.ChunkClaim;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ChunkData implements IData {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

    final static String chunkDataFolderPath = dataLayerFolderPath + File.separator + "ChunkData";

    private File chunkFile;
    private String ownerName;
    private int modifiedBlocks;
    private ArrayList<String> builderNames;
    private Date claimDate;
    private int chunkX;
    private int chunkZ;
    private String chunkWorld;

    ChunkData(File chunkDataFile) {
        this.chunkFile = chunkDataFile;
    }

    ChunkData(File chunkDataFile, ChunkData chunkData) {
        this(chunkDataFile);
        this.ownerName = chunkData.ownerName;
        this.modifiedBlocks = chunkData.modifiedBlocks;
        this.builderNames = chunkData.builderNames;
        this.claimDate = chunkData.claimDate;
        this.chunkX = chunkData.getChunkX();
        this.chunkZ = chunkData.getChunkZ();
        this.chunkWorld = chunkData.getChunkWorld();
    }

    public ChunkData(Chunk chunk) {
        this.chunkX = chunk.getX();
        this.chunkZ = chunk.getZ();
        this.chunkWorld = chunk.getWorld().getName();
        this.builderNames = new ArrayList<String>();
        this.claimDate = new Date();
    }

    public ChunkData(Chunk chunk, String owner, ArrayList<String> builders) {
        this(chunk);
        this.ownerName = owner;
        for (String builder : builders) {
            this.builderNames.add(builder);
        }
    }

    public boolean contains(Location location) {
        return this.chunkX == location.getChunk().getX() &&
                this.chunkZ == location.getChunk().getZ() &&
                this.chunkWorld.equals(location.getWorld());
    }

    public boolean isTrusted(String playerName) {
        return this.builderNames.contains(playerName) || this.ownerName.equals(playerName);
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public String getChunkWorld() {
        return chunkWorld;
    }

    public int getModifiedBlocks() {
        return modifiedBlocks;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public ArrayList<String> getBuilderNames() {
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

    public void writeDataToFile(BufferedWriter outStream) throws IOException {
        //1. Line: Owner Name
        outStream.write(this.getOwnerName());
        outStream.newLine();

        //2. Line: ChunkData Creation timestamp
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        outStream.write(dateFormat.format(claimDate));
        outStream.newLine();

        //3. Line: Number of modified blocks
        outStream.write(String.valueOf(modifiedBlocks));
        outStream.newLine();

        //4. Line: List of Builders
        for (String builder : builderNames) {
            outStream.write(builder + ";");
        }
        outStream.newLine();

        outStream.write(String.valueOf(chunkX));
        outStream.newLine();
        outStream.write(String.valueOf(chunkZ));
        outStream.newLine();
        outStream.write(chunkWorld);
        outStream.newLine();

        //filled line to prevent null
        outStream.write("==========");
        outStream.newLine();
    }

    public void readDataFromFile(BufferedReader inStream) throws IOException {
        //1. Line: Owner Name
        this.ownerName = inStream.readLine();

        //2. Line: ChunkData Creation timestamp
        String claimDateString = inStream.readLine();

        //3. Line: Number of modified blocks
        this.modifiedBlocks = Integer.parseInt(inStream.readLine());

        //4. Line: List of Builders
        this.builderNames = new ArrayList<String>(Arrays.asList(inStream.readLine().split(";")));

        int chunkX = Integer.parseInt(inStream.readLine());
        int chunkZ = Integer.parseInt(inStream.readLine());
        String world = inStream.readLine();

        inStream.close();

        try {
            this.claimDate = dateFormat.parse(claimDateString);
        } catch (ParseException e) {
            ChunkClaim.addLogEntry("Failed to parse Claim Date for chunk file " + this.getFile().getName() + "... falling back to today's date");
            this.claimDate = new Date();
        }

        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.chunkWorld = world;
    }

    public File getFile() {
        return chunkFile;
    }
}
