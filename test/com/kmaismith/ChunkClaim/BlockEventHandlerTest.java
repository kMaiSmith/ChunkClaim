/*
    ChunkClaim Plugin for Minecraft Bukkit Servers
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

package com.kmaismith.chunkclaim;

import com.kmaismith.chunkclaim.Data.ChunkData;
import com.kmaismith.chunkclaim.Data.DataManager;
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
