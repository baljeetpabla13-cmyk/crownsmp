package com.crownSMP.listeners;

import com.crownSMP.CrownSMPPlugin;
import com.crownSMP.models.CrownType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class CrownPassiveListener implements Listener {

    private static final UUID TITANS_HP_UUID   = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID TITANS_KBR_UUID  = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final String TITANS_HP_MOD  = "crownsmp:titans_hp";
    private static final String TITANS_KBR_MOD = "crownsmp:titans_kbr";

    private final CrownSMPPlugin plugin;

    public CrownPassiveListener(CrownSMPPlugin plugin) {
        this.plugin = plugin;
        startPassiveTicker();
    }

    /** Runs every 4 seconds to re-apply potion-based passives (they expire otherwise). */
    private void startPassiveTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    CrownType type = plugin.getCrownManager().getCrownType(p.getUniqueId());
                    if (type == null) continue;
                    tickPassive(p, type);
                }
            }
        }.runTaskTimer(plugin, 20L, 80L); // every 4 s
    }

    private void tickPassive(Player player, CrownType type) {
        int dur = 200; // 10 s, refreshed every 4 s — always active
        switch (type) {
            case TIDES ->
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, dur, 0, true, false));
            case INFERNO -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, dur, 0, true, false));
            }
            case STORMS -> {
                int amp = plugin.getConfig().getInt("abilities.storms.speed-amplifier", 1);
                // Extra speed during thunderstorm
                if (player.getWorld().isThundering()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur, amp, true, false));
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur, 0, true, false));
                }
            }
            case SHADOWS ->
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, dur, 0, true, false));
            case VOID -> {
                // Minor bonuses from all crown types
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, dur, 0, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, dur, 0, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur, 0, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, dur, 0, true, false));
            }
            default -> {}
        }
    }

    // ── Apply / Remove attribute-based passives ───────────────────────────

    public static void applyTidesPassive(Player player) {
        // Water Breathing applied via ticker
    }
    public static void removeTidesPassive(Player player) {
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
    }

    public static void applyInfernoPassive(Player player) {
        // Fire Resistance applied via ticker
    }
    public static void removeInfernoPassive(Player player) {
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
    }

    public static void applyStormsPassive(Player player) {
        // Speed applied via ticker
    }
    public static void removeStormsPassive(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    public static void applyTitansPassive(Player player) {
        int extraHearts = CrownSMPPlugin.getInstance().getConfig()
                .getInt("abilities.titans.extra-hearts", 4);
        double extraHp = extraHearts * 2.0;

        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            // Remove if already present
            attr.getModifiers().removeIf(m -> m.getName().equals(TITANS_HP_MOD));
            AttributeModifier hpMod = new AttributeModifier(
                    TITANS_HP_UUID, TITANS_HP_MOD, extraHp,
                    AttributeModifier.Operation.ADD_NUMBER);
            attr.addModifier(hpMod);
        }

        var kbAttr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.getModifiers().removeIf(m -> m.getName().equals(TITANS_KBR_MOD));
            AttributeModifier kbMod = new AttributeModifier(
                    TITANS_KBR_UUID, TITANS_KBR_MOD, 0.5,
                    AttributeModifier.Operation.ADD_NUMBER);
            kbAttr.addModifier(kbMod);
        }
    }

    public static void removeTitansPassive(Player player) {
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) attr.getModifiers().removeIf(m -> m.getName().equals(TITANS_HP_MOD));

        var kbAttr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) kbAttr.getModifiers().removeIf(m -> m.getName().equals(TITANS_KBR_MOD));
    }

    public static void applyShadowsPassive(Player player) {
        // Invisibility applied via ticker
    }
    public static void removeShadowsPassive(Player player) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    public static void applyVoidPassive(Player player) {
        // Applied via ticker
    }
}
