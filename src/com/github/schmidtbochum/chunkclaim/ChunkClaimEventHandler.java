package com.github.schmidtbochum.chunkclaim;

import com.github.schmidtbochum.chunkclaim.Data.ChunkData;
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
