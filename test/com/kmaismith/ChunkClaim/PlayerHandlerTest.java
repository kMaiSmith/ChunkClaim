package com.kmaismith.ChunkClaim;

import com.github.schmidtbochum.chunkclaim.Data.ChunkData;
import com.github.schmidtbochum.chunkclaim.Data.DataManager;
import com.github.schmidtbochum.chunkclaim.PlayerEventHandler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class PlayerHandlerTest {

    private PlayerEventHandler systemUnderTest;
    private DataManager mockDataStore;
    private Block mockBlock;
    private Player mockPlayer;
    private Location mockLocation;
    private PlayerInteractEvent event;
    private ChunkData mockChunk;

    @Before
    public void setupTestCase() {
        mockDataStore = mock(DataManager.class);
        systemUnderTest = new PlayerEventHandler(mockDataStore);

        mockPlayer = mock(Player.class);
        mockBlock = mock(Block.class);
        mockLocation = mock(Location.class);
        event = new PlayerInteractEvent(mockPlayer, null, null, mockBlock, null);
        when(mockBlock.getLocation()).thenReturn(mockLocation);
        mockChunk = mock(ChunkData.class);
        when(mockDataStore.getChunkAt(mockLocation)).thenReturn(mockChunk);
    }

    @Test
    public void testPlayersCannotInteractWithBlocksThatAreClaimedBySomeoneElse() {
        when(mockChunk.isTrusted("NotYou")).thenReturn(false);
        when(mockDataStore.getChunkAt(mockLocation)).thenReturn(mockChunk);
        when(mockChunk.getOwnerName()).thenReturn("NotYou");

        systemUnderTest.onPlayerInteract(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou don't have NotYou's permission to build here.");
    }

    @Test
    public void testOwnerOfChunkCanInteractWithBlocksThatAreInTheirClaim() {
        when(mockChunk.isTrusted("You")).thenReturn(true);
        when(mockDataStore.getChunkAt(mockLocation)).thenReturn(mockChunk);
        when(mockPlayer.getName()).thenReturn("You");

        systemUnderTest.onPlayerInteract(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testAdminCanInteractWithNonOwnedChunk() {
        when(mockChunk.isTrusted("Admin")).thenReturn(false);
        when(mockDataStore.getChunkAt(mockLocation)).thenReturn(mockChunk);
        when(mockPlayer.getName()).thenReturn("Admin");
        when(mockPlayer.hasPermission("chunkclaim.admin")).thenReturn(true);

        systemUnderTest.onPlayerInteract(event);
        Assert.assertFalse(event.isCancelled());
    }

}
