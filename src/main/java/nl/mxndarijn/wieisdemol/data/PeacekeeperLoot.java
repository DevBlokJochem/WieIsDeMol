package nl.mxndarijn.wieisdemol.data;

import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public enum PeacekeeperLoot {
    HEAD(MxDefaultItemStackBuilder.create(Material.NETHERITE_HELMET)
            .setName(ChatColor.GOLD + "Peacekeeper's Helmet")
            .addLore(ChatColor.GOLD + "Peacekeeper-Item")
            .setUnbreakable(true)
            .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true)
            .addEnchantment(Enchantment.BINDING_CURSE, 1, true)
            .build(),
            EquipmentSlot.HEAD),
    CHESTPLATE(MxDefaultItemStackBuilder.create(Material.NETHERITE_CHESTPLATE)
            .setName(ChatColor.GOLD + "Peacekeeper's Chestplate")
            .addLore(ChatColor.GOLD + "Peacekeeper-Item")
            .setUnbreakable(true)
            .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true)
            .addEnchantment(Enchantment.BINDING_CURSE, 1, true)
            .build(),
            EquipmentSlot.CHEST),
    LEGGINGS(MxDefaultItemStackBuilder.create(Material.NETHERITE_LEGGINGS)
            .setName(ChatColor.GOLD + "Peacekeeper's Leggings")
            .addLore(ChatColor.GOLD + "Peacekeeper-Item")
            .setUnbreakable(true)
            .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true)
            .addEnchantment(Enchantment.BINDING_CURSE, 1, true)
            .build(),
            EquipmentSlot.LEGS),

    BOOTS(MxDefaultItemStackBuilder.create(Material.NETHERITE_BOOTS)
            .setName(ChatColor.GOLD + "Peacekeeper's Boots")
            .addLore(ChatColor.GOLD + "Peacekeeper-Item")
            .setUnbreakable(true)
            .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true)
            .addEnchantment(Enchantment.BINDING_CURSE, 1, true)
            .build(),
            EquipmentSlot.FEET),

    SWORD(MxDefaultItemStackBuilder.create(Material.NETHERITE_SWORD)
            .setName(ChatColor.GOLD + "Peacekeeper's Sword")
            .addLore(ChatColor.GOLD + "Peacekeeper-Item")
            .setUnbreakable(true)
            .addEnchantment(Enchantment.DAMAGE_ALL, 5, true)
            .build(),
            null),
    ;


    private final ItemStack is;
    private final EquipmentSlot slot;

    PeacekeeperLoot(ItemStack is, EquipmentSlot slot) {
        this.is = is;
        this.slot = slot;
    }

    public ItemStack getIs() {
        return is;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }
}


