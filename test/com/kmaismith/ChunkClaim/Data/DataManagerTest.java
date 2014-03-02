package com.kmaismith.ChunkClaim.Data;

import com.kmaismith.ChunkClaim.ChunkClaimLogger;
import junit.framework.Assert;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class DataManagerTest {

    @Test
    public void testGetChunkReturnsAppropriateChunk() {
        ChunkClaimLogger logger = mock(ChunkClaimLogger.class);
        DataManager systemUnderTest = new DataManager(logger);

        World world = mock(World.class);
        Chunk chunk = mock(Chunk.class);

        when(chunk.getX()).thenReturn(123);
        when(chunk.getZ()).thenReturn(321);
        when(chunk.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("world");

        Location location = spy(new Location(world, 123 * 16 + 5, 64, 321 * 16 + 5));
        when(location.getChunk()).thenReturn(chunk);

        ChunkData chunkIWant = new ChunkData(chunk);

        systemUnderTest.addChunk(chunkIWant);

        Assert.assertNotNull(systemUnderTest.getChunkAt(location));
    }
}
