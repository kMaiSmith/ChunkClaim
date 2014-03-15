package com.kmaismith.chunkclaim;

import com.kmaismith.chunkclaim.Data.ChunkData;
import com.kmaismith.chunkclaim.Data.DataManager;
import com.kmaismith.chunkclaim.Data.PlayerData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class DataHelper {

    private DataManager dataManager;

    public DataHelper() {
        dataManager = mock(DataManager.class);
    }

    public DataHelper(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Location newLocation(String world, int x, int z) {
        Chunk bukkitChunk = mock(Chunk.class);
        World bukkitWorld = mock(World.class);
        when(bukkitWorld.getName()).thenReturn(world);
        when(bukkitChunk.getX()).thenReturn(x);
        when(bukkitChunk.getZ()).thenReturn(z);
        when(bukkitChunk.getWorld()).thenReturn(bukkitWorld);

        Location mockLocation = spy(new Location(bukkitWorld, x * 16, 64, z * 16));

        when(mockLocation.getChunk()).thenReturn(bukkitChunk);
        return mockLocation;
    }

    public Player newPlayer(String name, Location location, Boolean admin)
    {
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn(name);
        when(mockPlayer.getLocation()).thenReturn(location);
        when(mockPlayer.hasPermission("chunkclaim.admin")).thenReturn(admin);

        return mockPlayer;
    }

    public ChunkData newChunkData(String playerName, final ArrayList<String> trustedBuilders, Location location) {
        ChunkData chunk = new ChunkData(location.getChunk(), playerName, trustedBuilders);

        when(dataManager.getChunkAt(location)).thenReturn(chunk);

        return chunk;
    }

    public ChunkData newChunkData(String playerName, final ArrayList<String> trustedBuilders, int x, int z) {
        return newChunkData(playerName, trustedBuilders, newLocation("world", x, z));
    }

    public PlayerData newPlayerData(Player player, int daysSinceLogin, int daysSinceFirstLogin) {
        PlayerData playerData = mock(PlayerData.class);

        when(dataManager.readPlayerData(player.getName())).thenReturn(playerData);

        Date lastLogin = new Date((new Date()).getTime() - daysSinceLogin);
        when(playerData.getLastLogin()).thenReturn(lastLogin);
        Date firstLogin = new Date((new Date()).getTime() - daysSinceFirstLogin);
        when(playerData.getFirstJoin()).thenReturn(firstLogin);
        return playerData;
    }

}
