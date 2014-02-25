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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

class PlayerEventHandler implements Listener {

    private final DataManager dataStore;

    public PlayerEventHandler(DataManager dataStore) {
        this.dataStore = dataStore;
    }

    //when a player successfully joins the server...
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerJoin(PlayerJoinEvent event) {

        String playerName = event.getPlayer().getName();

        //note login time
        PlayerData playerData = this.dataStore.readPlayerData(playerName);

        //if (!event.getPlayer().hasPlayedBefore())
        //ChunkClaim.plugin.broadcast(ChatColor.LIGHT_PURPLE + "[GLaDOS] " + ChatColor.GREEN +"Welcome " + playerName + ChatColor.GREEN +" to The Colony!");

        event.getPlayer().sendMessage(ChatColor.DARK_RED + "Running ChunkClaim Alpha. ONLY FOR TESTING!");
        this.dataStore.savePlayerData(playerData);
    }

    //when a player quits...
    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = this.dataStore.readPlayerData(player.getName());

        //make sure his data is all saved
        this.dataStore.savePlayerData(playerData);

        //drop data about this player
        this.dataStore.clearCachedPlayerData(player.getName());
    }

    //when a player interacts with an entity...
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        ChunkData chunk = this.dataStore.getChunkAt(entity.getLocation());

        if (chunk != null) {
            if (entity instanceof StorageMinecart || entity instanceof PoweredMinecart || entity instanceof Animals) {
                if (!(player.isOp() || player.hasPermission("chunkclaim.admin")) && !chunk.isTrusted(player.getName())) {
                    ChunkClaim.plugin.sendMsg(player, "Not permitted.");
                    event.setCancelled(true);
                }

            }

        }
    }

    //block players from entering beds they don't have permission for
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerBedEnter(PlayerBedEnterEvent bedEvent) {
        Player player = bedEvent.getPlayer();
        Block block = bedEvent.getBed();

        ChunkData chunk = this.dataStore.getChunkAt(block.getLocation());

        if (chunk != null) {
            if (!player.isOp() && !chunk.isTrusted(player.getName())) {
                ChunkClaim.plugin.sendMsg(player, "Not permitted.");
                bedEvent.setCancelled(true);
            }
        }
    }

    //block use of buckets within other players' claims
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent bucketEvent) {
        Player player = bucketEvent.getPlayer();
        Block block = bucketEvent.getBlockClicked().getRelative(bucketEvent.getBlockFace());

        ChunkData chunk = this.dataStore.getChunkAt(block.getLocation());

        if (chunk == null) {
            return;
        }
        if (!(player.isOp() || player.hasPermission("chunkclaim.admin")) && !chunk.isTrusted(player.getName())) {
            ChunkClaim.plugin.sendMsg(player, "You don't have " + chunk.getOwnerName() + "'s permission to build here.");
            bucketEvent.setCancelled(true);
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerBucketFill(PlayerBucketFillEvent bucketEvent) {
        Player player = bucketEvent.getPlayer();
        Block block = bucketEvent.getBlockClicked();
        ChunkData chunk = this.dataStore.getChunkAt(block.getLocation());

        if (chunk == null) {
            return;
        }
        if (!(player.isOp() || player.hasPermission("chunkclaim.admin")) && !chunk.isTrusted(player.getName())) {
            ChunkClaim.plugin.sendMsg(player, "You don't have " + chunk.getOwnerName() + "'s permission.");
            bucketEvent.setCancelled(true);
        }
    }

    //when a player interacts with the world
    @EventHandler(priority = EventPriority.HIGH)
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        //determine target block. FEATURE: shovel and string can be used from a distance away
        Block clickedBlock;

        try {
            clickedBlock = event.getClickedBlock(); //null returned here means interacting with air
        } catch (Exception e) {//an exception intermittently comes from getTargetBlock(). when it does, just ignore the event
            return;
        }

        ChunkData chunk = clickedBlock != null ? this.dataStore.getChunkAt(clickedBlock.getLocation()) : null;

        if (chunk != null) {
            if (!(player.isOp() || player.hasPermission("chunkclaim.admin")) && !chunk.isTrusted(player.getName())) {
                event.setCancelled(true);
                ChunkClaim.plugin.sendMsg(player, "You don't have " + chunk.getOwnerName() + "'s permission to build here.");
            }
        }
    }
}
