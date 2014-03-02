package com.kmaismith.ChunkClaim;

import java.util.logging.Logger;

public class ChunkClaimLogger {

    private Logger logger;

    ChunkClaimLogger(Logger minecraftLogger) {
        this.logger = minecraftLogger;
    }

    public void addLogEntry(String entry) {
        logger.info("[ChunkClaim] " + entry);
    }

}
