package com.crownSMP.listeners;

import com.crownSMP.CrownSMPPlugin;
import com.crownSMP.models.CrownType;
import com.crownSMP.utils.CrownItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class CrownEquipListener implements Listener {

    private final CrownSMPPlugin plugin;

    public CrownEquipListener(CrownSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        ItemStack helmet = player.getInventory().getHelmet();
        CrownType type = CrownItemUtil.getCrownType(helmet);
        if (type != null) {
            plugin.getCrownManager().setWearer(player.getUniqueId(), type);
            applyPassives(player, type);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getCrownManager().removeWearer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        // Detect helmet slot changes
        boolean isHelmetSlot = (e.getSlotType() == InventoryType.SlotType.ARMOR
                && e.getSlot() == 39)
                || e.getSlotType() == InventoryType.SlotType.QUICKBAR;

        // Re-evaluate after the tick
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            ItemStack helmet = player.getInventory().getHelmet();
            CrownType newType = CrownItemUtil.getCrownType(helmet);
            CrownType oldType = plugin.getCrownManager().getCrownType(player.getUniqueId());

            if (newType == oldType) return;

            if (oldType != null) {
                removePassives(player, oldType);
                plugin.getCrownManager().removeWearer(player.getUniqueId());
            }
            if (newType != null) {
                plugin.getCrownManager().setWearer(player.getUniqueId(), newType);
                applyPassives(player, newType);
                player.sendMessage("§6You equipped the " + newType.getDisplayName() + "§6!");
            }
        });
    }

    // ── Passives ──────────────────────────────────────────────────────────

    public static void applyPassives(Player player, CrownType type) {
        switch (type) {
            case TIDES   -> CrownPassiveListener.applyTidesPassive(player);
            case INFERNO -> CrownPassiveListener.applyInfernoPassive(player);
            case STORMS  -> CrownPassiveListener.applyStormsPassive(player);
            case TITANS  -> CrownPassiveListener.applyTitansPassive(player);
            case SHADOWS -> CrownPassiveListener.applyShadowsPassive(player);
            case FROST   -> {} // handled via FrostWalk enchant on item
            case BLOOD   -> {} // handled via event
            case VOID    -> CrownPassiveListener.applyVoidPassive(player);
        }
    }

    public static void removePassives(Player player, CrownType type) {
        switch (type) {
            case TIDES   -> CrownPassiveListener.removeTidesPassive(player);
            case INFERNO -> CrownPassiveListener.removeInfernoPassive(player);
            case STORMS  -> CrownPassiveListener.removeStormsPassive(player);
            case TITANS  -> CrownPassiveListener.removeTitansPassive(player);
            case SHADOWS -> CrownPassiveListener.removeShadowsPassive(player);
            default      -> {}
        }
    }
}
