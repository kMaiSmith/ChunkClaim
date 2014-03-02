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

package com.kmaismith.ChunkClaim;

import com.kmaismith.ChunkClaim.Data.ChunkData;
import com.kmaismith.ChunkClaim.Data.DataManager;
import com.kmaismith.ChunkClaim.Data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class ChunkClaim extends JavaPlugin {
    public static ChunkClaim plugin;
    private ChunkClaimLogger logger; // = Logger.getLogger("Minecraft");
    private ChunkCommandHandler chunkCommandHandler; // = new ChunkCommandHandler(this, dataStore);

    private DataManager dataStore; // = new DataManager();

    ChunkClaim() {
        this.logger = new ChunkClaimLogger(Logger.getLogger("Minecraft"));
        this.dataStore = new DataManager(logger);
        this.chunkCommandHandler = new ChunkCommandHandler(this, this.dataStore);
    }

    ChunkClaim(Logger logger, DataManager dataManager) {
        this.logger = new ChunkClaimLogger(logger);
        this.dataStore = dataManager;
        this.chunkCommandHandler = new ChunkCommandHandler(this, dataManager);
    }

    public float config_maxCredits;
    public float config_startCredits;
    public float config_autoDeleteDays;

    public void onDisable() {
        Player[] players = this.getServer().getOnlinePlayers();
        for (Player player : players) {
            String playerName = player.getName();
            PlayerData playerData = this.dataStore.readPlayerData(playerName);
            this.dataStore.savePlayerData(playerData);
        }

        plugin = null;
    }

    public void onEnable() {
        plugin = this;

        // copy default config
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.config_startCredits = (float) this.getConfig().getDouble("startCredits");
        this.config_maxCredits = (float) this.getConfig().getDouble("maxCredits");
        this.config_autoDeleteDays = (float) this.getConfig().getDouble("autoDeleteDays");

        // register for events
        PluginManager pluginManager = this.getServer().getPluginManager();

        // register block events
        BlockEventHandler blockEventHandler = new BlockEventHandler(dataStore);
        pluginManager.registerEvents(blockEventHandler, this);

        // register player events
        PlayerEventHandler playerEventHandler = new PlayerEventHandler(dataStore);
        pluginManager.registerEvents(playerEventHandler, this);

        // register entity events
        EntityEventHandler entityEventHandler = new EntityEventHandler(dataStore);
        pluginManager.registerEvents(entityEventHandler, this);
    }

    // handles slash commands
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player = null;

        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (cmd.getName().equalsIgnoreCase("chunk") && player != null) {
            if (args.length == 0) {

                return chunkCommandHandler.handleChunkInfoCommand(player);

            } else if (args[0].equalsIgnoreCase("abandon")) {

                return chunkCommandHandler.handeChunkAbandon(player);

            } else if (args[0].equalsIgnoreCase("credits")) {

                return chunkCommandHandler.handleChunkCredits(player);

            } else if (args[0].equalsIgnoreCase("trust")) {

                PlayerData playerData = this.dataStore.readPlayerData(player.getName());

                if (args.length != 2) {
                    sendMsg(player, "Usage: /chunk trust <player>");
                    return true;
                }

                OfflinePlayer tp = resolvePlayer(args[1]);
                if (tp == null) {
                    sendMsg(player, "Player not found.");
                    return true;
                }
                String tName = tp.getName();
                if (tName.equals(player.getName())) {
                    sendMsg(player, "You don't trust yourself?");
                    return true;
                }

                ArrayList<ChunkData> chunksInRadius = this.dataStore.getChunksForPlayer(player.getName());

                if (!playerData.getBuilderNames().contains(tName)) {
                    for (ChunkData chunkPlot : chunksInRadius) {
                        if (!chunkPlot.isTrusted(tName)) {
                            chunkPlot.addBuilder(tName);
                            dataStore.writeChunkToStorage(chunkPlot);
                        }
                    }
                    playerData.getBuilderNames().add(tName);
                    this.dataStore.savePlayerData(playerData);
                }
                sendMsg(player, "Trusted " + tName + " in all your chunks.");
                return true;

            }
            //UNTRUST
            else if (args[0].equalsIgnoreCase("untrust")) {
                PlayerData playerData = this.dataStore.readPlayerData(player.getName());

                if (args.length != 2) {
                    sendMsg(player, "Usage: /chunk untrust <player>");
                    return true;
                }

                OfflinePlayer tp = resolvePlayer(args[1]);
                if (tp == null) {
                    sendMsg(player, "Player not found.");
                    return true;
                }

                String tName = tp.getName();
                if (tName.equals(player.getName())) {
                    sendMsg(player, "You don't trust yourself?");
                    return true;
                }

                ArrayList<ChunkData> chunksInRadius = this.dataStore.getChunksForPlayer(player.getName());

                if (playerData.getBuilderNames().contains(tName)) {
                    for (ChunkData chunkPlot : chunksInRadius) {
                        chunkPlot.removeBuilder(tName);
                        dataStore.writeChunkToStorage(chunkPlot);
                    }
                    playerData.getBuilderNames().remove(tName);
                    this.dataStore.savePlayerData(playerData);

                }

                sendMsg(player, "Untrusted " + tName + " in all your chunks.");
                return true;

            } else if (args[0].equalsIgnoreCase("deleteall")) {
                if (!player.hasPermission("chunkclaim.admin")) {
                    sendMsg(player, "No permission.");
                    return true;
                }
                if (args.length == 2) {
                    OfflinePlayer tp = resolvePlayer(args[1]);
                    if (tp == null) {

                        sendMsg(player, "Player not found.");
                        return true;
                    }
                    String tName = tp.getName();

                    sendMsg(player, dataStore.deleteChunksForPlayer(tName) + " chunks deleted.");
                    return true;
                } else {
                    sendMsg(player, "Usage: /chunk deleteall <player>");
                    return true;
                }

            } else if (args[0].equalsIgnoreCase("claim")) {
                if (args.length == 1) {

                    Location location = player.getLocation();

                    PlayerData playerData = dataStore.readPlayerData(player.getName());
                    ChunkData chunk = dataStore.getChunkAt(location);

                    String playerName = player.getName();

                    if (chunk == null) {
                        if (playerData.getCredits() > 0) {

                            ChunkData newChunk = new ChunkData(location.getChunk(), playerName, playerData.getBuilderNames());

                            this.dataStore.addChunk(newChunk);

                            playerData.subtractCredit();
                            this.dataStore.savePlayerData(playerData);

                            sendMsg(player, "You claimed this chunk. Credits left: " + playerData.getCredits());

                        } else {

                            sendMsg(player, "Not enough credits to claim this chunk.");

                        }
                        return true;
                    } else {
                        sendMsg(player, "This chunk is not public.");
                    }
                } else {
                    sendMsg(player, "Usage: /chunk claim");
                    return true;
                }

            } else if (args[0].equalsIgnoreCase("list")) {

                if (args.length == 1) {
                    String tName = player.getName();

                    ArrayList<ChunkData> chunksInRadius = this.dataStore.getChunksForPlayer(tName);

                    String adminString = "Here are your chunks:";
                    sendMsg(player, adminString);

                    for (ChunkData chunkPlot : chunksInRadius) {
                        adminString = "ID: " + chunkPlot.getChunkX() + "|" + chunkPlot.getChunkZ() + ", World Location: " + (chunkPlot.getChunkX() * 16) + "|" + (chunkPlot.getChunkZ() * 16);

                        sendMsg(player, adminString);


                    }
                    return true;
                } else if (args.length == 2 && player.hasPermission("chunkclaim.admin")) {

                    OfflinePlayer tp = resolvePlayer(args[1]);
                    if (tp == null) {

                        sendMsg(player, "Player not found.");
                        return true;
                    }
                    String tName = args[1];

                    ArrayList<ChunkData> chunksInRadius = this.dataStore.getChunksForPlayer(tName);

                    long loginDays = ((new Date()).getTime() - this.dataStore.readPlayerData(tName).getLastLogin().getTime()) / (1000 * 60 * 60 * 24);
                    long joinDays = ((new Date()).getTime() - this.dataStore.readPlayerData(tName).getFirstJoin().getTime()) / (1000 * 60 * 60 * 24);
                    String adminString = tName + " | Last Login: " + loginDays + " days ago. First Join: " + joinDays + " days ago.";
                    sendMsg(player, adminString);

                    for (ChunkData chunkPlot : chunksInRadius) {
                        adminString = "ID: " + chunkPlot.getChunkX() + "|" + chunkPlot.getChunkZ() + ", World Location: " + (chunkPlot.getChunkX() * 16) + "|" + (chunkPlot.getChunkZ() * 16);

                        sendMsg(player, adminString);


                    }
                    return true;

                } else {
                    return false;
                }
            }
        }
        return false;
    }

    OfflinePlayer resolvePlayer(String name) {

        Player player = this.getServerWrapper().getPlayer(name);
        if (player != null)
            return player;

        // then search offline players
        return this.getServerWrapper().getOfflinePlayer(name);

    }

    Server getServerWrapper() {
        return this.getServer();
    }

    public void sendMsg(Player player, String message) {
        player.sendMessage(ChatColor.YELLOW + message);
    }
}