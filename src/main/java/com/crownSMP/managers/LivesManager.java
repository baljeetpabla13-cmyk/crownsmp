package com.crownSMP.managers;

import com.crownSMP.CrownSMPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LivesManager {

    private final CrownSMPPlugin plugin;
    private final Map<UUID, Integer> lives = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public LivesManager(CrownSMPPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Persistence ───────────────────────────────────────────────────────

    public void loadData() {
        dataFile   = new File(plugin.getDataFolder(), "lives.yml");
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig.contains("lives")) {
            for (String key : dataConfig.getConfigurationSection("lives").getKeys(false)) {
                lives.put(UUID.fromString(key), dataConfig.getInt("lives." + key));
            }
        }
    }

    public void saveData() {
        lives.forEach((uuid, count) ->
                dataConfig.set("lives." + uuid.toString(), count));
        try { dataConfig.save(dataFile); }
        catch (IOException e) { plugin.getLogger().warning("Failed to save lives.yml: " + e.getMessage()); }
    }

    // ── Core API ──────────────────────────────────────────────────────────

    public int getLives(UUID uuid) {
        return lives.getOrDefault(uuid,
               plugin.getConfig().getInt("lives.starting-lives", 3));
    }

    public void setLives(UUID uuid, int amount) {
        lives.put(uuid, Math.max(0, amount));
        saveData();
    }

    public void addLives(UUID uuid, int amount) {
        setLives(uuid, getLives(uuid) + amount);
    }

    public boolean removeLife(UUID uuid) {
        int current = getLives(uuid);
        if (current <= 0) return false; // already eliminated
        setLives(uuid, current - 1);
        return true;
    }

    public boolean isEliminated(UUID uuid) {
        return getLives(uuid) <= 0;
    }

    // ── Elimination ───────────────────────────────────────────────────────

    public void eliminate(Player player) {
        String mode = plugin.getConfig().getString("lives.on-elimination", "SPECTATOR");

        Bukkit.broadcastMessage("§c§l" + player.getName()
                + " has been eliminated! They have no lives remaining.");

        // Remove their crown if they have one
        plugin.getCrownManager().removeCrown(player);

        if ("BAN".equalsIgnoreCase(mode)) {
            int duration = plugin.getConfig().getInt("lives.ban-duration-seconds", -1);
            if (duration <= 0) {
                player.banPlayer("§cEliminated — no lives remaining.");
            } else {
                long until = System.currentTimeMillis() + (duration * 1000L);
                player.getServer().getBanList(org.bukkit.BanList.Type.NAME)
                      .addBan(player.getName(), "Eliminated", new java.util.Date(until), "CrownSMP");
            }
            player.kickPlayer("§cYou have been eliminated!");
        } else {
            // SPECTATOR mode
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("§cYou have been eliminated! You are now in spectator mode.");
            player.sendMessage("§eAnother player may perform the revival ritual to bring you back.");
        }
    }
}
