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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class ChunkClaim extends JavaPlugin {
    public static ChunkClaim plugin;
    public static final Logger logger = Logger.getLogger("Minecraft");

    public DataStore dataStore;

    public List<String> config_worlds;
    public boolean config_protectContainers;
    public boolean config_protectSwitches;
    public boolean config_mobsForCredits;
    public int config_mobPrice;
    public float config_creditsPerHour;
    public float config_maxCredits;
    public float config_startCredits;
    public int config_minModBlocks;
    public float config_autoDeleteDays;
    public boolean config_nextToForce;
    private boolean config_regenerateChunk;

    public void onDisable() {
        Player[] players = this.getServer().getOnlinePlayers();
        for (Player player : players) {
            String playerName = player.getName();
            PlayerData playerData = this.dataStore.getPlayerData(playerName);
            this.dataStore.savePlayerData(playerName, playerData);
        }

        this.dataStore.close();
        plugin = null;
    }

    public void onEnable() {
        plugin = this;

        // copy default config
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.config_worlds = this.getConfig().getStringList("worlds");
        this.config_protectSwitches = this.getConfig().getBoolean("protectSwitches");
        this.config_protectContainers = this.getConfig().getBoolean("protectContainers");
        this.config_mobsForCredits = this.getConfig().getBoolean("mobsForCredits");
        this.config_mobPrice = this.getConfig().getInt("mobPrice");
        this.config_startCredits = (float) this.getConfig().getDouble("startCredits");
        this.config_creditsPerHour = (float) this.getConfig().getDouble("creditsPerHour");
        this.config_maxCredits = (float) this.getConfig().getDouble("maxCredits");
        this.config_minModBlocks = this.getConfig().getInt("minModBlocks");
        this.config_autoDeleteDays = (float) this.getConfig().getDouble("autoDeleteDays");
        this.config_nextToForce = this.getConfig().getBoolean("nextToForce");
        this.config_regenerateChunk = this.getConfig().getBoolean("regenerateChunk");

        try {
            this.dataStore = new FlatFileDataStore();
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

        // register world events
        WorldEventHandler worldEventHandler = new WorldEventHandler(dataStore);
        pluginManager.registerEvents(worldEventHandler, this);

        if (this.config_creditsPerHour > 0) {
            DeliverCreditsTask task = new DeliverCreditsTask();
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 20L * 60 * 60, 20L * 60 * 60);
        }

    }

    // handles slash commands
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        if (cmd.getName().equalsIgnoreCase("chunk") && player != null) {
            if (!ChunkClaim.plugin.config_worlds.contains(player.getWorld().getName())) {
                return true;
            }

            if (args.length == 0) {

                ChunkPlot chunk = this.dataStore.getChunkAt(player.getLocation(), null);
                Location location = player.getLocation();
                PlayerData playerData = this.dataStore.getPlayerData(player.getName());

                if (player.hasPermission("chunkclaim.admin")) {
                    String adminString = "ID: " + location.getChunk().getX() + "|" + location.getChunk().getZ();
                    if (chunk != null) {
                        adminString += ", Permanent: " + (chunk.getModifiedBlocks() < 0 ? "true" : ("false (" + chunk.getModifiedBlocks() + ")"));
                        long loginDays = ((new Date()).getTime() - this.dataStore.getPlayerData(chunk.getOwnerName()).lastLogin.getTime()) / (1000 * 60 * 60 * 24);
                        adminString += ", Last Login: " + loginDays + " days ago.";
                    }
                    sendMsg(player, adminString);
                    if (chunk != null && !chunk.getOwnerName().equals(player.getName())) {
                        StringBuilder builders = new StringBuilder();
                        for (String builder : chunk.getBuilderNames()) {
                            builders.append(builder);
                            builders.append(" ");
                        }
                        Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.Chunk, location);
                        Visualization.Apply(player, visualization);
                        sendMsg(player, "Trusted Builders:");
                        sendMsg(player, builders.toString());
                    }
                }

                if (chunk == null) {
                    sendMsg(player, "This chunk is public.");
                    Visualization visualization = Visualization.FromBukkitChunk(location.getChunk(), location.getBlockY(), VisualizationType.Public, location);
                    Visualization.Apply(player, visualization);
                    return true;

                } else if (chunk.getOwnerName().equals(player.getName())) {
                    if (chunk.getBuilderNames().size() > 0) {
                        StringBuilder builders = new StringBuilder();
                        for (String builder : chunk.getBuilderNames()) {
                            builders.append(builder);
                            builders.append(" ");
                        }
                        Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.Chunk, location);
                        Visualization.Apply(player, visualization);
                        sendMsg(player, "You own this chunk. Trusted Builders:");
                        sendMsg(player, builders.toString());

                    } else {
                        sendMsg(player, "You own this chunk. Use /chunk trust <player> to add other builders.");
                    }
                    Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.Chunk, location);
                    Visualization.Apply(player, visualization);
                    return true;

                } else {

                    if (chunk.isTrusted(player.getName())) {
                        sendMsg(player, chunk.getOwnerName() + " owns this chunk. You have build rights!");
                        if (playerData.lastChunk != chunk) {
                            playerData.lastChunk = chunk;
                            Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.Chunk, location);
                            Visualization.Apply(player, visualization);
                        }
                    } else {

                        sendMsg(player, chunk.getOwnerName() + " owns this chunk. You can't build here.");
                        if (playerData.lastChunk != chunk) {
                            playerData.lastChunk = chunk;
                            Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.ErrorChunk, location);
                            Visualization.Apply(player, visualization);
                        }
                    }
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("abandon")) {
                ChunkPlot chunk = this.dataStore.getChunkAt(player.getLocation(), null);
                PlayerData playerData = this.dataStore.getPlayerData(player.getName());
                Location location = player.getLocation();

                if (args.length == 2) {
                    int radius;
                    int abd = 0;

                    try {
                        radius = Integer.parseInt(args[1]);

                        if (radius < 0) {
                            sendMsg(player, "Error: Negative Radius");
                            return true;
                        }

                        if (radius > 10) {
                            sendMsg(player, "Error: Max Radius is 10.");
                            return true;
                        }

                        ArrayList<ChunkPlot> chunksInRadius = this.getChunksInRadius(chunk, player.getName(), radius);

                        for (ChunkPlot chunkPlot : chunksInRadius) {
                            this.dataStore.deleteChunk(chunkPlot);
                            playerData.credits++;
                            abd++;
                        }

                        this.dataStore.savePlayerData(player.getName(), playerData);
                        sendMsg(player, abd + " Chunks abandoned in radius " + radius + ". Credits: " + playerData.getCredits());
                        return true;

                    } catch (Exception e) {

                        sendMsg(player, "Usage: /chunk abandon [radius]");
                        return true;
                    }

                } else if (args.length == 1) {
                    if (chunk == null) {
                        sendMsg(player, "This chunk is public.");
                        Visualization visualization = Visualization.FromBukkitChunk(location.getChunk(), location.getBlockY(), VisualizationType.Public, location);
                        Visualization.Apply(player, visualization);

                    } else if (chunk.getOwnerName().equals(player.getName())) {
                        this.dataStore.deleteChunk(chunk);
                        playerData.credits++;
                        this.dataStore.savePlayerData(player.getName(), playerData);
                        sendMsg(player, "ChunkPlot abandoned. Credits: " + playerData.getCredits());

                        Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.Public, location);
                        Visualization.Apply(player, visualization);

                        return true;

                    } else {

                        if (playerData.lastChunk != chunk) {
                            playerData.lastChunk = chunk;
                            Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.ErrorChunk, location);
                            Visualization.Apply(player, visualization);
                        }
                        sendMsg(player, "You don't own this chunk. Only " + chunk.getOwnerName() + " or the staff can delete it.");
                        return true;
                    }

                } else {
                    sendMsg(player, "Usage: /chunk abandon [radius]");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("credits")) {

                PlayerData playerData = this.dataStore.getPlayerData(player.getName());
                sendMsg(player, "You have " + playerData.getCredits() + " credits.");
                return true;

            } else if (args[0].equalsIgnoreCase("trust")) {

                PlayerData playerData = this.dataStore.getPlayerData(player.getName());

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

                ArrayList<ChunkPlot> chunksInRadius = this.dataStore.getAllChunksForPlayer(player.getName());

                if (!playerData.builderNames.contains(tName)) {
                    for (ChunkPlot chunkPlot : chunksInRadius) {
                        if (!chunkPlot.isTrusted(tName)) {
                            chunkPlot.addBuilder(tName);
                            dataStore.writeChunkToStorage(chunkPlot);
                        }
                    }
                    playerData.builderNames.add(tName);
                    this.dataStore.savePlayerData(player.getName(), playerData);
                }
                sendMsg(player, "Trusted " + tName + " in all your chunks.");
                return true;

            }
            //UNTRUST
            else if (args[0].equalsIgnoreCase("untrust")) {
                PlayerData playerData = this.dataStore.getPlayerData(player.getName());

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

                ArrayList<ChunkPlot> chunksInRadius = this.dataStore.getAllChunksForPlayer(player.getName());

                if (playerData.builderNames.contains(tName)) {
                    for (ChunkPlot chunkPlot : chunksInRadius) {
                        chunkPlot.removeBuilder(tName);
                        dataStore.writeChunkToStorage(chunkPlot);
                    }
                    playerData.builderNames.remove(tName);
                    this.dataStore.savePlayerData(player.getName(), playerData);

                }

                sendMsg(player, "Untrusted " + tName + " in all your chunks.");
                return true;

            } else if (args[0].equalsIgnoreCase("ignore")) {
                if (!player.hasPermission("chunkclaim.admin")) {
                    sendMsg(player, "No permission.");
                    return true;
                }
                PlayerData playerData = this.dataStore.getPlayerData(player.getName());
                playerData.ignoreChunks = !playerData.ignoreChunks;
                if (playerData.ignoreChunks) {
                    sendMsg(player, "You now ignore chunks.");
                } else {
                    sendMsg(player, "You now respect chunks.");
                }
                return true;

            } else if (args[0].equalsIgnoreCase("delete")) {
                if (!player.hasPermission("chunkclaim.admin")) {
                    sendMsg(player, "No permission.");
                    return true;
                }

                Location location = player.getLocation();

                if (args.length == 3) {
                    int radius;
                    int abd = 0;

                    try {
                        radius = Integer.parseInt(args[2]);

                        if (radius < 0) {
                            sendMsg(player, "Error: Negative Radius");
                            return true;
                        }
                        if (radius > 10) {
                            sendMsg(player, "Error: Max Radius is 10.");
                            return true;
                        }
                        OfflinePlayer tp = resolvePlayer(args[1]);
                        if (tp == null) {

                            sendMsg(player, "Player not found.");
                            return true;
                        }
                        String tName = tp.getName();
                        PlayerData playerData = this.dataStore.getPlayerData(tName);

                        ChunkPlot chunk = new ChunkPlot(location.getChunk());
                        ArrayList<ChunkPlot> chunksInRadius = this.getChunksInRadius(chunk, tName, radius);

                        for (ChunkPlot chunkPlot : chunksInRadius) {
                            this.dataStore.deleteChunk(chunkPlot);
                            playerData.credits++;
                            abd++;
                        }

                        this.dataStore.savePlayerData(tName, playerData);
                        sendMsg(player, abd + " Chunks deleted in radius " + radius + ".");
                        return true;

                    } catch (Exception e) {
                        sendMsg(player, "Usage: /chunk delete [<player> <radius>]");
                        return true;
                    }

                } else if (args.length == 1) {
                    ChunkPlot chunk = this.dataStore.getChunkAt(player.getLocation(), null);

                    if (chunk == null) {
                        sendMsg(player, "This chunk is public.");
                        Visualization visualization = Visualization.FromBukkitChunk(location.getChunk(), location.getBlockY(), VisualizationType.Public, location);
                        Visualization.Apply(player, visualization);
                    } else {
                        PlayerData playerData = this.dataStore.getPlayerData(chunk.getOwnerName());
                        this.dataStore.deleteChunk(chunk);
                        playerData.credits++;
                        this.dataStore.savePlayerData(chunk.getOwnerName(), playerData);
                        sendMsg(player, "ChunkPlot deleted.");

                        Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.Public, location);
                        Visualization.Apply(player, visualization);

                        return true;
                    }

                } else {
                    sendMsg(player, "Usage: /chunk delete [<player> <radius>]");
                    return true;
                }

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

            } else if (args[0].equalsIgnoreCase("bonus")) {
                if (!player.hasPermission("chunkclaim.admin")) {
                    sendMsg(player, "No permission.");
                    return true;
                }
                if (args.length == 3) {
                    try {

                        int bonus = Integer.parseInt(args[2]);

                        OfflinePlayer tp = resolvePlayer(args[1]);
                        if (tp == null) {

                            sendMsg(player, "Player not found.");
                            return true;
                        }
                        String tName = tp.getName();
                        PlayerData playerData = this.dataStore.getPlayerData(tName);

                        playerData.credits += bonus;
                        playerData.bonus += bonus;


                        sendMsg(player, "Adjusted " + tName + "'s bonus by " + bonus + " credits. Total credits: " + playerData.getCredits());

                        this.dataStore.savePlayerData(player.getName(), playerData);

                        return true;

                    } catch (Exception e) {

                        sendMsg(player, "Usage: /chunk bonus <player> <credits>");
                        return true;
                    }
                } else {
                    sendMsg(player, "Usage: /chunk bonus <player> <credits>");
                    return true;
                }

            } else if (args[0].equalsIgnoreCase("claim")) {
                if (args.length == 1) {

                    Location location = player.getLocation();
                    if (!ChunkClaim.plugin.config_worlds.contains(location.getWorld().getName())) return true;

                    PlayerData playerData = dataStore.getPlayerData(player.getName());
                    ChunkPlot chunk = dataStore.getChunkAt(location, playerData.lastChunk);

                    String playerName = player.getName();

                    if (chunk == null) {
                        if (!player.hasPermission("chunkclaim.claim")) {
                            sendMsg(player, "You don't have permissions for claiming chunks.");
                            return true;
                        }
                        if (playerData.getCredits() > 0) {

                            if (config_nextToForce && !player.hasPermission("chunkclaim.admin")) {
                                ArrayList<ChunkPlot> playerChunks = dataStore.getAllChunksForPlayer(playerName);

                                if (playerChunks.size() > 0) {
                                    if (!dataStore.ownsNear(location, playerName)) {
                                        sendMsg(player, "You can only claim a new chunk next to your existing chunks.");
                                        return true;
                                    }
                                }
                            }

                            ChunkPlot newChunk = new ChunkPlot(location.getChunk(), playerName, playerData.builderNames);

                            this.dataStore.addChunk(newChunk);

                            playerData.credits--;
                            playerData.lastChunk = newChunk;
                            //newChunk.modify();
                            this.dataStore.savePlayerData(playerName, playerData);

                            sendMsg(player, "You claimed this chunk. Credits left: " + playerData.getCredits());

                            Visualization visualization = Visualization.FromChunk(newChunk, location.getBlockY(), VisualizationType.Chunk, location);
                            Visualization.Apply(player, visualization);

                        } else {

                            sendMsg(player, "Not enough credits to claim this chunk.");

                            if (playerData.lastChunk != chunk) {
                                playerData.lastChunk = chunk;
                                Visualization visualization = Visualization.FromBukkitChunk(location.getChunk(), location.getBlockY(), VisualizationType.Public, location);
                                Visualization.Apply(player, visualization);
                            }
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
                if (player.hasPermission("chunkclaim.admin")) {
                    if (args.length == 2) {


                        OfflinePlayer tp = resolvePlayer(args[1]);
                        if (tp == null) {

                            sendMsg(player, "Player not found.");
                            return true;
                        }
                        String tName = tp.getName();

                        ArrayList<ChunkPlot> chunksInRadius = this.dataStore.getAllChunksForPlayer(tName);

                        long loginDays = ((new Date()).getTime() - this.dataStore.getPlayerData(tp.getName()).lastLogin.getTime()) / (1000 * 60 * 60 * 24);
                        long joinDays = ((new Date()).getTime() - this.dataStore.getPlayerData(tp.getName()).firstJoin.getTime()) / (1000 * 60 * 60 * 24);
                        String adminString = tp.getName() + " | Last Login: " + loginDays + " days ago. First Join: " + joinDays + " days ago.";
                        sendMsg(player, adminString);

                        for (ChunkPlot chunkPlot : chunksInRadius) {
                            adminString = "ID: " + chunkPlot.getChunk().getX() + "|" + chunkPlot.getChunk().getZ() + "(" + (chunkPlot.getChunk().getX() * 16) + "|" + (chunkPlot.getChunk().getZ() * 16) + ")";

                            adminString += ", Permanent: " + (chunkPlot.getModifiedBlocks() < 0 ? "true" : ("false (" + chunkPlot.getModifiedBlocks() + ")"));

                            sendMsg(player, adminString);


                        }
                        return true;
                    } else {
                        sendMsg(player, "Usage: /chunk list <player>");
                        return true;
                    }
                } else {
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("mark")) {
                if (player.hasPermission("chunkclaim.admin")) {
                    if (args.length == 1) {

                        Location location = player.getLocation();
                        if (!ChunkClaim.plugin.config_worlds.contains(location.getWorld().getName())) return true;

                        PlayerData playerData = dataStore.getPlayerData(player.getName());
                        ChunkPlot chunk = dataStore.getChunkAt(location, playerData.lastChunk);

                        if (chunk != null) {
                            String playerName = player.getName();
                            ChunkClaim.addLogEntry("ChunkPlot at " + chunk.getChunk().getX() + "|" + chunk.getChunk().getZ() + " has been marked for deletion by " + playerName);
                            chunk.mark();
                            sendMsg(player, "Marked chunk for deletion.");

                        } else {
                            sendMsg(player, "This chunk is public.");
                        }
                        return true;
                    } else {
                        sendMsg(player, "Usage: /chunk mark");
                        return true;
                    }
                } else return false;
            } else if (args[0].equalsIgnoreCase("unmark")) {
                if (player.hasPermission("chunkclaim.admin")) {
                    if (args.length == 1) {

                        Location location = player.getLocation();
                        if (!ChunkClaim.plugin.config_worlds.contains(location.getWorld().getName())) return true;

                        PlayerData playerData = dataStore.getPlayerData(player.getName());
                        ChunkPlot chunk = dataStore.getChunkAt(location, playerData.lastChunk);

                        if (chunk != null) {
                            String playerName = player.getName();
                            ChunkClaim.addLogEntry("ChunkPlot at " + chunk.getChunk().getX() + "|" + chunk.getChunk().getZ() + " has been unmarked by " + playerName);
                            chunk.unmark();
                            sendMsg(player, "Unmarked chunk.");

                        } else {
                            sendMsg(player, "This chunk is public.");
                        }
                        return true;
                    } else {
                        sendMsg(player, "Usage: /chunk unmark");
                        return true;
                    }
                } else return false;
            } else if (args[0].equalsIgnoreCase("next")) {
                if (player.hasPermission("chunkclaim.admin")) {

                    if (args.length == 1) {
                        Location location = player.getLocation();
                        if (!ChunkClaim.plugin.config_worlds.contains(location.getWorld().getName())) {
                            return true;
                        }

                        ChunkPlot chunk = null;
                        String worldName = null;
                        boolean inspected = true;
                        boolean marked = true;
                        boolean permanent = false;

                        int r = this.dataStore.nextChunkId;

                        for (int i = 0; i < dataStore.chunks.size(); i++) {

                            int j = (r + i) % dataStore.chunks.size();

                            chunk = dataStore.chunks.get(j);
                            worldName = chunk.getChunk().getWorld().getName();
                            inspected = chunk.isInspected();
                            marked = chunk.isMarked();
                            permanent = chunk.getModifiedBlocks() == -1;

                            if (worldName.equals(player.getWorld().getName()) && !inspected && !marked && permanent) {
                                break;
                            }
                        }
                        if (chunk == null || !(worldName.equals(player.getWorld().getName()) && !inspected && !marked && permanent)) {
                            sendMsg(player, "No chunk found.");
                            return true;
                        }

                        chunk.setInspected(true);
                        int x = chunk.getChunk().getX() * 16 + 8;
                        int z = chunk.getChunk().getZ() * 16 + 8;
                        int y = player.getWorld().getHighestBlockYAt(new Location(player.getWorld(), x, 0, z)) + 15;

                        Location l = new Location(player.getWorld(), x, y, z, 0, 90);

                        player.teleport(l);

                        String adminString = "ID: " + chunk.getChunk().getX() + "|" + chunk.getChunk().getZ();
                        adminString += ", " + chunk.getOwnerName();
                        long loginDays = ((new Date()).getTime() - this.dataStore.getPlayerData(chunk.getOwnerName()).lastLogin.getTime()) / (1000 * 60 * 60 * 24);
                        adminString += ", Last Login: " + loginDays + " days ago.";
                        sendMsg(player, adminString);
                        Visualization visualization = Visualization.FromChunk(chunk, location.getBlockY(), VisualizationType.Chunk, location);
                        Visualization.Apply(player, visualization);

                        return true;

                    } else {
                        sendMsg(player, "Usage: /chunk next");
                        return true;
                    }
                } else return false;
            } else {
                return false;
            }
        }
        return false;
    }

    public ArrayList<ChunkPlot> getChunksInRadius(ChunkPlot chunk, String playerName, int radius) {

        ArrayList<ChunkPlot> chunksInRadius = new ArrayList<ChunkPlot>();

        for (int x = chunk.getChunk().getX() - radius; x <= chunk.getChunk().getZ() + radius; x++) {
            for (int z = chunk.getChunk().getX() - radius; z <= chunk.getChunk().getZ() + radius; z++) {

                ChunkPlot foundChunk = this.dataStore.getChunkAtPos(x, z, chunk.getChunk().getWorld().getName());

                if (foundChunk != null && foundChunk.getOwnerName().equals(playerName)) {

                    chunksInRadius.add(foundChunk);

                }

            }
        }

        return chunksInRadius;
    }

    public void regenerateChunk(ChunkPlot chunk) {
        if (config_regenerateChunk) {
            getServer().getWorld(chunk.getChunk().getWorld().getName()).regenerateChunk(chunk.getChunk().getX(), chunk.getChunk().getZ());
            getServer().getWorld(chunk.getChunk().getWorld().getName()).unloadChunkRequest(chunk.getChunk().getX(), chunk.getChunk().getZ());
        }

    }

    public OfflinePlayer resolvePlayer(String name) {

        Player player = this.getServer().getPlayer(name);
        if (player != null)
            return player;

        // then search offline players
        OfflinePlayer[] offlinePlayers = this.getServer().getOfflinePlayers();
        for (int i = 0; i < offlinePlayers.length; i++) {
            if (offlinePlayers[i].getName().equalsIgnoreCase(name)) {
                return offlinePlayers[i];
            }
        }

        // if none found, return null
        return null;

    }

    public static void addLogEntry(String entry) {
        logger.info("ChunkClaim: " + entry);
    }

    public void sendMsg(Player player, String message) {
        player.sendMessage(ChatColor.YELLOW + message);
    }
}