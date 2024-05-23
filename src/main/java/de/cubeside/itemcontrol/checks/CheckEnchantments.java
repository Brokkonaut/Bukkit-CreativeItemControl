package de.cubeside.itemcontrol.checks;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;

public class CheckEnchantments extends BaseCheckEnchantments {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:enchantments");

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    protected boolean isInvalidOnItem(NamespacedKey enchantmentKey, Material material) {
        return !allowOnAllItems && !Registry.ENCHANTMENT.get(enchantmentKey).canEnchantItem(new ItemStack(material));
    }
}
