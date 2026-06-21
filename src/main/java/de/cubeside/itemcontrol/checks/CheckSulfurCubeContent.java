package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.ItemChecker;
import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckSulfurCubeContent implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:sulfur_cube_content");

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key, CheckData data) {
        boolean changed = false;

        CompoundTag stack = itemComponentsTag.getCompound(key);
        if (stack == null) {
            itemComponentsTag.remove(key);
            changed = true;
        } else {
            Boolean result = ItemChecker.filterItem(stack, group, data);
            changed |= result != Boolean.FALSE;
            if (result == null) {
                itemComponentsTag.remove(key);
            }
        }
        return changed;
    }
}
