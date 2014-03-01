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
    private static final Logger logger = Logger.getLogger("Minecraft");

    public DataManager dataStore;

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

        try {
            this.dataStore = new DataManager();
        } catch (Exception e) {
            addLogEntry("Unable to initialize the file system data store. Details:");
            addLogEntry(e.getMessage());
        }

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

                return handleChunkInfoCommand(player);

            } else if (args[0].equalsIgnoreCase("abandon")) {

                return handeChunkAbandon(player);

            } else if (args[0].equalsIgnoreCase("credits")) {

                return handleChunkCredits(player);

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

    private boolean handleChunkCredits(Player player) {
        PlayerData playerData = this.dataStore.readPlayerData(player.getName());
        sendMsg(player, "You have " + playerData.getCredits() + " credits.");
        return true;
    }

    private boolean handeChunkAbandon(Player player) {
        ChunkData chunk = this.dataStore.getChunkAt(player.getLocation());
        PlayerData playerData = this.dataStore.readPlayerData(player.getName());

        if (chunk == null) {
            sendMsg(player, "This chunk is public.");


        } else if (chunk.getOwnerName().equals(player.getName()) || player.hasPermission("chunkclaim.admin")) {
            this.dataStore.deleteChunk(chunk);
            playerData.addCredit();
            this.dataStore.savePlayerData(playerData);
            sendMsg(player, "ChunkData abandoned. Credits: " + playerData.getCredits());
            return true;

        } else {

            sendMsg(player, "You don't own this chunk. Only " + chunk.getOwnerName() + " or the staff can delete it.");
            return true;
        }
        return false;
    }

    private boolean handleChunkInfoCommand(Player player) {
        ChunkData chunk = this.dataStore.getChunkAt(player.getLocation());
        Location location = player.getLocation();

        if (player.hasPermission("chunkclaim.admin")) {
            sendMsg(player, "ID: " + location.getChunk().getX() + "," + location.getChunk().getZ());
            if (chunk != null && !chunk.getOwnerName().equals(player.getName())) {
                long loginDays = ((new Date()).getTime() - this.dataStore.readPlayerData(chunk.getOwnerName()).getLastLogin().getTime()) / (1000 * 60 * 60 * 24);
                sendMsg(player, "Last Login: " + loginDays + " days ago.");
                StringBuilder builders = new StringBuilder();
                for (String builder : chunk.getBuilderNames()) {
                    builders.append(builder);
                    builders.append(" ");
                }
                sendMsg(player, "Trusted Builders: " + builders.toString());
                sendMsg(player, chunk.getOwnerName() + " owns this chunk.");
                return true;
            }
        }

        if (chunk == null) {
            sendMsg(player, "This chunk is public.");
            return true;

        } else if (chunk.getOwnerName().equals(player.getName())) {
            if (chunk.getBuilderNames().size() > 0) {
                StringBuilder builders = new StringBuilder();
                for (String builder : chunk.getBuilderNames()) {
                    builders.append(builder);
                    builders.append(" ");
                }
                sendMsg(player, "You own this chunk.");
                sendMsg(player, "Trusted Builders: " + builders.toString());

            } else {
                sendMsg(player, "You own this chunk. Use /chunk trust <player> to add other builders.");
            }
            return true;

        } else {

            if (chunk.isTrusted(player.getName())) {
                sendMsg(player, chunk.getOwnerName() + " owns this chunk. You have build rights!");
            } else {

                sendMsg(player, chunk.getOwnerName() + " owns this chunk. You can't build here.");

            }
            return true;
        }
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

    public static void addLogEntry(String entry) {
        logger.info("ChunkClaim: " + entry);
    }

    public void sendMsg(Player player, String message) {
        player.sendMessage(ChatColor.YELLOW + message);
    }
}