package com.crownSMP.managers;

import com.crownSMP.CrownSMPPlugin;
import com.crownSMP.models.CrownType;
import com.crownSMP.utils.CrownItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class RevivalManager {

    private final CrownSMPPlugin plugin;

    // Tracks ongoing rituals: target UUID → set of participant UUIDs
    private final Map<UUID, Set<UUID>> rituals = new HashMap<>();

    public RevivalManager(CrownSMPPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * A living player initiates/joins a revival ritual for a target.
     * Returns true if the ritual completed.
     */
    public boolean joinRitual(Player participant, Player target) {
        if (!plugin.getLivesManager().isEliminated(target.getUniqueId())) {
            participant.sendMessage("§c" + target.getName() + " is not eliminated.");
            return false;
        }

        int radius   = plugin.getConfig().getInt("revival.ritual-radius", 5);
        int required = plugin.getConfig().getInt("revival.required-participants", 2);
        int reqCrowns = plugin.getConfig().getInt("revival.required-crowns", 1);

        // Count crowns within radius of participant
        List<CrownType> nearbyCrowns = new ArrayList<>();
        for (ItemStack item : getNearbyDroppedCrowns(participant.getLocation(), radius)) {
            CrownType t = CrownItemUtil.getCrownType(item);
            if (t != null) nearbyCrowns.add(t);
        }
        // Also count worn crowns of nearby players
        for (Player nearby : participant.getWorld().getPlayers()) {
            if (nearby.getLocation().distance(participant.getLocation()) <= radius) {
                CrownType t = plugin.getCrownManager().getCrownType(nearby.getUniqueId());
                if (t != null && !nearbyCrowns.contains(t)) nearbyCrowns.add(t);
            }
        }

        if (nearbyCrowns.size() < reqCrowns) {
            participant.sendMessage("§cNot enough Crowns present for the ritual. Need §e"
                    + reqCrowns + "§c, found §e" + nearbyCrowns.size() + "§c.");
            return false;
        }

        rituals.computeIfAbsent(target.getUniqueId(), k -> new HashSet<>())
               .add(participant.getUniqueId());

        Set<UUID> participants = rituals.get(target.getUniqueId());
        int count = participants.size();

        if (count < required) {
            Bukkit.broadcastMessage("§d§l[Revival] §r§d" + participant.getName()
                    + " is performing the revival ritual for §l" + target.getName()
                    + "§r§d! (" + count + "/" + required + " participants)");
            playRitualEffect(participant.getLocation());
            return false;
        }

        // Complete the ritual
        completeRevival(target, nearbyCrowns);
        rituals.remove(target.getUniqueId());
        return true;
    }

    private void completeRevival(Player target, List<CrownType> crownsToScatter) {
        int weakness = plugin.getConfig().getInt("revival.revival-weakness-seconds", 60);

        plugin.getLivesManager().setLives(target.getUniqueId(), 1);
        target.setGameMode(GameMode.SURVIVAL);
        target.setHealth(20.0);
        target.setFoodLevel(20);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
                weakness * 20, 1, false, true, true));

        Bukkit.broadcastMessage("§a§l[Revival] §r§a" + target.getName()
                + " has been revived! They return with 1 life.");
        Bukkit.broadcastMessage("§6§lThe Crowns are being scattered across the world...");

        // Scatter all crowns involved
        plugin.getCrownManager().scatterCrowns(crownsToScatter);

        // Firework / sound effect at target
        target.getWorld().playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        target.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, target.getLocation(), 100, 0.5, 1, 0.5);
    }

    /** Admin force-revive: skips ritual checks */
    public void forceRevive(Player target) {
        int weakness = plugin.getConfig().getInt("revival.revival-weakness-seconds", 60);
        plugin.getLivesManager().setLives(target.getUniqueId(), 1);
        target.setGameMode(GameMode.SURVIVAL);
        target.setHealth(20.0);
        target.setFoodLevel(20);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
                weakness * 20, 1));
        Bukkit.broadcastMessage("§a§l[Revival] §r§aAn admin has revived " + target.getName() + "!");
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private List<ItemStack> getNearbyDroppedCrowns(Location loc, int radius) {
        List<ItemStack> found = new ArrayList<>();
        loc.getWorld().getNearbyEntities(loc, radius, radius, radius).forEach(e -> {
            if (e instanceof org.bukkit.entity.Item item) {
                if (CrownItemUtil.isCrown(item.getItemStack())) {
                    found.add(item.getItemStack());
                }
            }
        });
        return found;
    }

    private void playRitualEffect(Location loc) {
        loc.getWorld().spawnParticle(Particle.ENCHANT, loc, 50, 1, 1, 1);
        loc.getWorld().playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.5f);
    }
}
