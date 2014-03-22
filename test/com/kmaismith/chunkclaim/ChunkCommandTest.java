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

import com.kmaismith.chunkclaim.Data.DataManager;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class ChunkCommandTest {
    private ChunkClaim systemUnderTest;
    private Command mockCommand;
    private String commandLabel;
    private String[] args;
    private DataManager dataManager;
    private DataHelper dataHelper;

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
        args = new String[]{};
    }

    @Test
    public void testChunkCommandSaysTheChunkIsPublicWhenTheChunkIsntClaimed() {
        Location mockLocation = dataHelper.newLocation("nirn",0,0);
        Player mockPlayer = dataHelper.newPlayer("FiveSyllableName", mockLocation, false);
        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eThis chunk is public.");
    }

    @Test
    public void testChunkCommandSaysWhoOwnsTheChunkWhenTheChunkIsClaimed() {
        Location mockLocation = dataHelper.newLocation("elmosworld", 1, 2);
        Player mockPlayer = dataHelper.newPlayer("FooPlayer", mockLocation, false);
        dataHelper.newChunkData(
                "RandomPlayer",
                new ArrayList<String>(),
                mockLocation);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eRandomPlayer owns this chunk. You can't build here.");
    }

    @Test
    public void testChunkCommandSaysYouHaveBuildRightsWhenYouAreTrustedOnAChunk() {
        Location mockLocation = dataHelper.newLocation("relto",-2,-3);
        Player mockPlayer = dataHelper.newPlayer("APlayer", mockLocation, false);
        dataHelper.newChunkData(
                "Owner",
                new ArrayList<String>(Arrays.asList(new String[]{"APlayer"})),
                mockLocation);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eOwner owns this chunk. You have build rights!");
    }

    @Test
    public void testChunkCommandGivesAListOfTrustedBuildersWhenYouOwnTheChunk() {
        // Super awesome setup stuff, prone to changing with flow changes
        Location mockLocation = dataHelper.newLocation("DIM-5000", -12, 34);
        Player mockPlayer = dataHelper.newPlayer("Foovakhin", mockLocation, false);
        dataHelper.newChunkData(
                "Foovakhin",
                new ArrayList<String>(Arrays.asList(new String[]{"PlayerA", "PlayerB"})),
                mockLocation);

        // End of super awesome setup stuff

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eYou own this chunk.");
        verify(mockPlayer).sendMessage("§eTrusted Builders: PlayerA PlayerB ");
    }

    @Test
    public void testChunkCommandTellsTheChunkIdWhenPlayerHasAdminRightsAndTheChunkIsntClaimed() {
        Location mockLocation = dataHelper.newLocation("tokyo", 12, -34);
        Player mockPlayer = dataHelper.newPlayer("BanHammerWielder", mockLocation, true);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eID: 12,-34");
        verify(mockPlayer).sendMessage("§eThis chunk is public.");
    }

    @Test
    public void testChunkCommandGivesDaysSinceLastLoginAndTrustedBuilderAndOwnerWhenCalledByAdminWhoIsNotTheOwner() {
        Location mockLocation = dataHelper.newLocation("didneyworl", 0, 666);
        Player mockPlayer = dataHelper.newPlayer("Walt", mockLocation, true);

        dataHelper.newChunkData(
                "Mickey",
                new ArrayList<String>(Arrays.asList(new String[]{"Minnie", "Pluto"})),
                mockLocation);

        Player chunkOwner = dataHelper.newPlayer("Mickey", mockLocation, false);
        dataHelper.newPlayerData(chunkOwner, 2 * dayInMilliseconds, 4 * dayInMilliseconds);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eID: 0,666");
        verify(mockPlayer).sendMessage("§eLast Login: 2 days ago.");
        verify(mockPlayer).sendMessage("§eTrusted Builders: Minnie Pluto ");
        verify(mockPlayer).sendMessage("§eMickey owns this chunk.");
    }

    @Test
    public void testChunkCommandShowsChunkIDAndStandardInformationIfOwnerAndAdmin() {
        // Super awesome setup stuff, prone to changing with flow changes

        Location mockLocation = dataHelper.newLocation("jurassicpark", -8, -3);
        Player mockPlayer = dataHelper.newPlayer("Raptor", mockLocation, true);
        dataHelper.newChunkData(
                "Raptor",
                new ArrayList<String>(Arrays.asList(new String[]{"Trex", "PlayerB"})),
                mockLocation);

        // End of super awesome setup stuff

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);

        verify(mockPlayer).sendMessage("§eID: -8,-3");
        verify(mockPlayer).sendMessage("§eYou own this chunk.");
        verify(mockPlayer).sendMessage("§eTrusted Builders: Trex PlayerB ");
    }
}
