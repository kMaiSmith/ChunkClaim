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
