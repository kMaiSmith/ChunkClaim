package com.kmaismith.ChunkClaim;

import com.kmaismith.ChunkClaim.Data.ChunkData;
import com.kmaismith.ChunkClaim.Data.DataManager;
import com.kmaismith.ChunkClaim.Data.PlayerData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Mockito.*;

public class ChunkCommandTest {
    private ChunkClaim systemUnderTest;
    private Player mockPlayer;
    private Command mockCommand;
    private String commandLabel;
    private String[] args;

    private static int dayInMilliseconds = 1000 * 60 * 60 * 24;

    @Before
    public void setupTestCase() {
        systemUnderTest = new ChunkClaim();
        systemUnderTest.dataStore = mock(DataManager.class);

        mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("APlayer");

        mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("chunk");

        commandLabel = "";
        args = new String[]{};
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

    private ChunkData setupChunk(String playerName, final ArrayList<String> trustedBuilders, Location mockLocation) {
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

        return chunk;
    }

    private PlayerData setupPlayer(String playerName, int daysSinceLogin) {
        PlayerData player = mock(PlayerData.class);
        when(systemUnderTest.dataStore.readPlayerData(playerName)).thenReturn(player);
        Date lastLogin = new Date((new Date()).getTime() - daysSinceLogin);
        when(player.getLastLogin()).thenReturn(lastLogin);
        return player;
    }

    // /chunk

    @Test
    public void testChunkCommandSaysTheChunkIsPublicWhenTheChunkIsntClaimed() {
        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eThis chunk is public.");

    }

    @Test
    public void testChunkCommandSaysWhoOwnsTheChunkWhenTheChunkIsClaimed() {
        setupChunk("RandomPlayer", new ArrayList<String>(), null);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eRandomPlayer owns this chunk. You can't build here.");
    }

    @Test
    public void testChunkCommandSaysYouHaveBuildRightsWhenYouAreTrustedOnAChunk() {
        setupChunk("RandomPlayer",
                new ArrayList<String>(Arrays.asList(new String[]{"APlayer"})),
                null);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eRandomPlayer owns this chunk. You have build rights!");
    }

    @Test
    public void testChunkCommandGivesAListOfTrustedBuildersWhenYouOwnTheChunk() {
        // Super awesome setup stuff, prone to changing with flow changes

        Location mockLocation = setupLocation(12, -34);
        setupChunk("APlayer",
                new ArrayList<String>(Arrays.asList(new String[]{"PlayerA", "PlayerB"})),
                mockLocation);
        setupPlayer("APlayer", 2 * dayInMilliseconds);

        // End of super awesome setup stuff

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eYou own this chunk.");
        verify(mockPlayer).sendMessage("§eTrusted Builders: PlayerA PlayerB ");
    }

    @Test
    public void testChunkCommandTellsTheChunkIdWhenPlayerHasAdminRightsAndTheChunkIsntClaimed() {
        setupLocation(12, -34);

        setPlayerAsAdmin();

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eID: 12,-34");
        verify(mockPlayer).sendMessage("§eThis chunk is public.");
    }

    @Test
    public void testChunkCommandGivesDaysSinceLastLoginAndTrustedBuilderAndOwnerWhenCalledByAdminWhoIsNotTheOwner() {
        Location mockLocation = setupLocation(12, -34);
        setPlayerAsAdmin();
        setupChunk("SamplePlayer",
                new ArrayList<String>(Arrays.asList(new String[]{"PlayerA", "PlayerB"})),
                mockLocation);
        setupPlayer("SamplePlayer", 2 * dayInMilliseconds);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eID: 12,-34");
        verify(mockPlayer).sendMessage("§eLast Login: 2 days ago.");
        verify(mockPlayer).sendMessage("§eTrusted Builders: PlayerA PlayerB ");
        verify(mockPlayer).sendMessage("§eSamplePlayer owns this chunk.");

    }

    @Test
    public void testChunkCommandShowsChunkIDAndStandardInformationIfOwner() {
        // Super awesome setup stuff, prone to changing with flow changes

        Location mockLocation = setupLocation(12, -34);
        setPlayerAsAdmin();
        setupChunk("APlayer",
                new ArrayList<String>(Arrays.asList(new String[]{"PlayerA", "PlayerB"})),
                mockLocation);
        setupPlayer("APlayer", 2 * dayInMilliseconds);

        // End of super awesome setup stuff

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eID: 12,-34");
        verify(mockPlayer).sendMessage("§eYou own this chunk.");
        verify(mockPlayer).sendMessage("§eTrusted Builders: PlayerA PlayerB ");
    }

    // /chunk abandon

    @Test
    public void testChunkAbandonCommandAbandonsTheChunkBeingStoodIn() {
        args = new String[]{"abandon"};

        Location mockLocation = setupLocation(12, -34);
        ChunkData mockChunk = setupChunk("APlayer",
                new ArrayList<String>(),
                mockLocation);
        setupPlayer("APlayer", 0);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(systemUnderTest.dataStore).deleteChunk(mockChunk);
        verify(mockPlayer).sendMessage("§eChunkData abandoned. Credits: 0");
    }

    @Test
    public void testChunkAbandonCommandAbandonsTheChunkIfAdminAndNotOwnedByPlayer() {
        args = new String[]{"abandon"};

        Location mockLocation = setupLocation(12, -34);
        ChunkData mockChunk = setupChunk("SamplePlayer",
                new ArrayList<String>(),
                mockLocation);
        setupPlayer("APlayer", 0);
        setPlayerAsAdmin();

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(systemUnderTest.dataStore).deleteChunk(mockChunk);
        verify(mockPlayer).sendMessage("§eChunkData abandoned. Credits: 0");
    }

    @Test
    public void testChunkAbandonCommandDoesNotAbandonIfNonAdminAndPlayerDoesntOwnChunk() {
        args = new String[]{"abandon"};

        Location mockLocation = setupLocation(12, -34);
        ChunkData mockChunk = setupChunk("SamplePlayer",
                new ArrayList<String>(),
                mockLocation);
        setupPlayer("SamplePlayer", 0);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(systemUnderTest.dataStore, never()).deleteChunk(mockChunk);
        verify(mockPlayer).sendMessage("§eYou don't own this chunk. Only SamplePlayer or the staff can delete it.");
    }

    @Test
    public void testChunkAbandonCommandSpitsOutAnError() {
        args = new String[]{"abandon"};

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(systemUnderTest.dataStore, never()).deleteChunk((ChunkData) anyObject());
        verify(mockPlayer).sendMessage("§eThis chunk is public.");
    }

    @Test
    public void testChunkAbandonReturnsACreditForAbandoning() {
        args = new String[]{"abandon"};

        Location mockLocation = setupLocation(12, -34);
        setupChunk("APlayer",
                new ArrayList<String>(),
                mockLocation);
        PlayerData playerMock = setupPlayer("APlayer", 0);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(playerMock).addCredit();
    }

    // /chunk credits

    @Test
    public void testChunkCreditsShowsHowManyCreditsAPlayerHas() {
        args = new String[]{"credits"};
        PlayerData playerMock = setupPlayer("APlayer", 0);
        when(playerMock.getCredits()).thenReturn(7);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eYou have 7 credits.");
    }

    // /chunk list
    @Test
    public void testChunkList() {
    }
}
