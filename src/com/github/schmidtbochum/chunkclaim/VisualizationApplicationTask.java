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

import org.bukkit.entity.Player;

class VisualizationApplicationTask implements Runnable {

    private Visualization visualization;
    private Player player;
    private PlayerData playerData;

    public VisualizationApplicationTask(Player player, PlayerData playerData, Visualization visualization) {
        this.visualization = visualization;
        this.playerData = playerData;
        this.player = player;
    }

    @Override
    public void run() {
        //for each element (=block) of the visualization
        if (playerData.currentVisualization == visualization) {
            for (int i = 0; i < visualization.elements.size(); i++) {
                VisualizationElement element = visualization.elements.get(i);

                //send the player a fake block change event
                if (element.location != null)
                    player.sendBlockChange(element.location, element.visualizedMaterial, element.visualizedData);
            }
        }
    }
}
