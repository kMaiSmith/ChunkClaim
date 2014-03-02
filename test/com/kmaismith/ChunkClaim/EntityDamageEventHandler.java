/*
    ChunkClaim Plugin for Minecraft Bukkit Servers
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
import junit.framework.Assert;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class EntityDamageEventHandler {

    private Vehicle mockVehicle;
    private ChunkData mockChunk;
    private EntityEventHandler systemUnderTest;
    private Player mockPlayer;
    private Projectile mockProjectile;
    private Creature mockCreature;

    @Before
    public void setupTestCase() {
        mockVehicle = mock(Vehicle.class);
        mockCreature = mock(Creature.class);
        DataManager mockDataManager = mock(DataManager.class);
        Location mockLocation = mock(Location.class);
        mockChunk = mock(ChunkData.class);
        systemUnderTest = new EntityEventHandler(mockDataManager);

        when(mockVehicle.getLocation()).thenReturn(mockLocation);
        when(mockCreature.getLocation()).thenReturn(mockLocation);
        when(mockDataManager.getChunkAt(mockLocation)).thenReturn(mockChunk);
        when(mockChunk.getOwnerName()).thenReturn("APlayer");
    }

    private void playerIsUntrusted() {
        mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("ThatPlayer");
        when(mockChunk.isTrusted("ThatPlayer")).thenReturn(false);
    }

    private void playerIsTrusted() {
        mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("ThatPlayer");
        when(mockChunk.isTrusted("ThatPlayer")).thenReturn(true);
    }

    private void playerIsAdmin() {
        mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("Admin");
        when(mockPlayer.hasPermission("chunkclaim.admin")).thenReturn(true);
        when(mockChunk.isTrusted("ThatPlayer")).thenReturn(false);
    }

    private void playerThrewProjectile() {
        mockProjectile = mock(Projectile.class);
        when(mockProjectile.getShooter()).thenReturn(mockPlayer);
    }

    // onVehicleDamage regular damage

    @Test
    public void testUntrustedPlayerCannotDamageVehiclesWhenInChunk() {
        playerIsUntrusted();

        VehicleDamageEvent event = new VehicleDamageEvent(mockVehicle, mockPlayer, 10.0);

        systemUnderTest.onVehicleDamage(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou do not have APlayer's permission to break vehicles here.");
    }

    @Test
    public void testTrustedPlayerCanDamageVehiclesWhenInChunk() {
        playerIsTrusted();

        VehicleDamageEvent event = new VehicleDamageEvent(mockVehicle, mockPlayer, 10.0);

        systemUnderTest.onVehicleDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testAdminCanDamageVehiclesWhenInChunk() {
        playerIsAdmin();

        VehicleDamageEvent event = new VehicleDamageEvent(mockVehicle, mockPlayer, 10.0);

        systemUnderTest.onVehicleDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    // onVehicleDamage projectile damage

    @Test
    public void testUntrustedPlayerCannotDamageVehiclesWithProjectilesWhenInChunk() {

        playerIsUntrusted();
        playerThrewProjectile();

        VehicleDamageEvent event = new VehicleDamageEvent(mockVehicle, mockProjectile, 10.0);

        systemUnderTest.onVehicleDamage(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou do not have APlayer's permission to break vehicles here.");
    }

    @Test
    public void testTrustedPlayerCanDamageVehiclesWithProjectilesWhenInChunk() {
        playerIsTrusted();
        playerThrewProjectile();

        VehicleDamageEvent event = new VehicleDamageEvent(mockVehicle, mockProjectile, 10.0);

        systemUnderTest.onVehicleDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testAdminCanDamageVehiclesWithProjectilesWhenInChunk() {
        playerIsAdmin();
        playerThrewProjectile();

        VehicleDamageEvent event = new VehicleDamageEvent(mockVehicle, mockProjectile, 10.0);

        systemUnderTest.onVehicleDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    // onVehicleDamage non-player damage

    @Test
    public void testWhenVehicleIsAttackedByNonPlayerEventIsPassed() {
        VehicleDamageEvent event = new VehicleDamageEvent(mockVehicle, mock(Creeper.class), 10.0);

        systemUnderTest.onVehicleDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testWhenVehicleIsAttackedByNonPlayerWithProjectileEventIsPassed() {
        Skeleton mockSkeleton = mock(Skeleton.class);
        Projectile mockAttacker = mock(Projectile.class);
        when(mockAttacker.getShooter()).thenReturn(mockSkeleton);

        VehicleDamageEvent event = new VehicleDamageEvent(mockVehicle, mockAttacker, 10.0);

        systemUnderTest.onVehicleDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    // onEntityDamage regular damage

    @Test
    public void testUntrustedPlayerCannotDamageCreaturesWhenInChunk() {
        playerIsUntrusted();

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(mockPlayer, mockCreature, null, 10.0);

        systemUnderTest.onEntityDamage(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou do not have APlayer's permission to hurt creatures here.");
    }

    @Test
    public void testTrustedPlayerCanDamageCreaturesWhenInChunk() {
        playerIsTrusted();

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(mockPlayer, mockCreature, null, 10.0);

        systemUnderTest.onEntityDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testAdminCanDamageCreaturesWhenInChunk() {
        playerIsAdmin();

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(mockPlayer, mockCreature, null, 10.0);

        systemUnderTest.onEntityDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    // onEntityDamage projectile damage

    @Test
    public void testUntrustedPlayerCannotDamageCreaturesWithProjectilesWhenInChunk() {

        playerIsUntrusted();
        playerThrewProjectile();

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(mockProjectile, mockCreature, null, 10.0);

        systemUnderTest.onEntityDamage(event);
        Assert.assertTrue(event.isCancelled());
        verify(mockPlayer).sendMessage("§eYou do not have APlayer's permission to hurt creatures here.");
    }

    @Test
    public void testTrustedPlayerCanDamageCreaturesWithProjectilesWhenInChunk() {
        playerIsTrusted();
        playerThrewProjectile();

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(mockProjectile, mockCreature, null, 10.0);

        systemUnderTest.onEntityDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testAdminCanDamageCreaturesWithProjectilesWhenInChunk() {
        playerIsAdmin();
        playerThrewProjectile();

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(mockProjectile, mockCreature, null, 10.0);

        systemUnderTest.onEntityDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    // onEntityDamage non-player damage

    @Test
    public void testWhenCreaturesIsAttackedByNonPlayerEventIsPassed() {
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(mock(Creeper.class), mockCreature, null, 10.0);

        systemUnderTest.onEntityDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testWhenCreaturesIsAttackedByNonPlayerWithProjectileEventIsPassed() {
        Skeleton mockSkeleton = mock(Skeleton.class);
        Projectile mockAttacker = mock(Projectile.class);
        when(mockAttacker.getShooter()).thenReturn(mockSkeleton);

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(mockAttacker, mockCreature, null, 10.0);

        systemUnderTest.onEntityDamage(event);
        Assert.assertFalse(event.isCancelled());
    }

    @Test
    public void testWhenMonstersAreAttackedByUntrustedPlayersTheyStillGetHurt() {
        playerIsUntrusted();

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(mockPlayer, mock(Monster.class), null, 10.0);

        systemUnderTest.onEntityDamage(event);
        Assert.assertFalse(event.isCancelled());
    }
}
