package com.crownSMP.listeners;

import com.crownSMP.CrownSMPPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class LivesListener implements Listener {

    private final CrownSMPPlugin plugin;

    public LivesListener(CrownSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        boolean removed = plugin.getLivesManager().removeLife(player.getUniqueId());
        if (!removed) return;

        int remaining = plugin.getLivesManager().getLives(player.getUniqueId());

        if (remaining <= 0) {
            // Delay elimination slightly so the death event finishes cleanly
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    plugin.getLivesManager().eliminate(player), 20L);
        } else {
            player.sendMessage("§c§lYou died! §eLives remaining: §c" + remaining);
            player.getWorld().getPlayers().forEach(p ->
                    p.sendMessage("§e" + player.getName() + " §7has §c" + remaining + " §7lives remaining."));
        }
    }
}
