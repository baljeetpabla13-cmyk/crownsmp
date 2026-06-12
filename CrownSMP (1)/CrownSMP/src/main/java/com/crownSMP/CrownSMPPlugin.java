package com.crownSMP;

import com.crownSMP.commands.CrownCommand;
import com.crownSMP.listeners.*;
import com.crownSMP.managers.CrownManager;
import com.crownSMP.managers.LivesManager;
import com.crownSMP.managers.RevivalManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CrownSMPPlugin extends JavaPlugin {

    private static CrownSMPPlugin instance;
    private CrownManager crownManager;
    private LivesManager livesManager;
    private RevivalManager revivalManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        crownManager  = new CrownManager(this);
        livesManager  = new LivesManager(this);
        revivalManager = new RevivalManager(this);

        crownManager.loadCrowns();
        livesManager.loadData();

        registerListeners();
        registerCommands();

        getLogger().info("CrownSMP enabled!");
    }

    @Override
    public void onDisable() {
        livesManager.saveData();
        getLogger().info("CrownSMP disabled.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CrownEquipListener(this),   this);
        getServer().getPluginManager().registerEvents(new CrownPassiveListener(this),  this);
        getServer().getPluginManager().registerEvents(new CrownAbilityListener(this),  this);
        getServer().getPluginManager().registerEvents(new LivesListener(this),         this);
        getServer().getPluginManager().registerEvents(new RevivalListener(this),       this);
        getServer().getPluginManager().registerEvents(new MiningListener(this),        this);
    }

    private void registerCommands() {
        CrownCommand cmd = new CrownCommand(this);
        getCommand("crown").setExecutor(cmd);
        getCommand("crown").setTabCompleter(cmd);
    }

    public static CrownSMPPlugin getInstance() { return instance; }
    public CrownManager getCrownManager()       { return crownManager; }
    public LivesManager getLivesManager()       { return livesManager; }
    public RevivalManager getRevivalManager()   { return revivalManager; }
}
