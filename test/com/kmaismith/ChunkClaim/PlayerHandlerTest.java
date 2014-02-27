package com.kmaismith.ChunkClaim;

import com.github.schmidtbochum.chunkclaim.Data.ChunkData;
import com.github.schmidtbochum.chunkclaim.Data.DataManager;
import com.github.schmidtbochum.chunkclaim.PlayerEventHandler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: kyle
 * Date: 2/26/14
 * Time: 7:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerHandlerTest {

    @Test
    public void testPlayersCannotInteractWithBlocksThatAreClaimedBySomeoneElse() {
        DataManager mockDataStore = mock(DataManager.class);
        PlayerEventHandler systemUnderTest = new PlayerEventHandler(mockDataStore);

        Player mockPlayer = mock(Player.class);
        Block mockBlock = mock(Block.class);
        //World mockWorld = mock(World.class);
        Location location = mock(Location.class);//new Location(mockWorld, 160, 64, 160);
        PlayerInteractEvent event = new PlayerInteractEvent(mockPlayer, null, null, mockBlock, null);
        when(mockBlock.getLocation()).thenReturn(location);
        ChunkData mockChunk = mock(ChunkData.class);
        when(mockDataStore.getChunkAt(location)).thenReturn(mockChunk);
        when(mockChunk.isTrusted(anyString())).thenReturn(false);
        when(mockDataStore.getChunkAt(location)).thenReturn(mockChunk);
        when(mockChunk.getOwnerName()).thenReturn("NotYou");

        systemUnderTest.onPlayerInteract(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou don't have NotYou's permission to build here.");
    }

}
