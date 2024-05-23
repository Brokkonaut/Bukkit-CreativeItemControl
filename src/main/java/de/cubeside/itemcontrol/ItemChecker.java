package de.cubeside.itemcontrol;

import de.cubeside.itemcontrol.checks.ComponentCheck;
import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.nmsutils.nbt.CompoundTag;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

public class ItemChecker {
    public static Boolean filterItem(CompoundTag stack, GroupConfig group) {
        boolean modified = false;
        NamespacedKey id = NamespacedKey.fromString(stack.getString("id", "air"));
        Material m = id == null ? null : Registry.MATERIAL.get(id);
        if (group.getForbiddenItems().contains(m)) {
            stack.clear();
            return null;
        }
        CompoundTag components = stack.getCompound("components");
        if (components != null && !group.isAllowAllComponents()) {
            for (String keyString : new ArrayList<>(components.getAllKeys())) {
                NamespacedKey key = NamespacedKey.fromString(keyString);
                if (key == null) {
                    components.remove(keyString);
                    modified = true;
                } else {
                    ComponentCheck check = group.getComponentHandler(key);
                    if (check != null) {
                        try {
                            modified |= check.enforce(group, m, components, keyString);
                        } catch (Exception ex) {
                            Main.getInstance().getLogger().log(Level.SEVERE, "Could not execute check for " + key, ex);
                            Main.getInstance().getLogger().log(Level.SEVERE, Main.getInstance().getTools().getNbtUtils().writeString(stack));
                            stack.remove("components");
                            modified = true;
                        }
                    } else {
                        components.remove(keyString);
                        modified = true;
                    }
                }
            }
            if (components.size() == 0) {
                stack.remove("components");
                modified = true;
            }
        }
        return modified;
    }
}
