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
import org.satellite.dev.progiple.satecontainer.event.ContainerManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class CMenu extends AMenu {
    private int taskId;
    private final Set<LootItem> items = new HashSet<>();
    private final List<Integer> loot_slots;
    public CMenu(Player player) {
        super(player, Config.getString("menu.title"),
                (byte) (Config.getInt("menu.rows") * 9), Config.getSection("menu.decorations"));
        this.loot_slots = new ArrayList<>(Config.getIntList("menu.loot_slots"));
    }

    @Override
    public void onOpen(InventoryOpenEvent e) {
        int timer = Config.getInt("settings.getRewardTime");
        Player player = this.getPlayer();

        ContainerManager.getEvent().setMenuViewer(player);
        this.taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(SateContainer.getPlugin(), () -> {
            byte slot = this.getNextSlot();
            if (slot <= -1) this.closeInv(player);
            else {
                LootItem lootItem = new LootItem(this, timer * 20, slot);
                this.items.add(lootItem);
                this.loot_slots.remove(0);
                lootItem.insert(this, slot);
            }
        }, 0, (timer + 1) * 20L).getTaskId();
    }

    public void closeInv(Player player) {
        Bukkit.getScheduler().cancelTask(this.taskId);
        Bukkit.getScheduler().runTask(SateContainer.getPlugin(), () -> player.closeInventory());
        Config.sendMessage(player, "all_loot_collected");
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

        if (Bukkit.getScheduler().isCurrentlyRunning(this.taskId) || Bukkit.getScheduler().isQueued(this.taskId))
            Bukkit.getScheduler().cancelTask(this.taskId);
    }

    private byte getNextSlot() {
        return (byte) (this.loot_slots.isEmpty() ? -1 : this.loot_slots.get(0));
    }
}
