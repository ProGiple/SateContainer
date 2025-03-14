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
import org.novasparkle.lunaspring.Menus.AMenu;
import org.satellite.dev.progiple.satecontainer.SateContainer;
import org.satellite.dev.progiple.satecontainer.configs.Config;

import java.util.HashSet;
import java.util.Set;

@Getter
public class CMenu extends AMenu {
    @Getter private static Player opened = null;

    private int taskId;
    private final Set<LootItem> items = new HashSet<>();
    public CMenu(Player player) {
        super(player, Config.getString("menu.title"),
                (byte) (Config.getInt("menu.rows") * 9), Config.getSection("menu.decorations"));
    }

    @Override
    public void onOpen(InventoryOpenEvent e) {
        int timer = Config.getInt("settings.getRewardTime");
        this.getDecoration().insert(this);
        Player player = this.getPlayer();

        opened = player;
        this.taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(SateContainer.getPlugin(), () -> {
            byte slot = this.getEmptySlot(this.getInventory());
            if (slot <= -1) {
                Bukkit.getScheduler().cancelTask(this.taskId);
                Bukkit.getScheduler().runTask(SateContainer.getPlugin(), () -> player.closeInventory());
                Config.sendMessage(player, "all_loot_collected");
            }
            else {
                LootItem lootItem = new LootItem(this, timer * 20, slot);
                this.items.add(lootItem);
                lootItem.insert(this, slot);
            }
        }, 0, (timer + 1) * 20L).getTaskId();
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
        opened = null;
        this.items.forEach(item -> {
            int taskId = item.getTaskId();
            if (Bukkit.getScheduler().isCurrentlyRunning(taskId) || Bukkit.getScheduler().isQueued(taskId))
                Bukkit.getScheduler().cancelTask(taskId);
        });

        if (Bukkit.getScheduler().isCurrentlyRunning(this.taskId) || Bukkit.getScheduler().isQueued(this.taskId))
            Bukkit.getScheduler().cancelTask(this.taskId);
    }

    private byte getEmptySlot(Inventory inventory) {
        byte slot = -1;
        for (byte i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                slot = i;
                break;
            }
        }
        return slot;
    }
}
