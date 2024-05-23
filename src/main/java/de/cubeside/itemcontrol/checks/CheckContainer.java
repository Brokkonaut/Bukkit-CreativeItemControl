package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.ItemChecker;
import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckContainer implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:container");

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;

        ListTag itemList = itemComponentsTag.getList(key);
        if (itemList != null) {
            for (int i = itemList.size() - 1; i >= 0; i--) {
                CompoundTag entry = itemList.getCompound(i);
                if (entry == null) {
                    itemList.remove(i);
                    changed = true;
                } else {
                    CompoundTag stack = entry.getCompound("item");
                    if (stack == null) {
                        itemList.remove(i);
                        changed = true;
                    } else {
                        Boolean result = ItemChecker.filterItem(stack, group);
                        changed |= result != Boolean.FALSE;
                        if (result == null) {
                            itemList.remove(i);
                        }
                    }
                }
            }
            if (itemList.isEmpty()) {
                itemComponentsTag.remove(key);
                changed = true;
            }
        } else {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }
}
