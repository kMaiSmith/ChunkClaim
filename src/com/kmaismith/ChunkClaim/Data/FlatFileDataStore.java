/*
    ChunkClaim Plugin for Minecraft Bukkit Servers
    Copyright (C) 2012 Felix Schmidt
    Copyright (C) 2014 Kyle Smith
    
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

package com.kmaismith.chunkclaim.Data;

import com.kmaismith.chunkclaim.ChunkClaimLogger;

import java.io.*;


class FlatFileDataStore implements IDataStore {

    private final ChunkClaimLogger logger;

    FlatFileDataStore(ChunkClaimLogger logger) {

        this.logger = logger;
        //ensure data folders exist
        //if (new File(playerDataFolderPath).mkdirs()) {
        //    ChunkClaim.addLogEntry("Created Player Data Folder Directory");
        //}
        //if (new File(worldDataFolderPath).mkdirs()) {
        //    ChunkClaim.addLogEntry("Created World Data Folder Directory");
        //}
    }

    private void closeFile(Closeable stream, String filename) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException exception) {
            logger.addLogEntry("Failed to close stream for file " + filename);
        }
    }

    public boolean loadDataFromFile(IData data) {

        BufferedWriter outStream = null;

        try {
            File dataFile = data.getFile();

            BufferedReader inStream = new BufferedReader(new FileReader(dataFile));

            data.readDataFromFile(inStream);

            closeFile(outStream, dataFile.getName());

        } catch (IOException e) {
            logger.addLogEntry("IO Exception saving data to " + data.getFile().getName() + "... " + e.getMessage());
            return false;
        }
        return true;
    }

    public synchronized void writeDataToFile(IData data) {

        File dataFile = data.getFile();

        BufferedWriter outStream = null;

        try {
            if (dataFile.createNewFile()) {
                logger.addLogEntry("Created Data File " + dataFile.getName());
            }
            outStream = new BufferedWriter(new FileWriter(dataFile));

            data.writeDataToFile(outStream);

        } catch (IOException e) {
            logger.addLogEntry("IO Exception reading data from " + dataFile.getName() + "... " + e.getMessage());
        }

        closeFile(outStream, dataFile.getName());
    }

    public void deleteData(IData data) {
        // remove from disk
        File dataFile = data.getFile();

        if (dataFile.exists() && !dataFile.delete()) {
            logger.addLogEntry("Error: Unable to delete data file \"" + dataFile.getName() + "\"");
        }
    }

}
