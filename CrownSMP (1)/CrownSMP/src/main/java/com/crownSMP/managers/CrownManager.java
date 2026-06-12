package com.crownSMP.managers;

import com.crownSMP.CrownSMPPlugin;
import com.crownSMP.models.CrownType;
import com.crownSMP.utils.CooldownManager;
import com.crownSMP.utils.CrownItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CrownManager {

    private final CrownSMPPlugin plugin;
    private final CooldownManager cooldowns = new CooldownManager();

    // Maps player UUID → the CrownType they currently wear
    private final Map<UUID, CrownType> wearers = new HashMap<>();

    public CrownManager(CrownSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadCrowns() {
        CrownItemUtil.init(plugin);
        // Re-scan online players on reload
        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemStack helmet = p.getInventory().getHelmet();
            CrownType type = CrownItemUtil.getCrownType(helmet);
            if (type != null) wearers.put(p.getUniqueId(), type);
        }
    }

    // ── Wearer tracking ───────────────────────────────────────────────────

    public void setWearer(UUID uuid, CrownType type) {
        wearers.put(uuid, type);
    }

    public void removeWearer(UUID uuid) {
        wearers.remove(uuid);
    }

    public CrownType getCrownType(UUID uuid) {
        return wearers.get(uuid);
    }

    public boolean wearsCrown(UUID uuid) {
        return wearers.containsKey(uuid);
    }

    public boolean wearsCrown(UUID uuid, CrownType type) {
        return type.equals(wearers.get(uuid));
    }

    // ── Crown giving / removing ────────────────────────────────────────────

    public void giveCrown(Player player, CrownType type) {
        ItemStack crown = CrownItemUtil.buildCrown(type);
        player.getInventory().addItem(crown);
        player.sendMessage("§6You have received the " + type.getDisplayName() + "§6!");
    }

    public void removeCrown(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (CrownItemUtil.isCrown(helmet)) {
            player.getInventory().setHelmet(null);
            removeWearer(player.getUniqueId());
            player.sendMessage("§cYour Crown has been removed.");
        }
    }

    // ── Scatter ────────────────────────────────────────────────────────────

    /**
     * Scatter the given crowns to random locations in the overworld.
     * Called after a revival ritual.
     */
    public void scatterCrowns(List<CrownType> types) {
        int radius = plugin.getConfig().getInt("scatter.radius", 5000);
        int minY   = plugin.getConfig().getInt("scatter.min-y", 64);
        int maxY   = plugin.getConfig().getInt("scatter.max-y", 120);
        World world = Bukkit.getWorlds().get(0);
        Random rand = new Random();

        // Remove from current wearers first
        for (CrownType type : types) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (wearsCrown(p.getUniqueId(), type)) {
                    removeCrown(p);
                    break;
                }
            }
        }

        for (CrownType type : types) {
            int x = rand.nextInt(radius * 2) - radius;
            int z = rand.nextInt(radius * 2) - radius;
            int y = minY + rand.nextInt(Math.max(1, maxY - minY));

            Location loc = new Location(world, x + 0.5, y, z + 0.5);
            world.dropItem(loc, CrownItemUtil.buildCrown(type));

            Bukkit.broadcastMessage("§6§lA " + type.getDisplayName()
                    + " §6§lhas been scattered into the world!");
        }
    }

    /** Scatter ALL crown types. */
    public void scatterAll() {
        scatterCrowns(Arrays.asList(CrownType.values()));
    }

    // ── Cooldowns ─────────────────────────────────────────────────────────

    public CooldownManager getCooldowns() { return cooldowns; }

    public boolean checkCooldown(Player player, String ability) {
        if (cooldowns.isOnCooldown(player.getUniqueId(), ability)) {
            int rem = cooldowns.getRemaining(player.getUniqueId(), ability);
            player.sendMessage("§cAbility on cooldown! §7(" + rem + "s remaining)");
            return false;
        }
        return true;
    }

    public void applyCooldown(Player player, String ability) {
        int seconds = plugin.getConfig().getInt("cooldowns." + ability, 10);
        cooldowns.setCooldown(player.getUniqueId(), ability, seconds);
    }
}
