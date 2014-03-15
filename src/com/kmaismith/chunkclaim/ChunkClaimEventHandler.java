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

import com.kmaismith.chunkclaim.Data.ChunkData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

class ChunkClaimEventHandler {
    protected void revokeIfNotPermitted(Player attacker, ChunkData chunk, Cancellable event, String message) {
        if (chunk != null && attacker != null) {
            if (!attacker.hasPermission("chunkclaim.admin") && !chunk.isTrusted(attacker.getName())) {
                event.setCancelled(true);
                attacker.sendMessage(
                        ChatColor.YELLOW + "You do not have " +
                                chunk.getOwnerName() + "'s permission to " + message + " here.");
            }
        }
    }
}
