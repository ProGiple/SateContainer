package org.satellite.dev.progiple.satecontainer.event.menu;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.novasparkle.lunaspring.Menus.AMenu;
import org.satellite.dev.progiple.satecontainer.SateContainer;
import org.satellite.dev.progiple.satecontainer.configs.Config;
import org.satellite.dev.progiple.satecontainer.event.ContainerManager;

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.IntStream;

@Getter
public class CMenu extends AMenu {
    private final Set<LootItem> items = new HashSet<>();
    public CMenu(Player player) {
        super(player, Config.getString("menu.title"),
                (byte) (Config.getInt("menu.rows") * 9), Config.getSection("menu.decorations"));
    }

    @Override
    public void onOpen(InventoryOpenEvent e) {
        int timer = Config.getInt("settings.getRewardTime");
        ContainerManager.getEvent().setMenuViewer(this.getPlayer());
        Player player = ContainerManager.getEvent().getMenuViewer();

        ContainerManager.getEvent().setMenuTask(Bukkit.getScheduler().runTaskTimer(SateContainer.getPlugin(), () -> {
            byte slot = this.getNextSlot();
            if (player != null) {
                if (slot <= -1) {
                    player.closeInventory();
                    Config.sendMessage(player, "all_loot_collected");
                }
                else {
                    LootItem lootItem = new LootItem(this, timer * 20, slot);
                    this.items.add(lootItem);
                }
            }
        }, 10L, (timer + 1) * 20L));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
        ContainerManager.getEvent().setMenuViewer(null);
        this.items.forEach(item -> {
            int taskId = item.getTaskId();
            if (Bukkit.getScheduler().isCurrentlyRunning(taskId) || Bukkit.getScheduler().isQueued(taskId))
                Bukkit.getScheduler().cancelTask(taskId);
        });
        ContainerManager.getEvent().stopMenuScroll();
    }

    private byte getNextSlot() {
        Inventory inventory = this.getInventory();
        OptionalInt emptySlot = IntStream.range(0, inventory.getSize())
                .filter(i -> {
                    ItemStack item = inventory.getItem(i);
                    return item == null || item.getType() == Material.AIR;
                })
                .findFirst();

        return (byte) (emptySlot.orElse(-1));
    }
}
