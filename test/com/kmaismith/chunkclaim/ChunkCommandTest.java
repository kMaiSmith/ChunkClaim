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
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class ChunkCommandTest {
    private ChunkClaim systemUnderTest;
    private Player mockPlayer;
    private Command mockCommand;
    private String commandLabel;
    private String[] args;
    private DataManager dataStore;
    private DataHelper dataHelper;

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

        dataHelper = new DataHelper(dataStore);
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

    // /chunk credits

    @Test
    public void testChunkCreditsShowsHowManyCreditsAPlayerHas() {
        args = new String[]{"credits"};
        PlayerData playerMock = setupPlayer("APlayer", 0, 4 * dayInMilliseconds);
        when(playerMock.getCredits()).thenReturn(7);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eYou have 7 credits.");
    }
}
