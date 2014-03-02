package com.kmaismith.ChunkClaim;

import com.kmaismith.ChunkClaim.Data.ChunkData;
import com.kmaismith.ChunkClaim.Data.DataManager;
import com.kmaismith.ChunkClaim.Data.PlayerData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class ChunkCommandTest {
    private ChunkClaim systemUnderTest;
    private Player mockPlayer;
    private Command mockCommand;
    private String commandLabel;
    private String[] args;
    private DataManager dataStore;

    private final int dayInMilliseconds = 1000 * 60 * 60 * 24;

    @Before
    public void setupTestCase() {
        Logger minecraftLogger = mock(Logger.class);
        //ChunkClaimLogger logger = spy(ChunkClaimLogger(mock(Logger.class)))

        dataStore = mock(DataManager.class);
        systemUnderTest = spy(new ChunkClaim(minecraftLogger, dataStore));

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

        when(mockLocation.getBlockX()).thenReturn(x * 16);
        when(mockLocation.getBlockZ()).thenReturn(z * 16);

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
        when(dataStore.getChunkAt(mockLocation)).thenReturn(chunk);
        when(chunk.getBuilderNames()).thenReturn(trustedBuilders);


        when(chunk.isTrusted(anyString())).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                String arg = (String) invocation.getArguments()[0];

                return trustedBuilders.contains(arg);
            }
        });

        return chunk;
    }

    private ChunkData setupChunk(String playerName, final ArrayList<String> trustedBuilders, int x, int z) {
        ChunkData chunk = setupChunk(playerName, trustedBuilders, setupLocation(x, z));

        when(chunk.getChunkX()).thenReturn(x);
        when(chunk.getChunkZ()).thenReturn(z);
        return chunk;
    }

    private PlayerData setupPlayer(String playerName, int daysSinceLogin, int daysSinceFirstLogin) {
        PlayerData player = mock(PlayerData.class);
        when(dataStore.readPlayerData(playerName)).thenReturn(player);
        Date lastLogin = new Date((new Date()).getTime() - daysSinceLogin);
        when(player.getLastLogin()).thenReturn(lastLogin);
        Date firstLogin = new Date((new Date()).getTime() - daysSinceFirstLogin);
        when(player.getFirstJoin()).thenReturn(firstLogin);
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
        setupChunk("RandomPlayer", new ArrayList<String>(), setupLocation(0, 0));

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
        setupPlayer("APlayer", 2 * dayInMilliseconds, 4 * dayInMilliseconds);

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
        setupPlayer("SamplePlayer", 2 * dayInMilliseconds, 4 * dayInMilliseconds);

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
        setupPlayer("APlayer", 2 * dayInMilliseconds, 4 * dayInMilliseconds);

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
        setupPlayer("APlayer", 0, 4 * dayInMilliseconds);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(dataStore).deleteChunk(mockChunk);
        verify(mockPlayer).sendMessage("§eChunkData abandoned. Credits: 0");
    }

    @Test
    public void testChunkAbandonCommandAbandonsTheChunkIfAdminAndNotOwnedByPlayer() {
        args = new String[]{"abandon"};

        Location mockLocation = setupLocation(12, -34);
        ChunkData mockChunk = setupChunk("SamplePlayer",
                new ArrayList<String>(),
                mockLocation);
        setupPlayer("APlayer", 0, 4 * dayInMilliseconds);
        setPlayerAsAdmin();

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(dataStore).deleteChunk(mockChunk);
        verify(mockPlayer).sendMessage("§eChunkData abandoned. Credits: 0");
    }

    @Test
    public void testChunkAbandonCommandDoesNotAbandonIfNonAdminAndPlayerDoesntOwnChunk() {
        args = new String[]{"abandon"};

        Location mockLocation = setupLocation(12, -34);
        ChunkData mockChunk = setupChunk("SamplePlayer",
                new ArrayList<String>(),
                mockLocation);
        setupPlayer("SamplePlayer", 0, 4 * dayInMilliseconds);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(dataStore, never()).deleteChunk(mockChunk);
        verify(mockPlayer).sendMessage("§eYou don't own this chunk. Only SamplePlayer or the staff can delete it.");
    }

    @Test
    public void testChunkAbandonCommandSpitsOutAnError() {
        args = new String[]{"abandon"};

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(dataStore, never()).deleteChunk((ChunkData) anyObject());
        verify(mockPlayer).sendMessage("§eThis chunk is public.");
    }

    @Test
    public void testChunkAbandonReturnsACreditForAbandoning() {
        args = new String[]{"abandon"};

        Location mockLocation = setupLocation(12, -34);
        setupChunk("APlayer",
                new ArrayList<String>(),
                mockLocation);
        PlayerData playerMock = setupPlayer("APlayer", 0, 4 * dayInMilliseconds);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(playerMock).addCredit();
    }

    // /chunk credits

    @Test
    public void testChunkCreditsShowsHowManyCreditsAPlayerHas() {
        args = new String[]{"credits"};
        PlayerData playerMock = setupPlayer("APlayer", 0, 4 * dayInMilliseconds);
        when(playerMock.getCredits()).thenReturn(7);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eYou have 7 credits.");
    }

    // /chunk list
    @Test
    public void testChunkListDisplaysAListOfChunksOwnedByPlayerWhenPlayerIsOnline() {
        args = new String[]{"list", "ListablePlayer"};
        setupPlayer("ListablePlayer", dayInMilliseconds, 4 * dayInMilliseconds);
        setPlayerAsAdmin();
        Server mockServer = mock(Server.class);
        Player onlinePlayer = mock(Player.class);

        when(systemUnderTest.getServerWrapper()).thenReturn(mockServer);
        when(mockServer.getPlayer("ListablePlayer")).thenReturn(onlinePlayer);

        setupPlayersChunks("ListablePlayer");

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eListablePlayer | Last Login: 1 days ago. First Join: 4 days ago.");
        verify(mockPlayer).sendMessage("§eID: 12|34, World Location: 192|544");
        verify(mockPlayer).sendMessage("§eID: 13|35, World Location: 208|560");
    }

    @Test
    public void testChunkListDisplaysAListOfChunksOwnedByPlayerWhenPlayerIsOffline() {
        args = new String[]{"list", "ListablePlayer"};
        setupPlayer("ListablePlayer", dayInMilliseconds, 4 * dayInMilliseconds);
        setPlayerAsAdmin();

        Server mockServer = mock(Server.class);
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(systemUnderTest.getServerWrapper()).thenReturn(mockServer);
        when(mockServer.getOfflinePlayer("ListablePlayer")).thenReturn(offlinePlayer);

        setupPlayersChunks("ListablePlayer");

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eListablePlayer | Last Login: 1 days ago. First Join: 4 days ago.");
        verify(mockPlayer).sendMessage("§eID: 12|34, World Location: 192|544");
        verify(mockPlayer).sendMessage("§eID: 13|35, World Location: 208|560");
    }

    @Test
    public void testNonAdminPlayersCanListTheirOwnChunks() {
        args = new String[]{"list"};
        setupPlayer("APlayer", dayInMilliseconds, 4 * dayInMilliseconds);

        setupPlayersChunks("APlayer");

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eHere are your chunks:");
        verify(mockPlayer).sendMessage("§eID: 12|34, World Location: 192|544");
        verify(mockPlayer).sendMessage("§eID: 13|35, World Location: 208|560");
    }

    private void setupPlayersChunks(String playername) {
        ArrayList<ChunkData> twoChunk = new ArrayList<ChunkData>();

        ChunkData chunkOne = setupChunk(playername, null, 12, 34);
        ChunkData chunkTwo = setupChunk(playername, null, 13, 35);
        twoChunk.add(chunkOne);
        twoChunk.add(chunkTwo);

        when(dataStore.getChunksForPlayer(playername)).thenReturn(twoChunk);
    }
}
