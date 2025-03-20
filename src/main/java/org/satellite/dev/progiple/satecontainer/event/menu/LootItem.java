package org.satellite.dev.progiple.satecontainer.event.menu;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.Menus.Items.Item;
import org.novasparkle.lunaspring.Util.Utils;
import org.satellite.dev.progiple.satecontainer.SateContainer;
import org.satellite.dev.progiple.satecontainer.Vault;
import org.satellite.dev.progiple.satecontainer.configs.Config;
import org.satellite.dev.progiple.satecontainer.configs.ItemsConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public class LootItem extends Item {
    private static final List<Material> materials;
    static {
        materials = Stream.of(Material.values()).filter(Material::isItem).collect(Collectors.toList());
        Collections.shuffle(materials);
    }

    private final CMenu cMenu;
    private boolean isMoney = true;
    private int taskId = 0;
    public LootItem(CMenu menu, int timer, byte slot) {
        super(Material.STONE);
        this.cMenu = menu;
        this.startRefreshItem(timer, slot);
    }

    private void startRefreshItem(int timer, byte slot) {
        int refreshTime = Config.getInt("settings.refreshItemTime");
        Player player = this.cMenu.getPlayer();

        AtomicInteger usedTime = new AtomicInteger();
        this.taskId = Bukkit.getScheduler().runTaskTimer(SateContainer.getPlugin(), () -> {
            if (usedTime.get() >= timer) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                this.placeReward(player);
            }
            else {
                int index = Math.min(Utils.getRandom().nextInt(materials.size()), materials.size() - 1);
                Material material = materials.get(index);
                this.setAll(material, 1, " ", null, false);

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                usedTime.addAndGet(refreshTime);
            }
            this.insert(this.cMenu, slot);
        }, 0, refreshTime).getTaskId();
    }

    private void placeReward(Player player) {
        int items = (int) this.cMenu.getItems().stream().filter(i -> !i.isMoney).count();
        ConfigurationSection section;
        double cost = -1;

        if (items >= Config.getInt("settings.maxItemsLoot")
                || Config.getDouble("settings.itemLootChance") / 100 < Math.random())
            section = Config.getSection("menu.money_slot");
        else {
            Inventory inventory = player.getInventory();
            boolean hasEmptySlot = IntStream.range(0, inventory.getSize() - 5)
                    .mapToObj(inventory::getItem)
                    .anyMatch(item -> item == null || item.getType() == Material.AIR);

            List<String> keys = new ArrayList<>(ItemsConfig.getSection("items").getKeys(false));
            int index = Math.min(Utils.getRandom().nextInt(keys.size()), keys.size() - 1);

            ConfigurationSection itemSection = ItemsConfig.getSection(String.format("items.%s", keys.get(index)));
            section = hasEmptySlot ? itemSection : Config.getSection("menu.full_inv_slot");

            this.isMoney = false;
            if (hasEmptySlot) cost = LootItem.getRandomCost(Objects.requireNonNull(itemSection.getString("money_cost")));
        }

        if (cost <= -1) cost = LootItem.getRandomCost(Config.getString("settings.money_drop"));

        this.setAll(section);
        if (!this.cMenu.getItems().isEmpty())
            cost *= Math.pow(Config.getDouble("settings.money_multiplier"),
                    (Math.max(this.cMenu.getItems().size() - items, 1)));
        int finalCost = (int) Math.round(cost);

        this.setDisplayName(this.getDisplayName().replace("{cost}", String.valueOf(finalCost)));
        List<String> lore = new ArrayList<>(this.getLore());

        lore.replaceAll(line -> line.replace("{cost}", String.valueOf(finalCost)));
        this.setLore(lore);
        if (!this.isMoney && section.getKeys(false).contains("commands")) {
            List<String> commands = new ArrayList<>(section.getStringList("commands"));
            if (!commands.isEmpty()) {
                commands.forEach(command -> Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(), command
                                .replace("{player}", player.getName())
                                .replace("{cost}", String.valueOf(finalCost))));
            }
            else player.getInventory().addItem(new ItemStack(this.getMaterial(), this.getAmount()));
        }
        else {
            Vault.getEconomy().depositPlayer(player, finalCost);
            Config.sendMessage(player, "get_money", String.valueOf(finalCost));
        }

        Bukkit.getScheduler().cancelTask(this.taskId);
    }

    private static int getRandomCost(String str) {
        String[] split = str.split("-");
        if (split.length >= 2) {
            return Utils.getRandom().nextInt(Utils.toInt(split[1]) - Utils.toInt(split[0])) + Utils.toInt(split[0]);
        }
        return Utils.toInt(split[0]);
    }
}
