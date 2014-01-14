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

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class FlatFileDataStore extends DataStore {

    private final static String playerDataFolderPath = dataLayerFolderPath + File.separator + "PlayerData";
    private final static String worldDataFolderPath = dataLayerFolderPath + File.separator + "ChunkData";

    FlatFileDataStore() throws Exception {
        this.initialize();
    }

    @Override
    void initialize() throws Exception {

        //ensure data folders exist
        if (new File(playerDataFolderPath).mkdirs()) {
            ChunkClaim.addLogEntry("Created Player Data Folder Directory");
        }
        if (new File(worldDataFolderPath).mkdirs()) {
            ChunkClaim.addLogEntry("Created World Data Folder Directory");
        }

        //load worlds
        List<String> worldNameList = ChunkClaim.plugin.config_worlds;

        for (String worldName : worldNameList) {

            if (Bukkit.getServer().getWorld(worldName) == null) {
                continue;  //skips unloaded worlds
            }

            this.loadWorldData(worldName);
        }
        super.initialize();
    }

    @Override
    synchronized void loadWorldData(String worldName) {

        int minModifiedBlocks = 10;

        //create a new world object and register it
        ChunkWorld world = new ChunkWorld();
        this.worlds.put(worldName, world);


        //load chunks data into memory
        //get a list of all the chunks in the world folder
        String chunkDataFolderPath = worldDataFolderPath + File.separator + worldName;

        File chunkDataFolder = new File(chunkDataFolderPath);

        //ensure data folder exist
        if (chunkDataFolder.mkdirs()) {
            ChunkClaim.addLogEntry("Created Chunk Data Folder");
        }

        File[] files = chunkDataFolder.listFiles();

        for (File file : files) {
            //avoids folders
            if (!file.isFile()) {
                continue;
            }
            String fileName = file.getName();

            //get the chunk coordinates from the file name
            String[] chunkIdSep = fileName.split(";");

            //skip the file if no separator was found
            if (fileName.equals(chunkIdSep[0])) continue;

            int x;
            int z;

            try {
                x = Integer.parseInt(chunkIdSep[0]);
                z = Integer.parseInt(chunkIdSep[1]);
            } catch (Exception e) {
                continue; //skip the file
            }

            BufferedReader inStream = null;

            try {
                inStream = new BufferedReader(new FileReader(file.getAbsolutePath()));

                //1. Line: Owner Name
                String ownerName = inStream.readLine();

                //2. Line: ChunkPlot Creation timestamp
                String claimDateString = inStream.readLine();

                //3. Line: Number of modified blocks
                int modifiedBlocks = Integer.parseInt(inStream.readLine());

                //4. Line: List of Builders
                ArrayList<String> builderNames = new ArrayList<String>(Arrays.asList(inStream.readLine().split(";")));

                inStream.close();

                DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                Date claimDate = dateFormat.parse(claimDateString);

                Chunk chunkClaimChunk = Bukkit.getWorld(worldName).getChunkAt(x, z);

                ChunkPlot chunk = new ChunkPlot(chunkClaimChunk, ownerName, builderNames, claimDate);
                chunk.setModifiedBlocks(modifiedBlocks);
                this.chunks.add(chunk);

                if (modifiedBlocks < minModifiedBlocks) {
                    this.unusedChunks.add(chunk);
                }
                this.worlds.get(chunk.getChunk().getWorld().getName()).addChunk(chunk);
                chunk.setInDataStore(true);
            } catch (Exception e) {
                ChunkClaim.addLogEntry("Unable to load data for chunk \"" + worldName + "\\" + fileName + "\": " + e.getMessage());
            }

            //close the file
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException exception) {
                ChunkClaim.addLogEntry("Failed to close instream of file " + fileName);
            }
        }
    }

    @Override
    synchronized void writeChunkToStorage(ChunkPlot chunk) {

        String fileName = chunk.getChunk().getX() + ";" + chunk.getChunk().getZ();

        String worldName = chunk.getChunk().getWorld().getName();
        String chunkDataFolderPath = worldDataFolderPath + File.separator + worldName;

        //ensure that the world folder exists
        if (new File(chunkDataFolderPath).mkdirs()) {
            ChunkClaim.addLogEntry("Created Chunk Data Folder");
        }

        BufferedWriter outStream = null;

        try {
            //open the chunks's file
            File chunkFile = new File(chunkDataFolderPath + File.separator + fileName);
            if (chunkFile.createNewFile()) {
                ChunkClaim.addLogEntry("Created Chunk File " + fileName);
            }
            outStream = new BufferedWriter(new FileWriter(chunkFile));

            //write chunk to the file
            this.writeChunkData(chunk, outStream);

            //update date
        } catch (Exception e) {
            ChunkClaim.addLogEntry("Unexpected exception saving data for chunk \"" + worldName + "\\" + fileName + "\": " + e.getMessage());
        }

        //close the file
        try {
            if (outStream != null) outStream.close();
        } catch (IOException exception) {
            ChunkClaim.addLogEntry("Failed to close outstream for file " + fileName);
        }
    }

    synchronized private void writeChunkData(ChunkPlot chunk, BufferedWriter outStream) throws IOException {
        //1. Line: Owner Name
        outStream.write(chunk.getOwnerName());
        outStream.newLine();

        //2. Line: ChunkPlot Creation timestamp
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        outStream.write(dateFormat.format(chunk.getClaimDate()));
        outStream.newLine();

        //3. Line: Number of modified blocks
        outStream.write(String.valueOf(chunk.getModifiedBlocks()));
        outStream.newLine();

        //4. Line: List of Builders
        for (String builder : chunk.getBuilderNames()) {
            outStream.write(builder + ";");
        }
        outStream.newLine();

        //filled line to prevent null
        outStream.write("==========");
        outStream.newLine();
    }

    @Override
    void deleteChunkFromSecondaryStorage(ChunkPlot chunk) {
        String fileName = chunk.getChunk().getX() + ";" + chunk.getChunk().getZ();

        String worldName = chunk.getChunk().getWorld().getName();
        String chunkDataFolderPath = worldDataFolderPath + File.separator + worldName;

        //remove from disk
        File chunkFile = new File(chunkDataFolderPath + File.separator + fileName);

        if (chunkFile.exists() && !chunkFile.delete()) {
            ChunkClaim.addLogEntry("Error: Unable to delete chunk file \"" + worldName + "\\" + fileName + "\".");
        }

    }

    @Override
    synchronized PlayerData getPlayerDataFromStorage(String playerName) {
        File playerFile = new File(playerDataFolderPath + File.separator + playerName);

        PlayerData playerData = new PlayerData();
        playerData.playerName = playerName;

        //if it doesn't exist as a file
        if (!playerFile.exists()) {

            //create a file with defaults
            this.savePlayerData(playerName, playerData);
        }

        //otherwise, read the file
        else {
            BufferedReader inStream = null;
            try {

                inStream = new BufferedReader(new FileReader(playerFile.getAbsolutePath()));

                //first line is first join date
                String firstJoinTimestampString = inStream.readLine();

                //convert that to a date and store it
                DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                try {
                    playerData.firstJoin = dateFormat.parse(firstJoinTimestampString);
                } catch (ParseException parseException) {
                    ChunkClaim.addLogEntry("Unable to load first join date for \"" + playerFile.getName() + "\".");
                    playerData.firstJoin = null;
                }

                //first line is last login timestamp
                String lastLoginTimestampString = inStream.readLine();

                //convert that to a date and store it
                try {
                    playerData.lastLogin = dateFormat.parse(lastLoginTimestampString);
                } catch (ParseException parseException) {
                    ChunkClaim.addLogEntry("Unable to load last login for \"" + playerFile.getName() + "\".");
                    playerData.lastLogin = null;
                }
                //third line is credits
                String creditsString = inStream.readLine();
                playerData.credits = Float.parseFloat(creditsString);

                //fourth line is any bonus credits granted by administrators
                String bonusString = inStream.readLine();
                playerData.bonus = Float.parseFloat(bonusString);

                //5. line: list of builders
                String[] builders = inStream.readLine().split(";");

                for (String builder : builders) {
                    if (!builder.equals(""))
                        playerData.builderNames.add(builder);
                }

                inStream.close();

            } catch (Exception e) {

                ChunkClaim.addLogEntry("Unable to load data for player \"" + playerName + "\": " + e.getMessage());
            }

            //close the file
            try {
                if (inStream != null) inStream.close();
            } catch (IOException exception) {
                ChunkClaim.addLogEntry("Failed to close instream for player file for " + playerName);
            }
        }

        return playerData;
    }

    @Override
    synchronized public void savePlayerData(String playerName, PlayerData playerData) {

        BufferedWriter outStream = null;
        try {

            //open the player's file
            File playerDataFile = new File(playerDataFolderPath + File.separator + playerName);
            if (playerDataFile.createNewFile()) {
                ChunkClaim.addLogEntry("Created player file for " + playerName);
            }
            outStream = new BufferedWriter(new FileWriter(playerDataFile));

            //first line is first join date
            DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
            outStream.write(dateFormat.format(playerData.firstJoin));
            outStream.newLine();

            //second line is last login timestamp
            if (playerData.lastLogin == null) playerData.lastLogin = new Date();
            outStream.write(dateFormat.format(playerData.lastLogin));
            outStream.newLine();

            //third line is credits
            outStream.write(String.valueOf(playerData.credits));
            outStream.newLine();

            //fourth line is bonus
            outStream.write(String.valueOf(playerData.bonus));
            outStream.newLine();

            //fifth line are trusted builders (trusted on all chunks);
            for (int i = 0; i < playerData.builderNames.size(); i++) {

                outStream.write(playerData.builderNames.get(i) + ";");
            }
            outStream.newLine();

            //filled line to prevent null
            outStream.write("==========");
            outStream.newLine();

        } catch (Exception e) {

            ChunkClaim.addLogEntry("Unexpected exception saving data for player \"" + playerName + "\": " + e.getMessage());
        }

        //close the file
        try {
            if (outStream != null) outStream.close();
        } catch (IOException exception) {
            ChunkClaim.addLogEntry("Failed to close player file for " + playerName);
        }
    }
}
