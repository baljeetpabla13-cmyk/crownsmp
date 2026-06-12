package com.crownSMP.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    // key = playerUUID + ":" + abilityKey
    private final Map<String, Long> cooldowns = new HashMap<>();

    public boolean isOnCooldown(UUID player, String ability) {
        String key = player + ":" + ability;
        Long expires = cooldowns.get(key);
        if (expires == null) return false;
        if (System.currentTimeMillis() >= expires) {
            cooldowns.remove(key);
            return false;
        }
        return true;
    }

    public void setCooldown(UUID player, String ability, int seconds) {
        cooldowns.put(player + ":" + ability,
                      System.currentTimeMillis() + (seconds * 1000L));
    }

    /** Returns remaining seconds, or 0 if not on cooldown. */
    public int getRemaining(UUID player, String ability) {
        String key = player + ":" + ability;
        Long expires = cooldowns.get(key);
        if (expires == null) return 0;
        long remaining = expires - System.currentTimeMillis();
        return remaining <= 0 ? 0 : (int) Math.ceil(remaining / 1000.0);
    }
}
