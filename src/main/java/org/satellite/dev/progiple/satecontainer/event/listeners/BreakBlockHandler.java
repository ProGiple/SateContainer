package org.satellite.dev.progiple.satecontainer.event.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.satellite.dev.progiple.satecontainer.event.Container;
import org.satellite.dev.progiple.satecontainer.event.ContainerEvent;

public class BreakBlockHandler implements Listener {
    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) return;

        ContainerEvent event = ContainerEvent.getEvent();
        if (event == null) return;

        Container container = event.getContainer();
        if (container != null && event.getLocation().equals(block.getLocation()))
            e.setCancelled(true);
    }
}
