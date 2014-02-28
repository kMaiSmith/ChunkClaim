package com.kmaismith.ChunkClaim;

import com.github.schmidtbochum.chunkclaim.Data.DataManager;
import com.github.schmidtbochum.chunkclaim.Data.PlayerData;
import com.github.schmidtbochum.chunkclaim.PlayerEventHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: kyle
 * Date: 2/28/14
 * Time: 6:03 PM
 * To change this template use File | Settings | File Templates.
 */
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
}
