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
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class ChunkListCommandTest {
    private ChunkClaim systemUnderTest;
    private String[] args;
    private DataManager dataManager;
    private DataHelper dataHelper;
    private Command mockCommand;
    private String commandLabel;
    private final int dayInMilliseconds = 1000 * 60 * 60 * 24;

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
    public void testChunkListDisplaysAListOfChunksOwnedByPlayerWhenPlayerIsOnline() {
        args = new String[]{"list", "ListablePlayer"};

        Player dasAdmin = dataHelper.newPlayer("BigBadVoodooDaddy", null, true);
        dataHelper.newPlayerData(dasAdmin, 0, 0);

        Player mockPlayer = dataHelper.newPlayer("ListablePlayer", null, false);
        dataHelper.newPlayerData(mockPlayer, dayInMilliseconds, 4 * dayInMilliseconds);
        Server mockServer = mock(Server.class);
        Player onlinePlayer = mock(Player.class);

        when(systemUnderTest.getServerWrapper()).thenReturn(mockServer);
        when(mockServer.getPlayer("ListablePlayer")).thenReturn(onlinePlayer);

        setupPlayersChunks("ListablePlayer");

        systemUnderTest.onCommand(dasAdmin, mockCommand, commandLabel, args);

        verify(dasAdmin).sendMessage("§eListablePlayer | Last Login: 1 days ago. First Join: 4 days ago.");
        verify(dasAdmin).sendMessage("§eID: 12|34, World Location: 192|544");
        verify(dasAdmin).sendMessage("§eID: 13|35, World Location: 208|560");
    }

    @Test
    public void testChunkListDisplaysAListOfChunksOwnedByPlayerWhenPlayerIsOffline() {
        args = new String[]{"list", "ListablePlayer"};

        Player dasAdmin = dataHelper.newPlayer("BanHammerNator", null, true);
        dataHelper.newPlayerData(dasAdmin, 0, 0);

        Player mockPlayer = dataHelper.newPlayer("ListablePlayer", null, false);
        dataHelper.newPlayerData(mockPlayer, dayInMilliseconds, 4 * dayInMilliseconds);

        Server mockServer = mock(Server.class);
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(systemUnderTest.getServerWrapper()).thenReturn(mockServer);
        when(mockServer.getOfflinePlayer("ListablePlayer")).thenReturn(offlinePlayer);

        setupPlayersChunks("ListablePlayer");

        systemUnderTest.onCommand(dasAdmin, mockCommand, commandLabel, args);

        verify(dasAdmin).sendMessage("§eListablePlayer | Last Login: 1 days ago. First Join: 4 days ago.");
        verify(dasAdmin).sendMessage("§eID: 12|34, World Location: 192|544");
        verify(dasAdmin).sendMessage("§eID: 13|35, World Location: 208|560");
    }

    @Test
    public void testNonAdminPlayersCanListTheirOwnChunks() {
        args = new String[]{"list"};
        Player mockPlayer = dataHelper.newPlayer("APlayer", null, false);
        setupPlayersChunks("APlayer");

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eHere are your chunks:");
        verify(mockPlayer).sendMessage("§eID: 12|34, World Location: 192|544");
        verify(mockPlayer).sendMessage("§eID: 13|35, World Location: 208|560");
    }

    private void setupPlayersChunks(String playername) {
        ArrayList<ChunkData> chunkDatas = new ArrayList<ChunkData>();

        ChunkData chunkOne = dataHelper.newChunkData(playername, new ArrayList<String>(), 12, 34);
        ChunkData chunkTwo = dataHelper.newChunkData(playername, new ArrayList<String>(), 13, 35);
        chunkDatas.add(chunkOne);
        chunkDatas.add(chunkTwo);

        when(dataManager.getChunksForPlayer(playername)).thenReturn(chunkDatas);
    }
}
