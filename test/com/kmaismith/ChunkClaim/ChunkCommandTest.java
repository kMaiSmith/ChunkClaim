package com.kmaismith.ChunkClaim;

import com.github.schmidtbochum.chunkclaim.ChunkClaim;
import com.github.schmidtbochum.chunkclaim.Data.ChunkData;
import com.github.schmidtbochum.chunkclaim.Data.DataManager;
import com.github.schmidtbochum.chunkclaim.Data.PlayerData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: kyle
 * Date: 2/25/14
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChunkCommandTest {
    private ChunkClaim systemUnderTest;
    private Player mockPlayer;
    private Command mockCommand;
    private String commandLabel;
    private String[] args = {};

    private static int dayInMilliseconds = 1000 * 60 * 60 * 24;

    private void basicSetup(String playerName) {
        systemUnderTest = new ChunkClaim();
        systemUnderTest.dataStore = mock(DataManager.class);

        mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn(playerName);

        mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("chunk");

        commandLabel = "";
    }

    private Location setupLocation(int x, int z) {
        Chunk bukkitChunk = mock(Chunk.class);
        when(bukkitChunk.getX()).thenReturn(x);
        when(bukkitChunk.getZ()).thenReturn(z);

        Location mockLocation = mock(Location.class);
        when(mockLocation.getChunk()).thenReturn(bukkitChunk);
        when(mockPlayer.getLocation()).thenReturn(mockLocation);
        return mockLocation;
    }

    private void setPlayerAsAdmin() {
        when(mockPlayer.hasPermission("chunkclaim.admin")).thenReturn(true);
    }

    private void setupChunk(String playerName, final ArrayList<String> trustedBuilders, Location mockLocation) {
        ChunkData chunk = mock(ChunkData.class);
        when(chunk.getOwnerName()).thenReturn(playerName);
        when(systemUnderTest.dataStore.getChunkAt(mockLocation)).thenReturn(chunk);
        when(chunk.getBuilderNames()).thenReturn(trustedBuilders);

        when(chunk.isTrusted(anyString())).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();

                return trustedBuilders.contains(args[0]);
            }
        });
    }

    private void setupPlayer(String playerName, int daysSinceLogin) {
        PlayerData player = mock(PlayerData.class);
        when(systemUnderTest.dataStore.readPlayerData(playerName)).thenReturn(player);
        Date lastLogin = new Date((new Date()).getTime() - daysSinceLogin);
        when(player.getLastLogin()).thenReturn(lastLogin);
    }

    @Test
    public void testChunkCommandSaysTheChunkIsPublicWhenTheChunkIsntClaimed() {
        basicSetup("");

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eThis chunk is public.");

    }

    @Test
    public void testChunkCommandSaysWhoOwnsTheChunkWhenTheChunkIsClaimed() {
        basicSetup("FriendlyPlayer");

        setupChunk("RandomPlayer", new ArrayList<String>(), null);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eRandomPlayer owns this chunk. You can't build here.");
    }

    @Test
    public void testChunkCommandSaysYouHaveBuildRightsWhenYouAreTrustedOnAChunk() {
        basicSetup("FriendlyPlayer");

        setupChunk("RandomPlayer",
                new ArrayList<String>(Arrays.asList(new String[]{"FriendlyPlayer"})),
                null);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eRandomPlayer owns this chunk. You have build rights!");
    }

    @Test
    public void testChunkCommandTellsTheChunkIdWhenPlayerHasAdminRights() {
        basicSetup("");

        setupLocation(12, -34);

        setPlayerAsAdmin();

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eID: 12,-34");
        verify(mockPlayer).sendMessage("§eThis chunk is public.");
    }

    @Test
    public void testChunkCommandGivesDaysSinceLastLoginAndTrustedBuilderAndOwnerWhenCalledByAdminWhoIsNotTheOwner() {
        basicSetup("");

        // Super awesome setup stuff, prone to changing with flow changes

        Location mockLocation = setupLocation(12, -34);
        setPlayerAsAdmin();
        setupChunk("SamplePlayer",
                new ArrayList<String>(Arrays.asList(new String[]{"PlayerA", "PlayerB"})),
                mockLocation);
        setupPlayer("SamplePlayer", 2 * dayInMilliseconds);

        // End of super awesome setup stuff

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eID: 12,-34");
        verify(mockPlayer).sendMessage("§eLast Login: 2 days ago.");
        verify(mockPlayer).sendMessage("§eTrusted Builders: PlayerA PlayerB ");
        verify(mockPlayer).sendMessage("§eSamplePlayer owns this chunk.");

    }

    @Test
    public void testChunkCommandShowsChunkIDAndStandardInformationIfOwner() {
        basicSetup("SamplePlayer");

        // Super awesome setup stuff, prone to changing with flow changes

        Location mockLocation = setupLocation(12, -34);
        setPlayerAsAdmin();
        setupChunk("SamplePlayer",
                new ArrayList<String>(Arrays.asList(new String[]{"PlayerA", "PlayerB"})),
                mockLocation);
        setupPlayer("SamplePlayer", 2 * dayInMilliseconds);

        // End of super awesome setup stuff

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eID: 12,-34");
        verify(mockPlayer).sendMessage("§eYou own this chunk.");
        verify(mockPlayer).sendMessage("§eTrusted Builders: PlayerA PlayerB ");
    }
}
