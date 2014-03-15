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

package com.kmaismith.chunkclaim;

import com.kmaismith.chunkclaim.Data.ChunkData;
import com.kmaismith.chunkclaim.Data.DataManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class PlayerBlockHandlerTest {

    private PlayerEventHandler systemUnderTest;
    private DataManager mockDataStore;
    private Block mockBlock;
    private Player mockPlayer;
    private Location mockLocation;
    private ChunkData mockChunk;
    private Entity mockEntity;

    private void playerIsAdmin() {
        when(mockChunk.isTrusted("Admin")).thenReturn(false);
        when(mockDataStore.getChunkAt(mockLocation)).thenReturn(mockChunk);
        when(mockPlayer.getName()).thenReturn("Admin");
        when(mockPlayer.hasPermission("chunkclaim.admin")).thenReturn(true);
    }

    private void playerIsTrusted() {
        when(mockChunk.isTrusted("You")).thenReturn(true);
        when(mockDataStore.getChunkAt(mockLocation)).thenReturn(mockChunk);
        when(mockPlayer.getName()).thenReturn("You");
    }

    private void playerIsNotTrusted() {
        when(mockChunk.isTrusted("NotYou")).thenReturn(false);
        when(mockDataStore.getChunkAt(mockLocation)).thenReturn(mockChunk);
        when(mockChunk.getOwnerName()).thenReturn("NotYou");
    }

    @Before
    public void setupTestCase() {
        mockDataStore = mock(DataManager.class);
        systemUnderTest = new PlayerEventHandler(mockDataStore);

        mockPlayer = mock(Player.class);
        mockBlock = mock(Block.class);
        mockLocation = mock(Location.class);
        mockChunk = mock(ChunkData.class);
        mockEntity = mock(Entity.class);
        when(mockDataStore.getChunkAt(mockLocation)).thenReturn(mockChunk);
        when(mockBlock.getLocation()).thenReturn(mockLocation);
        when(mockEntity.getLocation()).thenReturn(mockLocation);
    }

    // onPlayerInteract

    @Test
    public void testUntrustedPlayerCannotInteractWithBlocksInClaimedChunk() {
        PlayerInteractEvent event = new PlayerInteractEvent(mockPlayer, null, null, mockBlock, null);

        playerIsNotTrusted();

        systemUnderTest.onPlayerInteract(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou do not have NotYou's permission to build here.");
    }

    @Test
    public void testAdminCanInteractWithBlocksInClaimedChunk() {
        PlayerInteractEvent event = new PlayerInteractEvent(mockPlayer, null, null, mockBlock, null);

        playerIsAdmin();

        systemUnderTest.onPlayerInteract(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testTrustedPlayerCanInteractWithBlocksInClaimedChunk() {
        PlayerInteractEvent event = new PlayerInteractEvent(mockPlayer, null, null, mockBlock, null);

        playerIsTrusted();

        systemUnderTest.onPlayerInteract(event);
        Assert.assertFalse(event.isCancelled());
    }

    // onPlayerBucketFill

    @Test
    public void testUntrustedPlayerCannotFilBucketsInClaimedChunk() {
        PlayerBucketFillEvent event = new PlayerBucketFillEvent(mockPlayer, mockBlock, null, null, null);

        playerIsNotTrusted();

        systemUnderTest.onPlayerBucketFill(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou do not have NotYou's permission to build here.");
    }

    @Test
    public void testAdminCanFillBucketsInNonOwnedChunkClaimedChunk() {
        PlayerBucketFillEvent event = new PlayerBucketFillEvent(mockPlayer, mockBlock, null, null, null);

        playerIsAdmin();

        systemUnderTest.onPlayerBucketFill(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testTrustedPlayerCanFillBucketsInClaimedChunk() {
        PlayerBucketFillEvent event = new PlayerBucketFillEvent(mockPlayer, mockBlock, null, null, null);

        playerIsAdmin();

        systemUnderTest.onPlayerBucketFill(event);
        Assert.assertFalse(event.isCancelled());
    }

    // onPlayerBucketEmpty

    @Test
    public void testUntrustedPlayerCannotEmptyBucketsInClaimedChunk() {
        PlayerBucketEmptyEvent event = new PlayerBucketEmptyEvent(mockPlayer, mockBlock, null, null, null);

        playerIsNotTrusted();

        systemUnderTest.onPlayerBucketEmpty(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou do not have NotYou's permission to build here.");
    }

    @Test
    public void testAdminCanEmptyBucketsInNonOwnedChunkClaimedChunk() {
        PlayerBucketEmptyEvent event = new PlayerBucketEmptyEvent(mockPlayer, mockBlock, null, null, null);

        playerIsAdmin();

        systemUnderTest.onPlayerBucketEmpty(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testTrustedPlayerCanEmptyBucketsInClaimedChunk() {
        PlayerBucketEmptyEvent event = new PlayerBucketEmptyEvent(mockPlayer, mockBlock, null, null, null);

        playerIsAdmin();

        systemUnderTest.onPlayerBucketEmpty(event);
        Assert.assertFalse(event.isCancelled());
    }

    // onPlayerInteractWithEntity

    @Test
    public void testUntrustedPlayerCannotInteractWithEntitiesInClaimedChunk() {
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(mockPlayer, mockEntity);

        playerIsNotTrusted();

        systemUnderTest.onPlayerInteractEntity(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou do not have NotYou's permission to interact with entities here.");
    }

    @Test
    public void testAdminCanInteractWithEntitiesInNonOwnedChunkClaimedChunk() {
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(mockPlayer, mockEntity);

        playerIsAdmin();

        systemUnderTest.onPlayerInteractEntity(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testTrustedPlayerCanInteractWithEntitiesInClaimedChunk() {
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(mockPlayer, mockEntity);

        playerIsAdmin();

        systemUnderTest.onPlayerInteractEntity(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testInteractingWithAirIsHandledGracefully() {
        PlayerInteractEvent event = new PlayerInteractEvent(mockPlayer, null, null, null, null);

        try {
            systemUnderTest.onPlayerInteract(event);
        } catch (NullPointerException e) {
            Assert.assertFalse("event.getClickedBlock() failed to get block because it was null", true);
        }
    }

}
