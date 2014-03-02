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

import com.kmaismith.chunkclaim.Data.DataManager;
import com.kmaismith.chunkclaim.Data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class PlayerJoinHandlerTest {

    private Player mockPlayer;
    private DataManager mockDataManager;
    private PlayerData chunkPlayer;
    private PlayerEventHandler systemUnderTest;

    @Before
    public void setupTestCase() {
        mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("PlayerPerson");
        mockDataManager = mock(DataManager.class);
        chunkPlayer = mock(PlayerData.class);
        when(mockDataManager.readPlayerData("PlayerPerson")).thenReturn(chunkPlayer);
        systemUnderTest = new PlayerEventHandler(mockDataManager);
    }

    // onPlayerJoin

    @Test
    public void testWhenPlayerJoinsPlayerDataIsLoadedFromDataStore() {
        PlayerJoinEvent event = new PlayerJoinEvent(mockPlayer, "Player has joined");

        systemUnderTest.onPlayerJoin(event);
        verify(mockDataManager).readPlayerData("PlayerPerson");
        verify(mockDataManager).savePlayerData(chunkPlayer);
    }

    @Test
    public void testWhenPlayerJoinsThePlayerIsNotifiedThatChunkClaimIsInBeta() {
        PlayerJoinEvent event = new PlayerJoinEvent(mockPlayer, "Player has joined");

        systemUnderTest.onPlayerJoin(event);
        verify(mockPlayer).sendMessage(
                "§eServer Running §4ChunkClaim Beta§e. Have fun and report any bugs to an admin");
    }

    // onPlayerQuit
    @Test
    public void testWhenPlayerQuitsDataIsSavedAndCacheIsCleared() {
        PlayerQuitEvent event = new PlayerQuitEvent(mockPlayer, "Player has quit");

        systemUnderTest.onPlayerQuit(event);
        verify(mockDataManager).savePlayerData(chunkPlayer);
        verify(mockDataManager).clearCachedPlayerData("PlayerPerson");
    }
}
