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
