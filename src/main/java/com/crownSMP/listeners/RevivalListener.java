package com.crownSMP.listeners;

import com.crownSMP.CrownSMPPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * The Revival ritual is initiated via /crown revive <player> command.
 * This class exists as a hook for future block-based ritual detection
 * (e.g. standing in a circle of candles) if desired.
 */
public class RevivalListener implements Listener {

    private final CrownSMPPlugin plugin;

    public RevivalListener(CrownSMPPlugin plugin) {
        this.plugin = plugin;
    }
}
