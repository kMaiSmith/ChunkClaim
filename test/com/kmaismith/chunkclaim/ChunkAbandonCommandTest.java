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
import com.kmaismith.chunkclaim.Data.PlayerData;
import junit.framework.Assert;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.logging.Logger;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

public class ChunkAbandonCommandTest {
    private ChunkClaim systemUnderTest;
    private String[] args;
    private DataManager dataManager;
    private DataHelper dataHelper;
    private Command mockCommand;
    private String commandLabel;

    @Before
    public void setupTestCase() {
        Logger minecraftLogger = mock(Logger.class);
        dataManager = mock(DataManager.class);
        dataHelper = new DataHelper(dataManager);
        systemUnderTest = spy(new ChunkClaim(minecraftLogger, dataManager));
        mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("chunk");
        commandLabel = "";
    }

    @Test
    public void testChunkAbandonCommandAbandonsTheChunkBeingStoodIn() {
        args = new String[]{"abandon"};

        Location mockLocation = dataHelper.newLocation("batworld", 12, -34);
        ChunkData mockChunk = dataHelper.newChunkData(
                "APlayer",
                new ArrayList<String>(),
                mockLocation);

        Player mockPlayer = dataHelper.newPlayer("APlayer", mockLocation, false);
        dataHelper.newPlayerData(mockPlayer, 0, 0);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(dataManager).deleteChunk(mockChunk);
        verify(mockPlayer).sendMessage("§eChunkData abandoned. Credits: 0");
    }

    @Test
    public void testChunkAbandonCommandAbandonsTheChunkIfAdminAndNotOwnedByPlayer() {
        args = new String[]{"abandon"};

        Location mockLocation = dataHelper.newLocation("scatmansworld", 0, -3000);
        ChunkData mockChunk = dataHelper.newChunkData(
                "Owner",
                new ArrayList<String>(),
                mockLocation);

        Player mockPlayer = dataHelper.newPlayer("Admin", mockLocation, true);
        dataHelper.newPlayerData(mockPlayer, 0, 0);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(dataManager).deleteChunk(mockChunk);
        verify(mockPlayer).sendMessage("§eChunkData abandoned. Credits: 0");
    }

    @Test
    public void testChunkAbandonCommandDoesNotAbandonIfNonAdminAndPlayerDoesntOwnChunk() {
        args = new String[]{"abandon"};

        Location mockLocation = dataHelper.newLocation("worldofwarcraft", -57, -34);
        ChunkData mockChunk = dataHelper.newChunkData(
                "OwnerGuy1337",
                new ArrayList<String>(),
                mockLocation);

        Player mockPlayer = dataHelper.newPlayer("TotallyNotAGriefer", mockLocation, false);
        dataHelper.newPlayerData(mockPlayer, 0, 0);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(dataManager, never()).deleteChunk(mockChunk);
        verify(mockPlayer).sendMessage("§eYou don't own this chunk. Only OwnerGuy1337 or the staff can delete it.");
    }

    @Test
    public void testChunkAbandonCommandSpitsOutAnErrorInPublicChunk() {
        args = new String[]{"abandon"};
        Player mockPlayer = dataHelper.newPlayer(null, null, false);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(dataManager, never()).deleteChunk((ChunkData) anyObject());
        verify(mockPlayer).sendMessage("§eThis chunk is public.");
    }

    @Test
    public void testChunkAbandonReturnsACreditForAbandoning() {
        args = new String[]{"abandon"};

        Location mockLocation = dataHelper.newLocation("joytotheworld", 13, -37);

        dataHelper.newChunkData(
                "APlayer",
                new ArrayList<String>(),
                mockLocation);

        Player mockPlayer = dataHelper.newPlayer("APlayer", mockLocation, false);
        PlayerData playerMock = dataHelper.newPlayerData(mockPlayer, 0, 0);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(playerMock).addCredit();
    }

    @Test
    public void testChunkAbandonAllAbandonsAllChunks() {
        args = new String[]{"abandon","all"};

        ArrayList<ChunkData> chunkDatas = new ArrayList<ChunkData>();
        // The execution path is simply testing for the size of the array, not the contents
        chunkDatas.add(null);
        chunkDatas.add(null);
        when(dataManager.getChunksForPlayer("FooPlayer")).thenReturn(chunkDatas);

        Player playerMock = dataHelper.newPlayer("FooPlayer", dataHelper.newLocation("wallyworld", 4, -9), false);
        dataHelper.newPlayerData(playerMock, 0, 0);

        systemUnderTest.onCommand(playerMock, mockCommand, commandLabel, args);

        verify(dataManager).deleteChunksForPlayer("FooPlayer");

        verify(playerMock).sendMessage("§eYour chunks have been abandoned. Credits: 0");
    }

    @Test
    public void testAbandonAllHappyPathReturnsTrue() {
        args = new String[]{"abandon","all"};

        ArrayList<ChunkData> chunkDatas = new ArrayList<ChunkData>();
        // The execution path is simply testing for the size of the array, not the contents
        chunkDatas.add(null);
        chunkDatas.add(null);
        when(dataManager.getChunksForPlayer("FooPlayer")).thenReturn(chunkDatas);

        Player playerMock = dataHelper.newPlayer("FooPlayer", dataHelper.newLocation("wallyworld", 4, -9), false);
        dataHelper.newPlayerData(playerMock, 0, 0);

        Assert.assertEquals(systemUnderTest.onCommand(playerMock, mockCommand, commandLabel, args), true);
    }

    @Test
    public void testChunklessPlayerReceivesErrorMessage() {
        args = new String[]{"abandon","all"};
        Player playerMock = dataHelper.newPlayer("PlayerZ", dataHelper.newLocation("supermarioland",-3,329), false);
        dataHelper.newPlayerData(playerMock, 0, 0);

        systemUnderTest.onCommand(playerMock, mockCommand, commandLabel, args);

        verify(playerMock).sendMessage("§eYou don't have any chunks.");
    }
}
