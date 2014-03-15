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
import com.kmaismith.chunkclaim.DataHelper;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class DataManagerTest {

    private DataManager systemUnderTest;
    private ChunkClaimLogger logger;

    private DataHelper helpers;

    @Before
    public void setup()
    {
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

    }

    @Test
    public void testReadPlayerDataWillCreatePlayerDataWhenNoPlayerDataIsPresent() {

    }

    @Test
    public void testDeleteChunksForPlayerDeletesAllChunksForAGivenPlayer() {

    }

    @Test
    public void testDeleteChunkWillDeleteAChunkFromList()
    {

    }

    @Test
    public void testDeleteChunkWillDeleteChunkFromStorage()
    {

    }

    @Test
    public void testAddChunkWillAddAChunkToTheCollection()
    {

    }

    @Test
    public void testAddChunkWillAddChunkToDataStore()
    {

    }

    @Test
    public void testSavePlayerDataWillWriteChangesToPlayerData()
    {

    }

    @Test
    public void testWriteChunkToStorageWritesChunkValuesToDataStore()
    {

    }

    @After
    public void cleanup()
    {
        try {
        FileUtils.deleteDirectory(new File("plugins"));
        } catch (IOException e) {
            assert false;
        }
    }
}
