package com.crownSMP.listeners;

import com.crownSMP.CrownSMPPlugin;
import com.crownSMP.models.CrownType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MiningListener implements Listener {

    private final CrownSMPPlugin plugin;

    // Ores that auto-smelt → their smelted result
    private static final Map<Material, Material> SMELT_MAP = Map.ofEntries(
            Map.entry(Material.IRON_ORE,        Material.IRON_INGOT),
            Map.entry(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT),
            Map.entry(Material.GOLD_ORE,        Material.GOLD_INGOT),
            Map.entry(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT),
            Map.entry(Material.COPPER_ORE,      Material.COPPER_INGOT),
            Map.entry(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT),
            Map.entry(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT),
            Map.entry(Material.ANCIENT_DEBRIS,  Material.NETHERITE_SCRAP)
    );

    // Ores for vein mining
    private static final Set<Material> ORE_TYPES = Set.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.NETHER_GOLD_ORE, Material.ANCIENT_DEBRIS
    );

    public MiningListener(CrownSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        CrownType type = plugin.getCrownManager().getCrownType(player.getUniqueId());
        if (type == null) return;

        switch (type) {
            case INFERNO -> handleAutoSmelt(e, player);
            case TITANS  -> handleVeinMine(e, player);
        }
    }

    // INFERNO — auto-smelt ores
    private void handleAutoSmelt(BlockBreakEvent e, Player player) {
        Material smelted = SMELT_MAP.get(e.getBlock().getType());
        if (smelted == null) return;

        e.setDropItems(false);
        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(),
                new ItemStack(smelted, 1));
    }

    // TITANS — vein mine (break connected ores)
    private void handleVeinMine(BlockBreakEvent e, Player player) {
        Block origin = e.getBlock();
        if (!ORE_TYPES.contains(origin.getType())) return;

        String key = "titans.vein-mine";
        if (!plugin.getCrownManager().checkCooldown(player, key)) return;

        int radius = plugin.getConfig().getInt("abilities.titans.vein-mine-radius", 5);
        Material oreType = origin.getType();
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(origin);

        while (!queue.isEmpty() && visited.size() < 64) {
            Block current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbour = current.getRelative(dx, dy, dz);
                        if (neighbour.getType() == oreType && !visited.contains(neighbour)) {
                            if (neighbour.getLocation().distance(origin.getLocation()) <= radius) {
                                queue.add(neighbour);
                            }
                        }
                    }
                }
            }
        }

        // Break all connected ore blocks (skip the origin — already handled by the event)
        for (Block b : visited) {
            if (b.equals(origin)) continue;
            b.breakNaturally(player.getInventory().getItemInMainHand());
        }

        plugin.getCrownManager().applyCooldown(player, key);
        player.sendMessage("§7Vein mined §e" + visited.size() + "§7 blocks!");
    }

}
