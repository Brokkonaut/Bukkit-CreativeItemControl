package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class CheckStoredEnchantments extends BaseCheckEnchantments {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:stored_enchantments");

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        if (!allowOnAllItems && material != Material.ENCHANTED_BOOK) {
            itemComponentsTag.remove(key);
            return true;
        }
        return super.enforce(group, material, itemComponentsTag, key);
    }
}
