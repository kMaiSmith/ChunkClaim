/*
    ChunkClaim Plugin for Minecraft Bukkit Servers
    Copyright (C) 2012 Felix Schmidt
    Based on code by Felix Schmidt, Copyright (C) 2014 Kyle Smith
    
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

    Contact: Kyle Smith <kMaiSmith@gmail.com>
 */

package com.kmaismith.chunkclaim;

import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import com.kmaismith.chunkclaim.Data.ChunkData;
import com.kmaismith.chunkclaim.Data.DataManager;
import com.kmaismith.chunkclaim.Data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class ChunkClaim extends JavaPlugin {
    public static ChunkClaim plugin;
    private ChunkClaimLogger logger;
    private DataManager dataStore;
    private ChunkCommandHandler chunkCommandHandler;
    private transient PriceIndex priceIndex;

    public ChunkClaim() {
        super();
        this.logger = new ChunkClaimLogger(Logger.getLogger("Minecraft"));
        this.dataStore = new DataManager(logger);
        this.chunkCommandHandler = new ChunkCommandHandler(this, dataStore);
    }

    // Sadness constructor (for tests) :'(
    ChunkClaim(Logger logger, DataManager dataManager) {
        super();
        this.logger = new ChunkClaimLogger(logger);
        this.dataStore = dataManager;
        this.chunkCommandHandler = new ChunkCommandHandler(this, dataManager);
    }

    public float config_startCredits;
    public int config_maxCredits;

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
        this.config_maxCredits = this.getConfig().getInt("maxCredits");
        
        // price index
        priceIndex = new PriceIndex(this.getDataFolder());

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

                chunkCommandHandler.handleChunkInfoCommand(player);
                return true;

            } else if (args[0].equalsIgnoreCase("abandon")) {
                return chunkCommandHandler.handleChunkAbandon(player, args);

            } else if (args[0].equalsIgnoreCase("credits")) {

                chunkCommandHandler.handleChunkCredits(player);
                return true;

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
                }

            } else if (args[0].equalsIgnoreCase("buy")) {
                int total = dataStore.readPlayerData(player.getName()).getCredits() + dataStore.getChunksForPlayer(player.getName()).size();
                if(total >= this.config_maxCredits && !player.hasPermission("chunkclaim.admin")) {
                    sendMsg(player, "You have reached the maximum number of chunks allowed.");
                    sendMsg(player, "If you need more, you will have to consult a server operator.");
                    return true;
                } else {
                    // Many thanks to msoulworrier for contributions.
                    // We highly recommend: config_startCredits=4 and 25<=config_maxCredits<=30
                    // let x be the number of chunks
                    // let m be the price index
                    // let f(x) be the price, relative to the price index
                    // f(x)=me^{.753(.6542x)}+4
                    BigDecimal reqBal = new BigDecimal(Math.pow(2.71828,  (0.4926126 * total) + 4)).multiply(new BigDecimal(priceIndex.getPI()));
                    boolean hasEnough;
                    try {
                        hasEnough = com.earth2me.essentials.api.Economy.hasEnough(player.getName(), reqBal);
                    } catch (com.earth2me.essentials.api.UserDoesNotExistException e) {
                        sendMsg(player, "Internal error: UserDoesNotExistException. No transaction was made. Please report.");
                        return true; // Should this be false? I don't know.
                    }
                    if (hasEnough) {
                        try {
                            com.earth2me.essentials.api.Economy.substract(player.getName(), reqBal);
                        } catch (NoLoanPermittedException | ArithmeticException	| UserDoesNotExistException e) {
                            sendMsg(player, "Internal error: UserDoesNotExistException. No transaction was made. Please report.");
                            return true; // Should this be false? I don't know.
                        }
                        PlayerData playerData = dataStore.readPlayerData(player.getName());
                        playerData.addCredit();
                        dataStore.savePlayerData(playerData);
                        sendMsg(player, "Successfully purchased a chunk credit for $" + String.valueOf(reqBal.doubleValue()) + ".");
                        return true;
                } else {
                    sendMsg(player, "You can't afford to buy another chunk for $" + String.valueOf(reqBal.doubleValue()) + ".");
                    return true;
                }
            }
        } else if (args[0].equalsIgnoreCase("index")) {
            if(args.length == 2 && player.hasPermission("chunkclaim.admin")) {
                // Check if second argument is valid... TODO
                priceIndex.setPI(BigDecimal.valueOf(Double.parseDouble(args[1])));
                return true;
                }
            // Display the price index
            sendMsg(player, "The current price index is $" + priceIndex.getPI());
            return true;
            } else {
                    return false;
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
