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


package com.kmaismith.randompackage;

import com.kmaismith.chunkclaim.ChunkClaim;
import org.junit.Test;

public class ChunkClaimPluginTest {

    // Do note this is a really not nice test.  the reason for this test is
    // so i don't fall for the same mistake twice. if the default constructor
    // isn't public then it upon bukkit trying to load the plugin it explodes
    // in your face with an obscure error.

    // this test should become irrelevant when a proper end to end acceptance
    // test is written
    @Test
    public void testChunkClaimDefaultConstructorMustBePublicToBeConsideredAValidPlugin() {
        new ChunkClaim();
    }
}
