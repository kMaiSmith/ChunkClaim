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

import static org.mockito.Mockito.*;

public class DataManagerTest {

    private DataManager systemUnderTest;
    private ChunkClaimLogger logger;

    @Before
    public void setup()
    {
        logger = mock(ChunkClaimLogger.class);
        systemUnderTest = new DataManager(logger);
    }

    @Test
    public void testGetChunkReturnsAppropriateChunk()
    {

        World world = mock(World.class);
        Chunk chunk = mock(Chunk.class);

        when(chunk.getX()).thenReturn(123);
        when(chunk.getZ()).thenReturn(321);
        when(chunk.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("world");

        Location location = spy(new Location(world, 123 * 16 + 5, 64, 321 * 16 + 5));
        when(location.getChunk()).thenReturn(chunk);

        ChunkData chunkIWant = new ChunkData(chunk, "someone", new ArrayList<String>());

        systemUnderTest.addChunk(chunkIWant);

        Assert.assertNotNull(systemUnderTest.getChunkAt(location));
    }

    @Test
    public void testDataManagerFindsAllRegisteredChunksWhenInitialized() {
        World world = mock(World.class);
        Chunk chunk = mock(Chunk.class);

        when(chunk.getX()).thenReturn(123);
        when(chunk.getZ()).thenReturn(321);
        when(chunk.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("world");

        Location location = spy(new Location(world, 123 * 16 + 5, 64, 321 * 16 + 5));
        when(location.getChunk()).thenReturn(chunk);

        ChunkData chunkIWant = new ChunkData(chunk, "someone", new ArrayList<String>());

        systemUnderTest.addChunk(chunkIWant);

        systemUnderTest = new DataManager(logger);

        Assert.assertNotNull(systemUnderTest.getChunkAt(location));
    }

    @Test
    public void testGetAllChunksReturnsAllChunksInCollection() {

    }

    @Test
    public void testGivenAPlayerNameGetAllChunksForPlayerWillGetAllChunksForPlayer() {

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
