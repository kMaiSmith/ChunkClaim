package com.kmaismith.chunkclaim;

import com.kmaismith.chunkclaim.Data.ChunkData;
import com.kmaismith.chunkclaim.Data.DataManager;
import com.kmaismith.chunkclaim.Data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Date;

class ChunkCommandHandler {
    private final ChunkClaim chunkClaim;
    private final DataManager dataManager;

    public ChunkCommandHandler(ChunkClaim chunkClaim, DataManager dataManager) {
        this.chunkClaim = chunkClaim;
        this.dataManager = dataManager;
    }

    void handleChunkCredits(Player player) {
        PlayerData playerData = dataManager.readPlayerData(player.getName());
        chunkClaim.sendMsg(player, "You have " + playerData.getCredits() + " credits.");
    }

    boolean handeChunkAbandon(Player player) {
        ChunkData chunk = dataManager.getChunkAt(player.getLocation());
        PlayerData playerData = dataManager.readPlayerData(player.getName());

        if (chunk == null) {
            chunkClaim.sendMsg(player, "This chunk is public.");


        } else if (chunk.getOwnerName().equals(player.getName()) || player.hasPermission("chunkclaim.admin")) {
            dataManager.deleteChunk(chunk);
            playerData.addCredit();
            dataManager.savePlayerData(playerData);
            chunkClaim.sendMsg(player, "ChunkData abandoned. Credits: " + playerData.getCredits());
            return true;

        } else {

            chunkClaim.sendMsg(player, "You don't own this chunk. Only " + chunk.getOwnerName() + " or the staff can delete it.");
            return true;
        }
        return false;
    }

    void handleChunkInfoCommand(Player player) {
        ChunkData chunk = dataManager.getChunkAt(player.getLocation());
        Location location = player.getLocation();

        if (player.hasPermission("chunkclaim.admin")) {
            chunkClaim.sendMsg(player, "ID: " + location.getChunk().getX() + "," + location.getChunk().getZ());
            if (chunk != null && !chunk.getOwnerName().equals(player.getName())) {
                long loginDays = ((new Date()).getTime() - dataManager.readPlayerData(chunk.getOwnerName()).getLastLogin().getTime()) / (1000 * 60 * 60 * 24);
                chunkClaim.sendMsg(player, "Last Login: " + loginDays + " days ago.");
                StringBuilder builders = new StringBuilder();
                for (String builder : chunk.getBuilderNames()) {
                    builders.append(builder);
                    builders.append(" ");
                }
                chunkClaim.sendMsg(player, "Trusted Builders: " + builders.toString());
                chunkClaim.sendMsg(player, chunk.getOwnerName() + " owns this chunk.");
            }
        }

        if (chunk == null) {
            chunkClaim.sendMsg(player, "This chunk is public.");

        } else if (chunk.getOwnerName().equals(player.getName())) {
            if (chunk.getBuilderNames().size() > 0) {
                StringBuilder builders = new StringBuilder();
                for (String builder : chunk.getBuilderNames()) {
                    builders.append(builder);
                    builders.append(" ");
                }
                chunkClaim.sendMsg(player, "You own this chunk.");
                chunkClaim.sendMsg(player, "Trusted Builders: " + builders.toString());

            } else {
                chunkClaim.sendMsg(player, "You own this chunk. Use /chunk trust <player> to add other builders.");
            }

        } else {

            if (chunk.isTrusted(player.getName())) {
                chunkClaim.sendMsg(player, chunk.getOwnerName() + " owns this chunk. You have build rights!");
            } else {

                chunkClaim.sendMsg(player, chunk.getOwnerName() + " owns this chunk. You can't build here.");

            }
        }
    }
}