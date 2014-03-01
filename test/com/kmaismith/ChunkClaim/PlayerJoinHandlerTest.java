package com.kmaismith.ChunkClaim;

import com.github.schmidtbochum.chunkclaim.Data.DataManager;
import com.github.schmidtbochum.chunkclaim.Data.PlayerData;
import com.github.schmidtbochum.chunkclaim.PlayerEventHandler;
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
