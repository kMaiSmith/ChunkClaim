/*
    ChunkClaim Plugin for Minecraft Bukkit Servers
    Copyright (C) 2012 Felix Schmidt
    
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

package com.kmaismith.ChunkClaim.Data;

import com.kmaismith.ChunkClaim.ChunkClaim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class PlayerData implements IData {
    private File playerFile;
    private String playerName;
    private int credits = (int) ChunkClaim.plugin.config_startCredits;
    private ArrayList<String> builderNames = new ArrayList<String>();
    private Date lastLogin = new Date();
    private Date firstJoin = new Date();

    private PlayerData() {
        this.firstJoin = new Date();
        this.lastLogin = new Date();
    }

    PlayerData(String playerName) {
        this();
        this.playerFile = new File(PlayerData.playerDataFolderPath + File.separator + playerName + ".dat");
        this.playerName = playerName;
    }

    final static String playerDataFolderPath = dataLayerFolderPath + File.separator + "PlayerData";

    public int getCredits() {
        return credits;
    }

    public void addCredit() {
        credits++;
    }

    public void subtractCredit() {
        credits--;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public Date getFirstJoin() {
        return firstJoin;
    }

    public ArrayList<String> getBuilderNames() {
        return builderNames;
    }

    public void readDataFromFile(BufferedReader inStream) throws IOException {
        //first line is first join date
        String firstJoinTimestampString = inStream.readLine();

        //convert that to a date and store it
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        try {
            firstJoin = dateFormat.parse(firstJoinTimestampString);
        } catch (ParseException parseException) {
            ChunkClaim.addLogEntry("Unable to load first join date for \"" + playerName + "\"... Falling back to today's date");
            firstJoin = new Date();
        }

        //first line is last login timestamp
        String lastLoginTimestampString = inStream.readLine();

        //convert that to a date and store it
        try {
            lastLogin = dateFormat.parse(lastLoginTimestampString);
        } catch (ParseException parseException) {
            ChunkClaim.addLogEntry("Unable to load last login for \"" + getFile().getName() + "\"... Falling back to today's date");
            lastLogin = new Date();
        }
        //third line is credits
        String creditsString = inStream.readLine();
        credits = Integer.parseInt(creditsString);

        //5. line: list of builders
        builderNames = new ArrayList<String>(Arrays.asList(inStream.readLine().split(";")));

        inStream.close();

    }

    public void writeDataToFile(BufferedWriter outStream) throws IOException {
        //first line is first join date
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        outStream.write(dateFormat.format(firstJoin));
        outStream.newLine();

        outStream.write(dateFormat.format(new Date()));
        outStream.newLine();

        //third line is credits
        outStream.write(String.valueOf(credits));
        outStream.newLine();

        //fifth line are trusted builders (trusted on all chunks)
        for (String builder : builderNames) {
            outStream.write(builder + ";");
        }
        outStream.newLine();

        //filled line to prevent null
        outStream.write("==========");
        outStream.newLine();
    }

    public File getFile() {
        return playerFile;
    }
}
