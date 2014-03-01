package com.kmaismith.ChunkClaim;

import com.kmaismith.ChunkClaim.Data.ChunkData;
import com.kmaismith.ChunkClaim.Data.DataManager;
import junit.framework.Assert;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class BlockEventHandlerTest {

    @Test
    public void testUntrustedPlayerCannotPlaceBlocksInsideChunkClaims() {
        Block mockBlock = mock(Block.class);
        Player mockPlayer = mock(Player.class);
        ChunkData mockChunkData = mock(ChunkData.class);
        DataManager mockDataManager = mock(DataManager.class);
        Location mockLocation = mock(Location.class);
        BlockEventHandler systemUnderTest = new BlockEventHandler(mockDataManager);

        when(mockBlock.getLocation()).thenReturn(mockLocation);
        when(mockDataManager.getChunkAt(mockLocation)).thenReturn(mockChunkData);
        when(mockPlayer.getName()).thenReturn("APlayer");
        when(mockChunkData.isTrusted("APlayer")).thenReturn(false);
        when(mockChunkData.getOwnerName()).thenReturn("SuperOwner");

        BlockPlaceEvent event = new BlockPlaceEvent(mockBlock, null, null, null, mockPlayer, true);

        systemUnderTest.onBlockPlace(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou don't have SuperOwner's permission to build here.");


    }

}
