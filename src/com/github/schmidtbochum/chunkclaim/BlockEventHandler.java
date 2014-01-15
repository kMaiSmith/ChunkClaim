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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

class BlockEventHandler implements Listener {

    private final DataManager dataManager;

    public BlockEventHandler(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    //when a player breaks a block...
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!ChunkClaim.plugin.config_worlds.contains(event.getBlock().getWorld().getName())) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        PlayerData playerData = this.dataManager.readPlayerData(player.getName());
        ChunkData chunk = dataManager.getChunkAt(location);

        if (playerData.ignoreChunks) {
            return;
        }

        if (chunk == null) {
            String playerName = player.getName();

            if (!player.hasPermission("chunkclaim.claim")) {
                ChunkClaim.plugin.sendMsg(player, "You don't have permissions for claiming chunks.");
                event.setCancelled(true);
                return;
            }
            if (!dataManager.ownsNear(location, playerName)) {
                if (!ChunkClaim.plugin.config_nextToForce && !player.hasPermission("chunkclaim.admin")) {
                    ChunkClaim.plugin.sendMsg(player, "You don't own a chunk next to this one.");
                    ChunkClaim.plugin.sendMsg(player, "Confirm with /chunk claim. Please don't spam claimed chunks.");
                } else {
                    ArrayList<ChunkData> playerChunks = ChunkClaim.plugin.dataStore.getChunksForPlayer(playerName);
                    if (playerChunks.size() > 0) {
                        ChunkClaim.plugin.sendMsg(player, "You can only build next to your first claimed chunk.");
                    } else {
                        ChunkClaim.plugin.sendMsg(player, "You don't own a chunk next to this one.");
                        ChunkClaim.plugin.sendMsg(player, "Confirm with /chunk claim. Please don't spam claimed chunks.");
                    }
                }
                event.setCancelled(true);
                Visualization visualization = Visualization.FromBukkitChunk(location.getChunk(), location.getBlockY(), VisualizationType.Public, location);
                Visualization.Apply(player, visualization);
            } else if (playerData.getCredits() > 0) {
                ChunkData newChunk = new ChunkData(location.getChunk(), playerName, playerData.builderNames);

                this.dataManager.addChunk(newChunk);

                playerData.credits--;
                playerData.lastChunk = newChunk;
                this.dataManager.savePlayerData(playerName, playerData);

                ChunkClaim.plugin.sendMsg(player, "You claimed this chunk. Credits left: " + playerData.getCredits());

                Visualization visualization = Visualization.FromChunk(newChunk, location.getBlockY(), VisualizationType.Chunk, location);
                Visualization.Apply(player, visualization);

            } else {
                ChunkClaim.plugin.sendMsg(player, "Not enough credits to claim this chunk.");

                if (playerData.lastChunk != chunk) {
                    playerData.lastChunk = chunk;
                    Visualization visualization = Visualization.FromBukkitChunk(location.getChunk(), location.getBlockY(), VisualizationType.Public, location);
                    Visualization.Apply(player, visualization);
                }
                event.setCancelled(true);
            }
        } else {
            ChunkClaim.plugin.sendMsg(player, "You don't have " + chunk.getOwnerName() + "'s permission to build here.");
            if (playerData.lastChunk != chunk) {
                playerData.lastChunk = chunk;
                Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.ErrorChunk, location);
                Visualization.Apply(player, visualization);
            }
            event.setCancelled(true);
        }
    }

    //when a player places a block...
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (!ChunkClaim.plugin.config_worlds.contains(event.getBlock().getWorld().getName())) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        PlayerData playerData = this.dataManager.readPlayerData(player.getName());
        ChunkData chunk = dataManager.getChunkAt(location);

        if (playerData.ignoreChunks) {
            return;
        }

        if (chunk == null) {

            if (!player.hasPermission("chunkclaim.claim")) {
                ChunkClaim.plugin.sendMsg(player, "You don't have permissions for claiming chunks.");
                event.setCancelled(true);
            }

            String playerName = player.getName();

            //prevent fire spam
            if (block.getType() == Material.FIRE) {
                event.setCancelled(true);
                return;
            }
            //prevent tree spam
            if (block.getType() == Material.SAPLING) {
                ChunkClaim.plugin.sendMsg(player, "Please dont spam chunks with trees.");
                event.setCancelled(true);
                return;
            }
            if (!dataManager.ownsNear(location, playerName)) {
                if (!ChunkClaim.plugin.config_nextToForce && !player.hasPermission("chunkclaim.admin")) {
                    ChunkClaim.plugin.sendMsg(player, "You don't own a chunk next to this one.");
                    ChunkClaim.plugin.sendMsg(player, "Confirm with /chunk claim. Please don't spam claimed chunks.");
                } else {
                    ArrayList<ChunkData> playerChunks = ChunkClaim.plugin.dataStore.getChunksForPlayer(playerName);
                    if (playerChunks.size() > 0) {
                        ChunkClaim.plugin.sendMsg(player, "You can only build next to your first claimed chunk.");
                    } else {
                        ChunkClaim.plugin.sendMsg(player, "You don't own a chunk next to this one.");
                        ChunkClaim.plugin.sendMsg(player, "Confirm with /chunk claim. Please don't spam claimed chunks.");
                    }
                }
                event.setCancelled(true);
                Visualization visualization = Visualization.FromBukkitChunk(location.getChunk(), location.getBlockY(), VisualizationType.Public, location);
                Visualization.Apply(player, visualization);
                return;
            } else {
                //check credits
                if (playerData.getCredits() <= 0) {
                    ChunkClaim.plugin.sendMsg(player, "Not enough credits to claim this chunk.");

                    Visualization visualization = Visualization.FromBukkitChunk(location.getChunk(), location.getBlockY(), VisualizationType.Public, location);
                    Visualization.Apply(player, visualization);

                    event.setCancelled(true);
                    return;
                }
            }
            //claim the chunk
            ChunkData newChunk = new ChunkData(location.getChunk(), playerName, playerData.builderNames);

            this.dataManager.addChunk(newChunk);

            playerData.credits--;
            playerData.lastChunk = newChunk;
            newChunk.modify();
            this.dataManager.savePlayerData(playerName, playerData);

            ChunkClaim.plugin.sendMsg(player, "You claimed this chunk. Credits left: " + playerData.getCredits());

            Visualization visualization = Visualization.FromChunk(newChunk, location.getBlockY(), VisualizationType.Chunk, location);
            Visualization.Apply(player, visualization);

        } else if (chunk.isTrusted(player.getName())) {
            chunk.modify();

        } else {
            ChunkClaim.plugin.sendMsg(player, "You don't have " + chunk.getOwnerName() + "'s permission to build here.");

            if (playerData.lastChunk != chunk) {
                playerData.lastChunk = chunk;
                Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.ErrorChunk, location);
                Visualization.Apply(player, visualization);
            }
            event.setCancelled(true);
        }
    }

    //blocks "pushing" other players' blocks around (pistons)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {

        if (!ChunkClaim.plugin.config_worlds.contains(event.getBlock().getWorld().getName())) return;

        List<Block> blocks = event.getBlocks();

        Block piston = event.getBlock();
        ChunkData pistonChunk = this.dataManager.getChunkAt(piston.getLocation());
        String pistonOwnerName = (pistonChunk == null) ? null : pistonChunk.getOwnerName();

        //if no blocks moving, then only check to make sure we're not pushing into a claim from outside
        //this avoids pistons breaking non-solids just inside a claim, like torches, doors, and touchplates
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


        ChunkData chunk;
        //which blocks are being pushed?
        for (Block block : blocks) {
            //if ANY of the pushed blocks are owned by someone other than the piston owner, cancel the event
            chunk = this.dataManager.getChunkAt(block.getLocation());
            if (chunk == null || !chunk.isTrusted(pistonOwnerName)) {
                event.setCancelled(true);
                event.getBlock().getWorld().createExplosion(event.getBlock().getLocation(), 0);
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(event.getBlock().getType()));
                event.getBlock().setType(Material.AIR);
                return;
            }
        }

        Block block = blocks.get(blocks.size() - 1).getRelative(event.getDirection());

        chunk = this.dataManager.getChunkAt(block.getLocation());
        if (chunk == null || !chunk.isTrusted(pistonOwnerName)) {
            event.setCancelled(true);
            event.getBlock().getWorld().createExplosion(event.getBlock().getLocation(), 0);
            event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(event.getBlock().getType()));
            event.getBlock().setType(Material.AIR);
        }
    }

    //blocks theft by pulling blocks out of a claim (again pistons)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {

        if (!ChunkClaim.plugin.config_worlds.contains(event.getBlock().getWorld().getName())) return;

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

    //blocks are ignited ONLY by flint and steel
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockIgnite(BlockIgniteEvent igniteEvent) {

        if (!ChunkClaim.plugin.config_worlds.contains(igniteEvent.getBlock().getWorld().getName())) {
            return;
        }

        if (igniteEvent.getCause() != IgniteCause.FLINT_AND_STEEL) {
            igniteEvent.setCancelled(true);
        }
    }

    //fire doesn't spread, but other blocks still do (mushrooms and vines, for example)
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockSpread(BlockSpreadEvent spreadEvent) {

        if (!ChunkClaim.plugin.config_worlds.contains(spreadEvent.getBlock().getWorld().getName())) {
            return;
        }

        if (spreadEvent.getSource().getType() == Material.FIRE) {
            spreadEvent.setCancelled(true);
        }
    }

    //blocks are not destroyed by fire
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBurn(BlockBurnEvent burnEvent) {

        if (!ChunkClaim.plugin.config_worlds.contains(burnEvent.getBlock().getWorld().getName())) {
            return;
        }

        burnEvent.setCancelled(true);
    }

    //ensures fluids don't flow out of chunks, unless into another chunk where the owner is trusted to build
    private ChunkData lastSpreadChunk = null;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockFromTo(BlockFromToEvent spreadEvent) {

        if (!ChunkClaim.plugin.config_worlds.contains(spreadEvent.getBlock().getWorld().getName())) {
            return;
        }

        //always allow fluids to flow straight down
        if (spreadEvent.getFace() == BlockFace.DOWN) {
            return;
        }

        //from where?
        Block fromBlock = spreadEvent.getBlock();
        ChunkData fromChunk = this.dataManager.getChunkAt(fromBlock.getLocation());
        if (fromChunk != null) {
            this.lastSpreadChunk = fromChunk;
        }

        //where to?
        Block toBlock = spreadEvent.getToBlock();
        ChunkData toChunk = this.dataManager.getChunkAt(toBlock.getLocation());

        //if it's within the same claim or wilderness to wilderness, allow it
        if (fromChunk == toChunk) {
            return;
        }

        //block any spread into the wilderness from a claim
        if (fromChunk != null && toChunk == null) {
            spreadEvent.setCancelled(true);
        }
        //if spreading into a claim
        else {
            //who owns the spreading block, if anyone?
            String fromOwner = null;
            if (fromChunk != null) {
                fromOwner = fromChunk.getOwnerName();
            }

            //cancel unless the owner of the spreading block is allowed to build in the receiving claim
            if (fromOwner == null || !toChunk.isTrusted(fromOwner)) {
                spreadEvent.setCancelled(true);
            }
        }
    }

    //ensures dispensers can't be used to dispense a block(like water or lava) or item across a claim boundary
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onDispense(BlockDispenseEvent dispenseEvent) {

        if (!ChunkClaim.plugin.config_worlds.contains(dispenseEvent.getBlock().getWorld().getName())) return;

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

    @EventHandler(ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent growEvent) {

        if (!ChunkClaim.plugin.config_worlds.contains(growEvent.getLocation().getBlock().getWorld().getName())) {
            return;
        }

        Location rootLocation = growEvent.getLocation();
        ChunkData rootChunk = this.dataManager.getChunkAt(rootLocation);
        String rootOwnerName = (rootChunk == null) ? null : rootChunk.getOwnerName();

        //for each block growing
        for (int i = 0; i < growEvent.getBlocks().size(); i++) {
            BlockState block = growEvent.getBlocks().get(i);
            ChunkData blockChunk = this.dataManager.getChunkAt(block.getLocation());
            if (blockChunk != null) {
                if (rootOwnerName == null || !blockChunk.isTrusted(rootOwnerName)) {
                    growEvent.getBlocks().remove(i--);
                }
            } else if (rootOwnerName != null) {
                growEvent.getBlocks().remove(i--);
            }
        }
    }
}

