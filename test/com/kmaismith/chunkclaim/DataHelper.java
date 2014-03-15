package com.kmaismith.chunkclaim;

import com.kmaismith.chunkclaim.Data.ChunkData;
import com.kmaismith.chunkclaim.Data.DataManager;
import com.kmaismith.chunkclaim.Data.PlayerData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Date;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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

        Location mockLocation = mock(Location.class);

        when(mockLocation.getBlockX()).thenReturn(x * 16);
        when(mockLocation.getBlockZ()).thenReturn(z * 16);

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
        ChunkData chunk = mock(ChunkData.class);
        when(chunk.getOwnerName()).thenReturn(playerName);
        when(chunk.getBuilderNames()).thenReturn(trustedBuilders);

        when(chunk.isTrusted(anyString())).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                String arg = (String) invocation.getArguments()[0];

                return trustedBuilders.contains(arg);
            }
        });

        when(chunk.getChunkX()).thenReturn(location.getChunk().getX());
        when(chunk.getChunkZ()).thenReturn(location.getChunk().getZ());

        when(dataManager.getChunkAt(location)).thenReturn(chunk);

        return chunk;
    }

    public ChunkData newChunkData(String playerName, final ArrayList<String> trustedBuilders, int x, int z) {
        return newChunkData(playerName, trustedBuilders, newLocation("world", x, z));
    }

    public PlayerData newPlayer(Player player, int daysSinceLogin, int daysSinceFirstLogin) {
        PlayerData playerData = mock(PlayerData.class);

        when(dataManager.readPlayerData(player.getName())).thenReturn(playerData);

        Date lastLogin = new Date((new Date()).getTime() - daysSinceLogin);
        when(playerData.getLastLogin()).thenReturn(lastLogin);
        Date firstLogin = new Date((new Date()).getTime() - daysSinceFirstLogin);
        when(playerData.getFirstJoin()).thenReturn(firstLogin);
        return playerData;
    }

}
