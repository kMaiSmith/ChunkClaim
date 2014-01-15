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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;

import java.util.Date;

class PlayerEventHandler implements Listener {

    private final IDataStore dataStore;

    public PlayerEventHandler(IDataStore dataStore) {
        this.dataStore = dataStore;
    }

    //when a player successfully joins the server...
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerJoin(PlayerJoinEvent event) {

        String playerName = event.getPlayer().getName();

        //note login time
        PlayerData playerData = this.dataStore.getPlayerData(playerName);
        playerData.lastLogin = new Date();

        if (playerData.firstJoin == null)
            playerData.firstJoin = new Date();
        //if (!event.getPlayer().hasPlayedBefore())
        //ChunkClaim.plugin.broadcast(ChatColor.LIGHT_PURPLE + "[GLaDOS] " + ChatColor.GREEN +"Welcome " + playerName + ChatColor.GREEN +" to The Colony!");

        //event.getPlayer().sendMessage(ChatColor.DARK_RED + "Running ChunkClaim Alpha. ONLY FOR TESTING!");
        this.dataStore.savePlayerData(playerName, playerData);
    }

    //when a player quits...
    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = this.dataStore.getPlayerData(player.getName());

        //make sure his data is all saved
        this.dataStore.savePlayerData(player.getName(), playerData);

