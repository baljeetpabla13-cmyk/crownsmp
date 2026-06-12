package com.crownSMP.listeners;

import com.crownSMP.CrownSMPPlugin;
import com.crownSMP.models.CrownType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class CrownAbilityListener implements Listener {

    private final CrownSMPPlugin plugin;

    public CrownAbilityListener(CrownSMPPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Melee hit abilities ───────────────────────────────────────────────

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        CrownType type = plugin.getCrownManager().getCrownType(attacker.getUniqueId());
        if (type == null) return;

        switch (type) {
            case TIDES  -> handleTidesHit(attacker, e);
            case BLOOD  -> handleBloodHit(attacker, e);
        }
    }

    // TIDES — water knockback on hit
    private void handleTidesHit(Player attacker, EntityDamageByEntityEvent e) {
        String key = "tides.water-knockback";
        if (!plugin.getCrownManager().checkCooldown(attacker, key)) return;

        double strength = plugin.getConfig().getDouble("abilities.tides.knockback-strength", 2.5);
        Entity target = e.getEntity();
        Vector dir = target.getLocation().toVector()
                .subtract(attacker.getLocation().toVector())
                .normalize().multiply(strength).setY(0.4);
        target.setVelocity(dir);
        target.getWorld().spawnParticle(Particle.SPLASH, target.getLocation(), 30, 0.3, 0.3, 0.3);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1f, 1f);

        plugin.getCrownManager().applyCooldown(attacker, key);
    }

    // BLOOD — lifesteal on hit
    private void handleBloodHit(Player attacker, EntityDamageByEntityEvent e) {
        double heal = plugin.getConfig().getDouble("abilities.blood.lifesteal-per-hit", 1.0);
        double newHp = Math.min(attacker.getMaxHealth(), attacker.getHealth() + heal);
        attacker.setHealth(newHp);
        attacker.getWorld().spawnParticle(Particle.HEART, attacker.getLocation().add(0, 1, 0), 3);
    }

    // ── Right-click abilities (sneak + right-click) ───────────────────────

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!player.isSneaking()) return;
        if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
         && e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        CrownType type = plugin.getCrownManager().getCrownType(player.getUniqueId());
        if (type == null) return;

        switch (type) {
            case INFERNO -> triggerFireStrike(player);
            case STORMS  -> triggerLightningStrike(player);
            case TITANS  -> triggerEarthWall(player);
            case SHADOWS -> triggerShadowTeleport(player);
            case FROST   -> triggerFreeze(player);
            case VOID    -> triggerVoidTeleport(player);
        }
    }

    // INFERNO — fire strike on looked-at entity
    private void triggerFireStrike(Player player) {
        String key = "inferno.fire-strike";
        if (!plugin.getCrownManager().checkCooldown(player, key)) return;

        Entity target = getTargetEntity(player, 10);
        if (target == null) { player.sendMessage("§cNo target in range."); return; }

        int dur = plugin.getConfig().getInt("abilities.inferno.fire-strike-duration-ticks", 100);
        target.setFireTicks(dur);
        target.getWorld().strikeLightningEffect(target.getLocation());
        player.sendMessage("§cFire strike!");
        plugin.getCrownManager().applyCooldown(player, key);
    }

    // STORMS — lightning strike
    private void triggerLightningStrike(Player player) {
        String key = "storms.lightning-strike";
        if (!plugin.getCrownManager().checkCooldown(player, key)) return;

        Entity target = getTargetEntity(player, 20);
        if (target == null) { player.sendMessage("§cNo target in range."); return; }

        double dmg = plugin.getConfig().getDouble("abilities.storms.lightning-damage", 6.0);
        player.getWorld().strikeLightning(target.getLocation());
        if (target instanceof LivingEntity le) le.damage(dmg, player);
        player.sendMessage("§eLightning strike!");
        plugin.getCrownManager().applyCooldown(player, key);
    }

    // TITANS — earth wall
    private void triggerEarthWall(Player player) {
        String key = "titans.earth-wall";
        if (!plugin.getCrownManager().checkCooldown(player, key)) return;

        int size = plugin.getConfig().getInt("abilities.titans.wall-size", 3);
        int durTicks = plugin.getConfig().getInt("abilities.titans.wall-duration-ticks", 100);

        Location base = player.getLocation().getBlock().getRelative(
                player.getFacing()).getLocation();
        List<Block> wallBlocks = new ArrayList<>();

        for (int y = 0; y < 5; y++) {
            for (int x = -size / 2; x <= size / 2; x++) {
                Block b = base.clone().add(x, y, 0).getBlock();
                if (b.getType() == Material.AIR) {
                    b.setType(Material.STONE);
                    wallBlocks.add(b);
                }
            }
        }

        // Remove after duration
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                wallBlocks.forEach(b -> { if (b.getType() == Material.STONE) b.setType(Material.AIR); }),
                durTicks);

        player.sendMessage("§7Earth wall raised!");
        plugin.getCrownManager().applyCooldown(player, key);
    }

    // SHADOWS — short teleport in look direction
    private void triggerShadowTeleport(Player player) {
        String key = "shadows.teleport";
        if (!plugin.getCrownManager().checkCooldown(player, key)) return;

        int range = plugin.getConfig().getInt("abilities.shadows.teleport-range", 16);
        Location dest = player.getTargetBlockExact(range) != null
                ? player.getTargetBlockExact(range).getLocation().add(0, 1, 0)
                : player.getLocation().add(player.getLocation().getDirection().multiply(range));

        player.teleport(dest);
        player.getWorld().spawnParticle(Particle.SMOKE, dest, 20, 0.3, 0.3, 0.3);
        player.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        player.sendMessage("§5Shadow step!");
        plugin.getCrownManager().applyCooldown(player, key);
    }

    // FROST — freeze nearby entities
    private void triggerFreeze(Player player) {
        String key = "frost.freeze";
        if (!plugin.getCrownManager().checkCooldown(player, key)) return;

        int dur  = plugin.getConfig().getInt("abilities.frost.freeze-duration-ticks", 80);
        int slow = plugin.getConfig().getInt("abilities.frost.slow-amplifier", 2);

        player.getWorld().getNearbyEntities(player.getLocation(), 8, 8, 8).forEach(e -> {
            if (e instanceof LivingEntity le && e != player) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, slow));
                le.setFreezeTicks(dur);
                le.getWorld().spawnParticle(Particle.SNOWFLAKE, le.getLocation(), 15, 0.5, 0.5, 0.5);
            }
        });
        player.sendMessage("§fFreezing blast!");
        plugin.getCrownManager().applyCooldown(player, key);
    }

    // VOID — long-range teleport to looked-at block
    private void triggerVoidTeleport(Player player) {
        String key = "void.void-teleport";
        if (!plugin.getCrownManager().checkCooldown(player, key)) return;

        int range = plugin.getConfig().getInt("abilities.void.teleport-range", 32);
        Location from = player.getLocation();
        Block target = player.getTargetBlockExact(range);
        Location dest = (target != null)
                ? target.getLocation().add(0, 1, 0)
                : from.clone().add(from.getDirection().multiply(range));

        player.getWorld().spawnParticle(Particle.PORTAL, from, 40, 0.3, 0.5, 0.3);
        player.teleport(dest);
        player.getWorld().spawnParticle(Particle.PORTAL, dest, 40, 0.3, 0.5, 0.3);
        player.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        player.sendMessage("§8Void step!");
        plugin.getCrownManager().applyCooldown(player, key);
    }

    // ── Stealth toggle (SHADOWS — sneak) ─────────────────────────────────

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if (!plugin.getCrownManager().wearsCrown(player.getUniqueId(), CrownType.SHADOWS)) return;
        // Invisibility is always-on via ticker; sneak activates deeper stealth
        if (e.isSneaking()) {
            String key = "shadows.stealth";
            if (!plugin.getCrownManager().checkCooldown(player, key)) return;
            int dur = plugin.getConfig().getInt("abilities.shadows.stealth-duration-ticks", 100);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, dur, 0, false, false));
            player.sendMessage("§5You fade into the shadows...");
            plugin.getCrownManager().applyCooldown(player, key);
        }
    }

    // ── Electric Arrow (STORMS) ───────────────────────────────────────────

    @EventHandler
    public void onProjectileHit(org.bukkit.event.entity.ProjectileHitEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player shooter)) return;
        if (!plugin.getCrownManager().wearsCrown(shooter.getUniqueId(), CrownType.STORMS)) return;
        if (!(e.getEntity() instanceof Arrow)) return;
        if (e.getHitEntity() == null) return;

        String key = "storms.electric-arrow";
        if (plugin.getCrownManager().getCooldowns().isOnCooldown(shooter.getUniqueId(), key)) return;

        shooter.getWorld().strikeLightningEffect(e.getHitEntity().getLocation());
        if (e.getHitEntity() instanceof LivingEntity le) {
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
        }
        plugin.getCrownManager().applyCooldown(shooter, key);
    }

    // ── Kill events ───────────────────────────────────────────────────────

    @EventHandler
    public void onKill(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player killer)) return;
        if (!plugin.getCrownManager().wearsCrown(killer.getUniqueId(), CrownType.BLOOD)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;
        if (target.getHealth() - e.getFinalDamage() > 0) return; // not a kill

        double heal = plugin.getConfig().getDouble("abilities.blood.kill-heal", 4.0);
        killer.setHealth(Math.min(killer.getMaxHealth(), killer.getHealth() + heal));
        killer.getWorld().spawnParticle(Particle.HEART, killer.getLocation().add(0, 2, 0), 5);
    }

    // ── Utility ───────────────────────────────────────────────────────────

    private Entity getTargetEntity(Player player, double range) {
        for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), range, range, range)) {
            if (e == player) continue;
            if (e instanceof LivingEntity) {
                return e;
            }
        }
        return null;
    }
}
