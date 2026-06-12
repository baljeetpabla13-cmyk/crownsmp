package com.crownSMP.models;

import org.bukkit.ChatColor;

public enum CrownType {
    TIDES      ("Crown of Tides",    ChatColor.AQUA,         1001),
    INFERNO    ("Crown of Inferno",  ChatColor.RED,          1002),
    STORMS     ("Crown of Storms",   ChatColor.YELLOW,       1003),
    TITANS     ("Crown of Titans",   ChatColor.DARK_GRAY,    1004),
    SHADOWS    ("Crown of Shadows",  ChatColor.DARK_PURPLE,  1006),
    FROST      ("Crown of Frost",    ChatColor.WHITE,        1007),
    BLOOD      ("Crown of Blood",    ChatColor.DARK_RED,     1008),
    VOID       ("Crown of the Void", ChatColor.BLACK,        1009);

    private final String displayName;
    private final ChatColor color;
    private final int customModelData;

    CrownType(String displayName, ChatColor color, int customModelData) {
        this.displayName    = displayName;
        this.color          = color;
        this.customModelData = customModelData;
    }

    public String getDisplayName()  { return color + displayName; }
    public String getRawName()      { return displayName; }
    public ChatColor getColor()     { return color; }
    public int getCustomModelData() { return customModelData; }
}
