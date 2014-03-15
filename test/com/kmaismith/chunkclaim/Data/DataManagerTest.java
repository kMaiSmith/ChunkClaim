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

import com.kmaismith.chunkclaim.ChunkClaim;
import com.kmaismith.chunkclaim.ChunkClaimLogger;
import com.kmaismith.chunkclaim.DataHelper;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

public class DataManagerTest {

    private DataManager systemUnderTest;
    private ChunkClaimLogger logger;

    private DataHelper helpers;

    @Before
    public void setup()
    {
        ChunkClaim.plugin = new ChunkClaim();
        ChunkClaim.plugin.config_startCredits = 10;

        helpers = new DataHelper();

        logger = mock(ChunkClaimLogger.class);
        systemUnderTest = new DataManager(logger);
    }

    @Test
    public void testGetChunkReturnsAppropriateChunk()
    {
        Location location = helpers.newLocation("world", 123, 321);

        ChunkData chunkIWant = helpers.newChunkData("player", new ArrayList<String>(), location);

        systemUnderTest.addChunk(chunkIWant);

        Assert.assertNotNull(systemUnderTest.getChunkAt(location));
    }

    @Test
    public void testDataManagerFindsAllRegisteredChunksWhenInitialized() {
        Location location = helpers.newLocation("world", 123, 321);

        ChunkData chunkIWant = helpers.newChunkData("player", new ArrayList<String>(), location);

        systemUnderTest.addChunk(chunkIWant);

        systemUnderTest = new DataManager(logger);

        Assert.assertNotNull(systemUnderTest.getChunkAt(location));
    }

    @Test
    public void testGivenAPlayerNameGetAllChunksForPlayerWillGetAllChunksForPlayer() {
        Location location1 = helpers.newLocation("world", 123, 321);
        Location location2 = helpers.newLocation("world", 321, 123);
        Location location3 = helpers.newLocation("world", 222, 123);

        ChunkData chunk1 = helpers.newChunkData("player", new ArrayList<String>(), location1);
        ChunkData chunk2 = helpers.newChunkData("player", new ArrayList<String>(), location2);
        ChunkData chunk3 = helpers.newChunkData("player2", new ArrayList<String>(), location3);

        systemUnderTest.addChunk(chunk1);
        systemUnderTest.addChunk(chunk2);
        systemUnderTest.addChunk(chunk3);

        List<ChunkData> chunks = systemUnderTest.getChunksForPlayer("player");

        Assert.assertEquals(chunks.size(), 2);
        for(ChunkData chunk : chunks) {
            Assert.assertEquals(chunk.getOwnerName(), "player");
        }
    }

