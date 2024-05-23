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
    public static boolean filterItem(CompoundTag clickedTag, GroupConfig group) {
        boolean modified = false;
        NamespacedKey id = NamespacedKey.fromString(clickedTag.getString("id", "air"));
        Material m = id == null ? null : Registry.MATERIAL.get(id);
        CompoundTag components = clickedTag.getCompound("components");
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
                            modified |= check.enforce(m, components, keyString);
                        } catch (Exception ex) {
                            Main.getInstance().getLogger().log(Level.SEVERE, "Could not execute check for " + key, ex);
                            Main.getInstance().getLogger().log(Level.SEVERE, Main.getInstance().getTools().getNbtUtils().writeString(clickedTag));
                            clickedTag.remove("components");
                            modified = true;
                        }
                    } else {
                        components.remove(keyString);
                        modified = true;
                    }
                }
            }
            if (components.size() == 0) {
                clickedTag.remove("components");
                modified = true;
            }
        }
        return modified;
    }
}
