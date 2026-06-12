package com.crownSMP.utils;

import com.crownSMP.models.CrownType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CrownItemUtil {

    private static NamespacedKey CROWN_KEY;

    public static void init(JavaPlugin plugin) {
        CROWN_KEY = new NamespacedKey(plugin, "crown_type");
    }

    /** Build a Crown ItemStack for the given type. */
    public static ItemStack buildCrown(CrownType type) {
        ItemStack item = new ItemStack(Material.NETHERITE_HELMET);
        ItemMeta  meta = item.getItemMeta();

        meta.setDisplayName(type.getDisplayName());
        meta.setCustomModelData(type.getCustomModelData());
        meta.setUnbreakable(false); // durability can degrade — armour portion
        meta.addEnchant(Enchantment.PROTECTION, 4, true);
        meta.addEnchant(Enchantment.UNBREAKING,  3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        // Lore
        meta.setLore(buildLore(type));

        // Tag so we can always identify this as a Crown
        meta.getPersistentDataContainer().set(CROWN_KEY, PersistentDataType.STRING, type.name());

        item.setItemMeta(meta);
        return item;
    }

    /** Returns the CrownType for an item, or null if it isn't a Crown. */
    public static CrownType getCrownType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String tag = item.getItemMeta()
                         .getPersistentDataContainer()
                         .get(CROWN_KEY, PersistentDataType.STRING);
        if (tag == null) return null;
        try { return CrownType.valueOf(tag); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static boolean isCrown(ItemStack item) {
        return getCrownType(item) != null;
    }

    // ── Lore ───────────────────────────────────────────────────────────────
    private static List<String> buildLore(CrownType type) {
        return switch (type) {
            case TIDES   -> List.of("§bPassive: Water Breathing, Swift Trident",
                                    "§bAbility: Water Knockback");
            case INFERNO -> List.of("§cPassive: Fire Resistance, Lava Immunity",
                                    "§cAbility: Auto-Smelt, Fire Strike");
            case STORMS  -> List.of("§ePassive: Speed, Storm Haste",
                                    "§eAbility: Lightning Strike, Electric Arrow");
            case TITANS  -> List.of("§7Passive: Extra Hearts, Knockback Resistance",
                                    "§7Ability: Vein Mine, Earth Wall");
            case SHADOWS -> List.of("§5Passive: Reduced Mob Detection",
                                    "§5Ability: Short Teleport, Stealth");
            case FROST   -> List.of("§fPassive: Frost Walk",
                                    "§fAbility: Freeze, Slow");
            case BLOOD   -> List.of("§4Passive: Lifesteal",
                                    "§4Ability: Heal on Hit, Heal on Kill");
            case VOID    -> List.of("§8Passive: Minor All-Crown Bonuses",
                                    "§8Ability: Void Teleport");
        };
    }
}
