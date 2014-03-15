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
import com.kmaismith.chunkclaim.Data.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class ChunkCreditsCommandTest {
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
    public void testChunkCreditsShowsHowManyCreditsAPlayerHas() {
        args = new String[]{"credits"};

        Player mockPlayer = dataHelper.newPlayer("APlayer", null, false);
        PlayerData playerMock = dataHelper.newPlayerData(mockPlayer, 0, 4 * dayInMilliseconds);
        when(playerMock.getCredits()).thenReturn(7);

        systemUnderTest.onCommand(mockPlayer, mockCommand, commandLabel, args);
        verify(mockPlayer).sendMessage("§eYou have 7 credits.");
    }
}
