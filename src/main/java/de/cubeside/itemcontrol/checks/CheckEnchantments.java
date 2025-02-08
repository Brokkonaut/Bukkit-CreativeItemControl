package de.cubeside.itemcontrol.checks;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class CheckEnchantments extends BaseCheckEnchantments {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:enchantments");
    private static final Registry<Enchantment> ENCHANTMENT_REGISTY = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    protected boolean isInvalidOnItem(NamespacedKey enchantmentKey, Material material) {
        return !allowOnAllItems && !ENCHANTMENT_REGISTY.get(enchantmentKey).canEnchantItem(new ItemStack(material));
    }
}
