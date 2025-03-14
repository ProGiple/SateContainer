package org.satellite.dev.progiple.satecontainer.event.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.novasparkle.lunaspring.Events.CooldownPrevent;
import org.satellite.dev.progiple.satecontainer.event.Container;
import org.satellite.dev.progiple.satecontainer.event.ContainerEvent;

public class ClickOnBlockHandler implements Listener {
    private final CooldownPrevent<Block> cooldown;
    public ClickOnBlockHandler() {
        this.cooldown = new CooldownPrevent<>();
        this.cooldown.setCooldownMS(10);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null || e.getAction() != Action.RIGHT_CLICK_BLOCK || this.cooldown.cancelEvent(null, block)) return;

        ContainerEvent event = ContainerEvent.getEvent();
        if (event == null) return;

        Container container = event.getContainer();
        if (container != null && event.getLocation().equals(block.getLocation())) {
            container.click(e.getPlayer());
            e.setCancelled(true);
        }
    }
}