    @Test
    public void testReadPlayerDataWillReadPlayerDataForPlayer() {
        // Sample player file stolen from a player
        String playerFileContents =
                "2014.02.16.18.49.49\n" +
                "2014.03.13.02.25.21\n" +
                "5\n" +
                "\n" +
                "==========";

        try {
            PrintWriter playerFile = new PrintWriter(PlayerData.playerDataFolderPath + File.separator + "playerA.dat");
            playerFile.print(playerFileContents);
            playerFile.close();
        } catch(FileNotFoundException e) {
            assert false;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        Date loginDate = new Date();

        try {
            loginDate = dateFormat.parse("2014.02.16.18.49.49");
        } catch (ParseException parseException) {
            assert false;
        }

        PlayerData player = systemUnderTest.readPlayerData("playerA");
        systemUnderTest.savePlayerData(player);
        systemUnderTest.clearCachedPlayerData("playerA");

        Assert.assertEquals(player.getFirstJoin(), loginDate);
    }

    @Test
    public void testReadPlayerDataWillCreatePlayerDataWhenNoPlayerDataIsPresent() {
        File playerFile = new File(PlayerData.playerDataFolderPath + File.separator + "playerA.dat");

        Assert.assertFalse(playerFile.exists());

        PlayerData player = systemUnderTest.readPlayerData("playerA");
        systemUnderTest.savePlayerData(player);

        // There are already tests covering the contents being written correctly
        Assert.assertTrue(playerFile.exists());
    }

    @Test
    public void testDeleteChunksForPlayerDeletesAllChunksForAGivenPlayer() {
        Location location1 = helpers.newLocation("world", 123, 321);
        Location location2 = helpers.newLocation("world", 321, 123);
        Location location3 = helpers.newLocation("world", 222, 123);

        ChunkData chunk1 = helpers.newChunkData("player", new ArrayList<String>(), location1);
        ChunkData chunk2 = helpers.newChunkData("player", new ArrayList<String>(), location2);
        ChunkData chunk3 = helpers.newChunkData("player2", new ArrayList<String>(), location3);

        systemUnderTest.addChunk(chunk1);
        systemUnderTest.addChunk(chunk2);
        systemUnderTest.addChunk(chunk3);

        int previouslyOwnedChunks = systemUnderTest.deleteChunksForPlayer("player");

        Assert.assertEquals(previouslyOwnedChunks, 2);

        Assert.assertEquals(systemUnderTest.getChunksForPlayer("player"), new ArrayList<ChunkData>());
    }

    @Test
    public void testDeleteChunkWillDeleteAChunkFromList()
    {
        Location location1 = helpers.newLocation("world", 123, 321);
        ChunkData chunk1 = helpers.newChunkData("player", new ArrayList<String>(), location1);
        chunk1 = systemUnderTest.addChunk(chunk1);
        systemUnderTest.writeChunkToStorage(chunk1);

        Assert.assertNotNull(systemUnderTest.getChunkAt(location1));
        systemUnderTest.deleteChunk(chunk1);
        Assert.assertNull(systemUnderTest.getChunkAt(location1));
    }

    @Test
    public void testDeleteChunkWillDeleteChunkFromStorage()
    {
        Location location1 = helpers.newLocation("world", 123, 321);
        ChunkData chunk1 = helpers.newChunkData("player", new ArrayList<String>(), location1);
        chunk1 = systemUnderTest.addChunk(chunk1);
        systemUnderTest.writeChunkToStorage(chunk1);

        Assert.assertTrue(chunk1.getFile().exists());
        systemUnderTest.deleteChunk(chunk1);
        Assert.assertFalse(chunk1.getFile().exists());
    }

    @Test
    public void testAddChunkWillAddAChunkToTheCollection()
    {
        Location location1 = helpers.newLocation("world", 123, 321);
        ChunkData chunk1 = helpers.newChunkData("player", new ArrayList<String>(), location1);
        Assert.assertNull(systemUnderTest.getChunkAt(location1));
        systemUnderTest.addChunk(chunk1);
        Assert.assertNotNull(systemUnderTest.getChunkAt(location1));
    }

    @Test
    public void testAddChunkWillAddChunkToDataStore()
    {
        Location location1 = helpers.newLocation("world", 123, 321);
        ChunkData chunk1 = helpers.newChunkData("player", new ArrayList<String>(), location1);
        Assert.assertNull(chunk1.getFile());
        chunk1 = systemUnderTest.addChunk(chunk1);
        Assert.assertTrue(chunk1.getFile().exists());

    }

    @Test
    public void testWriteChunkToStorageWritesChunkValuesToDataStore()
    {
        Location location1 = helpers.newLocation("world", 123, 321);
        ChunkData chunk1 = helpers.newChunkData("player", new ArrayList<String>(), location1);
        chunk1 = systemUnderTest.addChunk(chunk1);
        chunk1.addBuilder("personB");
        systemUnderTest.writeChunkToStorage(chunk1);

        systemUnderTest = new DataManager(logger);
        ChunkData chunk2 = systemUnderTest.getChunkAt(location1);
        Assert.assertTrue(chunk2.isTrusted("personB"));
    }

    @After
    public void cleanup()
    {
        try {
            FileUtils.deleteDirectory(new File("plugins"));
        } catch (IOException e) {
            assert false;
        }

        ChunkClaim.plugin = null;
    }
}
