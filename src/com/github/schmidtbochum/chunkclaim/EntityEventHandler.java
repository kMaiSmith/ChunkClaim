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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

class EntityEventHandler implements Listener {
    private final DataManager dataStore;

    public EntityEventHandler(DataManager dataStore) {
        this.dataStore = dataStore;
    }

    //when an entity is damaged
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        //only actually interested in entities damaging entities (ignoring environmental damage)
        if (!(event instanceof EntityDamageByEntityEvent)) return;

        //monsters are never protected
        if (event.getEntity() instanceof Monster) return;
        EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;

        //determine which player is attacking, if any
        Player attacker = null;
        Arrow arrow;
        Entity damageSource = subEvent.getDamager();
        if (damageSource instanceof Player) {
            attacker = (Player) damageSource;
        } else if (damageSource instanceof Arrow) {
            arrow = (Arrow) damageSource;
            if (arrow.getShooter() instanceof Player) {
                attacker = (Player) arrow.getShooter();
            }
        } else if (damageSource instanceof ThrownPotion) {
            ThrownPotion potion = (ThrownPotion) damageSource;
            if (potion.getShooter() instanceof Player) {
                attacker = (Player) potion.getShooter();
            }
        }
        //if the entity is an non-monster creature (remember monsters disqualified above), or a vehicle
        if ((subEvent.getEntity() instanceof Creature)) {
            ChunkData chunk = dataStore.getChunkAt(event.getEntity().getLocation());

            //if it's claimed
            if (chunk != null) {
                if (attacker == null) {
                    event.setCancelled(true);
                } else {
                    if (!chunk.isTrusted(attacker.getName())) {
                        event.setCancelled(true);
                        ChunkClaim.plugin.sendMsg(attacker, "Not permitted.");
                    }
                }
            }
        }
    }

    //when a vehicle is damaged
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleDamage(VehicleDamageEvent event) {
        //determine which player is attacking, if any
        Player attacker = null;
        Arrow arrow;
        Entity damageSource = event.getAttacker();
        if (damageSource instanceof Player) {
            attacker = (Player) damageSource;
        } else if (damageSource instanceof Arrow) {
            arrow = (Arrow) damageSource;
            if (arrow.getShooter() instanceof Player) {
                attacker = (Player) arrow.getShooter();
            }
        } else if (damageSource instanceof ThrownPotion) {
            ThrownPotion potion = (ThrownPotion) damageSource;
            if (potion.getShooter() instanceof Player) {
                attacker = (Player) potion.getShooter();
            }
        }
        PlayerData playerData = null;
        if (attacker != null) {
            playerData = this.dataStore.readPlayerData(attacker.getName());
        }
        ChunkData chunk = dataStore.getChunkAt(event.getVehicle().getLocation());

        //if it's claimed
        if (chunk != null) {
            if (attacker == null) {
                event.setCancelled(true);
            } else {
                if (!chunk.isTrusted(attacker.getName())) {
                    event.setCancelled(true);
                    ChunkClaim.plugin.sendMsg(attacker, "Not permitted.");
                }
            }
        }
    }
}