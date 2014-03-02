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

package com.kmaismith.chunkclaim.Data;

import com.kmaismith.chunkclaim.ChunkClaim;
import junit.framework.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.*;

public class PlayerDataTest {

    @Test
    public void testPlayerDataSavesAppropriateLastLoginDate() {
        final int dayInMilliseconds = 1000 * 60 * 60 * 24;

        ChunkClaim.plugin = mock(ChunkClaim.class);
        ChunkClaim.plugin.config_startCredits = 10;

        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        Date firstJoin = new Date(new Date().getTime() - dayInMilliseconds);
        Date lastLogin = new Date(new Date().getTime() - dayInMilliseconds / 2);

        BufferedReader playerReader = mock(BufferedReader.class);
        PlayerData newPlayer = new PlayerData("Player");
        BufferedWriter playerWriter = mock(BufferedWriter.class);

        // The need for this try catch is either a mockito weakness or
        // a java one.
        try {
            when(playerReader.readLine())
                    .thenReturn(dateFormat.format(firstJoin))
                    .thenReturn(dateFormat.format(lastLogin))
                    .thenReturn("7")
                    .thenReturn(";PersonB;PersonC;PersonD;");

            newPlayer.readDataFromFile(playerReader);

            newPlayer.writeDataToFile(playerWriter);

            Date now = new Date();
            verify(playerWriter).write(dateFormat.format(firstJoin));
            verify(playerWriter).write(dateFormat.format(now));


        } catch (IOException e) {
            Assert.assertTrue(false);
        }

    }

}
