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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.util.Vector;

import java.util.List;

class BlockEventHandler implements Listener {

    private final DataManager dataManager;

    public BlockEventHandler(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    //when a player breaks a block...
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        ChunkData chunk = dataManager.getChunkAt(location);

        if (chunk == null) {
            return;
        }

        if (!player.hasPermission("chunkclaim.admin") && !chunk.isTrusted(player.getName())) {

            ChunkClaim.plugin.sendMsg(player, "You don't have " + chunk.getOwnerName() + "'s permission to build here.");

            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        ChunkData chunk = dataManager.getChunkAt(location);

        if (chunk == null) {
            return;
        }

        if (!player.hasPermission("chunkclaim.admin") && !chunk.isTrusted(player.getName())) {
            player.sendMessage(ChatColor.YELLOW + "You don't have " + chunk.getOwnerName() + "'s permission to build here.");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockPistonExtend(BlockPistonExtendEvent event) {
        Block piston = event.getBlock();
        ChunkData pistonChunk = this.dataManager.getChunkAt(piston.getLocation());
        String pistonOwnerName = (pistonChunk == null) ? null : pistonChunk.getOwnerName();

        List<Block> blocks = event.getBlocks();
        if (blocks.size() == 0) {

            Block invadedBlock = piston.getRelative(event.getDirection());
            ChunkData invadedBlockChunk = this.dataManager.getChunkAt(invadedBlock.getLocation());
            String invadedBlockOwnerName = (invadedBlockChunk == null) ? null : invadedBlockChunk.getOwnerName();

            if (pistonOwnerName == null || invadedBlockOwnerName == null || (!invadedBlockChunk.isTrusted(pistonOwnerName))) {
                event.setCancelled(true);
                return;
            }
            return;
        }

        for (Block block : blocks) {
            ChunkData chunk = this.dataManager.getChunkAt(block.getLocation());
            if (chunk == null || !chunk.isTrusted(pistonOwnerName)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    //blocks theft by pulling blocks out of a claim (again pistons)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockPistonRetract(BlockPistonRetractEvent event) {

        //we only care about sticky pistons
        if (!event.isSticky()) return;

        //who owns the moving block, if anyone?
        ChunkData movingBlockChunk = this.dataManager.getChunkAt(event.getRetractLocation());
        if (movingBlockChunk == null) {
            event.setCancelled(true);
            return;
        }
        String movingBlockOwnerName = movingBlockChunk.getOwnerName();

        //who owns the piston, if anyone?
        ChunkData pistonChunk = this.dataManager.getChunkAt(event.getBlock().getLocation());
        if (pistonChunk == null) {
            event.setCancelled(true);
            return;
        }
        String pistonOwnerName = pistonChunk.getOwnerName();

        if ((!pistonChunk.isTrusted(movingBlockOwnerName) && !movingBlockChunk.isTrusted(pistonOwnerName))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockFromTo(BlockFromToEvent spreadEvent) {
        //from where?
        Block fromBlock = spreadEvent.getBlock();
        ChunkData fromChunk = this.dataManager.getChunkAt(fromBlock.getLocation());

        //where to?
        Block toBlock = spreadEvent.getToBlock();
        ChunkData toChunk = this.dataManager.getChunkAt(toBlock.getLocation());

        //if it's within the same claim or wilderness to wilderness, allow it
        if (fromChunk == toChunk) {
            return;
        }

        //block any spread into the wilderness from a claim
        if (fromChunk == null) {
            spreadEvent.setCancelled(true);
        }
    }

    //ensures dispensers can't be used to dispense a block(like water or lava) or item across a claim boundary
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onDispense(BlockDispenseEvent dispenseEvent) {
        //from where?
        Block fromBlock = dispenseEvent.getBlock();

        //to where?
        Vector velocity = dispenseEvent.getVelocity();
        int xChange = 0;
        int zChange = 0;
        if (Math.abs(velocity.getX()) > Math.abs(velocity.getZ())) {
            if (velocity.getX() > 0) {
                xChange = 1;
            } else {
                xChange = -1;
            }
        } else {
            if (velocity.getZ() > 0) {
                zChange = 1;
            } else {
                zChange = -1;
            }
        }

        Block toBlock = fromBlock.getRelative(xChange, 0, zChange);

        ChunkData fromChunk = this.dataManager.getChunkAt(fromBlock.getLocation());
        ChunkData toChunk = this.dataManager.getChunkAt(toBlock.getLocation());

        Material materialDispensed = dispenseEvent.getItem().getType();
        if (materialDispensed == Material.WATER_BUCKET || materialDispensed == Material.LAVA_BUCKET) {

            //wilderness is NOT OK
            if (fromChunk == null || toChunk == null) {
                dispenseEvent.setCancelled(true);
                return;
            }

            //within chunks is OK
            if (fromChunk == toChunk) return;

            //chunks are ok
            if (toChunk.isTrusted(fromChunk.getOwnerName())) {
                return;
            }

            //everything else is NOT OK
            dispenseEvent.setCancelled(true);
        }
    }
}

