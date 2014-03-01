package com.kmaismith.ChunkClaim.Data;

import com.github.schmidtbochum.chunkclaim.Data.ChunkData;
import com.github.schmidtbochum.chunkclaim.Data.DataManager;
import junit.framework.Assert;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: kyle
 * Date: 2/28/14
 * Time: 8:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataManagerTest {

    @Test
    public void testGetChunkReturnsAppropriateChunk() {
        DataManager systemUnderTest = new DataManager();

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