        //drop data about this player
        this.dataStore.clearCachedPlayerData(player.getName());
    }

    //when a player interacts with an entity...
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if (!ChunkClaim.plugin.config_worlds.contains(event.getPlayer().getLocation().getWorld().getName())) return;

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        ChunkPlot chunk = this.dataStore.getChunkAt(entity.getLocation(), null);

        if (chunk != null) {
            if (entity instanceof StorageMinecart || entity instanceof PoweredMinecart || entity instanceof Animals) {
                if (!chunk.isTrusted(player.getName())) {
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

        ChunkPlot chunk = this.dataStore.getChunkAt(block.getLocation(), null);

        if (chunk != null) {
            if (!chunk.isTrusted(player.getName())) {
                ChunkClaim.plugin.sendMsg(player, "Not permitted.");
                bedEvent.setCancelled(true);
            }
        }
    }

    //block use of buckets within other players' claims
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent bucketEvent) {
        if (!ChunkClaim.plugin.config_worlds.contains(bucketEvent.getBlockClicked().getWorld().getName())) {
            return;
        }

        Player player = bucketEvent.getPlayer();
        Block block = bucketEvent.getBlockClicked().getRelative(bucketEvent.getBlockFace());

        ChunkPlot chunk = this.dataStore.getChunkAt(block.getLocation(), null);

        if (chunk == null) {
            bucketEvent.setCancelled(true);
            return;
        }
        if (!chunk.isTrusted(player.getName())) {
            ChunkClaim.plugin.sendMsg(player, "You don't have " + chunk.getOwnerName() + "'s permission to build here.");
            bucketEvent.setCancelled(true);
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerBucketFill(PlayerBucketFillEvent bucketEvent) {

        if (!ChunkClaim.plugin.config_worlds.contains(bucketEvent.getBlockClicked().getWorld().getName())) return;

        Player player = bucketEvent.getPlayer();
        Block block = bucketEvent.getBlockClicked();
        ChunkPlot chunk = this.dataStore.getChunkAt(block.getLocation(), null);

        if (chunk == null) {
            bucketEvent.setCancelled(true);
            return;
        }
        if (!chunk.isTrusted(player.getName())) {
            ChunkClaim.plugin.sendMsg(player, "You don't have " + chunk.getOwnerName() + "'s permission.");
            bucketEvent.setCancelled(true);
        }
    }

    //when a player drops an item
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (!ChunkClaim.plugin.config_worlds.contains(event.getPlayer().getWorld().getName())) return;

        Item item = event.getItemDrop();
        Material material = item.getItemStack().getType();

        if (!(material == Material.WRITTEN_BOOK || material == Material.BOOK_AND_QUILL)) {
            event.setCancelled(true);
        }

    }

    //when a player interacts with the world
    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerInteract(PlayerInteractEvent event) {

        if (!ChunkClaim.plugin.config_worlds.contains(event.getPlayer().getWorld().getName())) return;

        Player player = event.getPlayer();

        //determine target block. FEATURE: shovel and string can be used from a distance away
        Block clickedBlock;

        try {
            clickedBlock = event.getClickedBlock(); //null returned here means interacting with air
        } catch (Exception e) {//an exception intermittently comes from getTargetBlock(). when it does, just ignore the event
            return;
        }

        //if no block, stop here
        if (clickedBlock == null) {
            return;
        }

        Material clickedBlockType = clickedBlock.getType();

        //apply rules for putting out fires (requires build permission)
        PlayerData playerData = this.dataStore.getPlayerData(player.getName());

        if (event.getClickedBlock() != null && event.getClickedBlock().getRelative(event.getBlockFace()).getType() == Material.FIRE) {
            ChunkPlot chunk = this.dataStore.getChunkAt(clickedBlock.getLocation(), playerData.lastChunk);

            if (chunk != null) {
                playerData.lastChunk = chunk;
                if (!chunk.isTrusted(player.getName())) {
                    event.setCancelled(true);
                    ChunkClaim.plugin.sendMsg(player, "You don't have " + chunk.getOwnerName() + "'s permission to build here.");
                }
            }
        }
        //apply rules for containers and crafting blocks
        else if (ChunkClaim.plugin.config_protectContainers && (clickedBlock.getState() instanceof InventoryHolder || clickedBlockType == Material.WORKBENCH || clickedBlockType == Material.ENDER_CHEST || clickedBlockType == Material.DISPENSER || clickedBlockType == Material.BREWING_STAND || clickedBlockType == Material.JUKEBOX || clickedBlockType == Material.ENCHANTMENT_TABLE)) {
            ChunkPlot chunk = this.dataStore.getChunkAt(clickedBlock.getLocation(), playerData.lastChunk);
            if (chunk != null) {
                playerData.lastChunk = chunk;
                if (!chunk.isTrusted(player.getName())) {
                    event.setCancelled(true);
                    ChunkClaim.plugin.sendMsg(player, "Not permitted.");
                }
            }
        }
        //otherwise apply rules for buttons and switches
        else if (ChunkClaim.plugin.config_protectSwitches && (clickedBlockType == null || clickedBlockType == Material.STONE_BUTTON || clickedBlockType == Material.LEVER)) {
            ChunkPlot chunk = this.dataStore.getChunkAt(clickedBlock.getLocation(), playerData.lastChunk);
            if (chunk != null) {
                playerData.lastChunk = chunk;
                if (!chunk.isTrusted(player.getName())) {
                    event.setCancelled(true);
                    ChunkClaim.plugin.sendMsg(player, "Not permitted.");
                }
            }

        }
        //apply rule for players trampling tilled soil back to dirt (never allow it)
        //NOTE: that this event applies only to players. monsters and animals can still trample.
        else if (event.getAction() == Action.PHYSICAL && clickedBlockType == Material.SOIL) {
            event.setCancelled(true);
        }

        //apply rule for note blocks and repeaters
        else if (clickedBlockType == Material.NOTE_BLOCK || clickedBlockType == Material.DIODE_BLOCK_ON || clickedBlockType == Material.DIODE_BLOCK_OFF) {
            ChunkPlot chunk = this.dataStore.getChunkAt(clickedBlock.getLocation(), playerData.lastChunk);
            if (chunk != null) {
                playerData.lastChunk = chunk;
                if (!chunk.isTrusted(player.getName())) {
                    event.setCancelled(true);
                    ChunkClaim.plugin.sendMsg(player, "Not permitted.");
                }
            }
        }
        //otherwise handle right click (shovel, string, bonemeal)
        else {
            //ignore all actions except right-click on a block or in the air
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

            //what's the player holding?
            Material materialInHand = player.getItemInHand().getType();

            //check for build permission (ink sac == bone meal, must be a Bukkit bug?)
            if (materialInHand == Material.INK_SACK || materialInHand == Material.BOAT || materialInHand == Material.MINECART || materialInHand == Material.POWERED_MINECART || materialInHand == Material.STORAGE_MINECART || materialInHand == Material.BOAT) {
                ChunkPlot chunk = this.dataStore.getChunkAt(clickedBlock.getLocation(), playerData.lastChunk);
                if (chunk != null) {
                    playerData.lastChunk = chunk;
                    if (!chunk.isTrusted(player.getName())) {
                        event.setCancelled(true);
                        ChunkClaim.plugin.sendMsg(player, "Not permitted.");
                    }
                } else {
                    event.setCancelled(true);
                    ChunkClaim.plugin.sendMsg(player, "Not permitted.");
                }
            } else if (materialInHand == Material.MONSTER_EGG) {
                if (ChunkClaim.plugin.config_mobsForCredits && (player.getItemInHand().getDurability() == EntityType.WOLF.getTypeId() || player.getItemInHand().getDurability() == EntityType.OCELOT.getTypeId())) {

                    ChunkPlot chunk = this.dataStore.getChunkAt(clickedBlock.getLocation(), playerData.lastChunk);
                    if (chunk != null) {
                        playerData.lastChunk = chunk;
                        if (!chunk.isTrusted(player.getName())) {
                            event.setCancelled(true);
                            ChunkClaim.plugin.sendMsg(player, "Not permitted.");
                        } else {
                            if (playerData.getCredits() >= ChunkClaim.plugin.config_mobPrice) {
                                playerData.credits -= 30;
                                ChunkClaim.plugin.sendMsg(player, "You spawned this mob for " + ChunkClaim.plugin.config_mobPrice + " credits. Credits left: " + playerData.getCredits());

                            } else {
                                event.setCancelled(true);
                                ChunkClaim.plugin.sendMsg(player, "Not enough credits to spawn a mob (" + ChunkClaim.plugin.config_mobPrice + ").");
                            }
                        }
                    } else {
                        event.setCancelled(true);
                        ChunkClaim.plugin.sendMsg(player, "Not permitted.");
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

}
