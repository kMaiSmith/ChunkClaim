/*
    ChunkClaim Plugin for Minecraft Bukkit Servers
    Copyright (C) 2012 Felix Schmidt
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

package com.kmaismith.chunkclaim;

import com.kmaismith.chunkclaim.Data.ChunkData;
import com.kmaismith.chunkclaim.Data.DataManager;
import com.kmaismith.chunkclaim.Data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

class PlayerEventHandler extends ChunkClaimEventHandler implements Listener {

    private final DataManager dataStore;

    public PlayerEventHandler(DataManager dataStore) {
        this.dataStore = dataStore;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = this.dataStore.readPlayerData(player.getName());

        player.sendMessage(ChatColor.YELLOW +
                "Server Running " + ChatColor.DARK_RED + "ChunkClaim Beta" + ChatColor.YELLOW +
                ". Have fun and report any bugs to an admin");
        this.dataStore.savePlayerData(playerData);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = this.dataStore.readPlayerData(player.getName());

        this.dataStore.savePlayerData(playerData);
        this.dataStore.clearCachedPlayerData(player.getName());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        ChunkData chunk = this.dataStore.getChunkAt(entity.getLocation());

        revokeIfNotPermitted(player, chunk, event, "interact with entities");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        ChunkData chunk = this.dataStore.getChunkAt(block.getLocation());

        revokeIfNotPermitted(player, chunk, event, "build");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        ChunkData chunk = this.dataStore.getChunkAt(block.getLocation());

        revokeIfNotPermitted(player, chunk, event, "build");
    }

    @EventHandler(priority = EventPriority.HIGH)
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        try {
            Block clickedBlock = event.getClickedBlock();
            ChunkData chunk = this.dataStore.getChunkAt(clickedBlock.getLocation());

            revokeIfNotPermitted(player, chunk, event, "build");
        } catch (NullPointerException e) {
            // now the catch block isn't empty
        }
    }
}
