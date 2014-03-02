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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

class EntityEventHandler extends ChunkClaimEventHandler implements Listener {
    private final DataManager dataStore;

    public EntityEventHandler(DataManager dataStore) {
        this.dataStore = dataStore;
    }

    private Player getAttackingPlayer(Entity damageSource) {
        if (damageSource instanceof Player) {
            return (Player) damageSource;
        } else if (damageSource instanceof Projectile) {
            Projectile projectile = (Projectile) damageSource;
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onEntityDamage(EntityDamageEvent event) {
        Entity damagedEntity = event.getEntity();
        if (!(event instanceof EntityDamageByEntityEvent) || (damagedEntity instanceof Monster)) {
            return;
        }

        Player attacker = getAttackingPlayer(((EntityDamageByEntityEvent) event).getDamager());
        ChunkData chunk = dataStore.getChunkAt(event.getEntity().getLocation());

        revokeIfNotPermitted(attacker, chunk, event, "hurt creatures");

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onVehicleDamage(VehicleDamageEvent event) {
        Player attacker = getAttackingPlayer(event.getAttacker());
        ChunkData chunk = dataStore.getChunkAt(event.getVehicle().getLocation());

        revokeIfNotPermitted(attacker, chunk, event, "break vehicles");
    }

}