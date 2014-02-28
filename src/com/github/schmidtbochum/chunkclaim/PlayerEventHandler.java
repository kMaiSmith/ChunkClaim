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

package com.github.schmidtbochum.chunkclaim;

import com.github.schmidtbochum.chunkclaim.Data.ChunkData;
import com.github.schmidtbochum.chunkclaim.Data.DataManager;
import com.github.schmidtbochum.chunkclaim.Data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class PlayerEventHandler implements Listener {

    private final DataManager dataStore;

    public PlayerEventHandler(DataManager dataStore) {
        this.dataStore = dataStore;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = this.dataStore.readPlayerData(player.getName());

        player.sendMessage(ChatColor.YELLOW +
                "Server Running " + ChatColor.DARK_RED + "ChunkClaim Beta" + ChatColor.YELLOW +
                ". Have fun and report any bugs to an admin");
        this.dataStore.savePlayerData(playerData);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = this.dataStore.readPlayerData(player.getName());

        this.dataStore.savePlayerData(playerData);
        this.dataStore.clearCachedPlayerData(player.getName());
    }

    private void revokeIfNotPermitted(Player player, ChunkData chunk, Cancellable cancellable) {
        if (chunk == null) {
            return;
        }
        if (!player.hasPermission("chunkclaim.admin") && !chunk.isTrusted(player.getName())) {
            cancellable.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW +
                    "You don't have " + chunk.getOwnerName() + "'s permission to build here.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        ChunkData chunk = this.dataStore.getChunkAt(entity.getLocation());

        revokeIfNotPermitted(player, chunk, event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        ChunkData chunk = this.dataStore.getChunkAt(block.getLocation());

        revokeIfNotPermitted(player, chunk, event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        ChunkData chunk = this.dataStore.getChunkAt(block.getLocation());

        revokeIfNotPermitted(player, chunk, event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        ChunkData chunk = this.dataStore.getChunkAt(clickedBlock.getLocation());

        revokeIfNotPermitted(player, chunk, event);
    }
}
