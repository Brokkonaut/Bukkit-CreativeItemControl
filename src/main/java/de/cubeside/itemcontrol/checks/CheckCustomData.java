package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckCustomData implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:custom_data");

    private boolean allowAll;
    private List<String> allowedKeys;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allowAll = ConfigUtil.getOrCreate(data, "allow_all", false);
        allowedKeys = ConfigUtil.getOrCreate(data, "allowed_keys", List.of());
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        if (!allowAll) {
            CompoundTag compound = itemComponentsTag.getCompound(key);
            if (compound != null) {
                for (String customkey : new ArrayList<>(compound.getAllKeys())) {
                    if (!allowedKeys.contains(customkey)) {
                        compound.remove(customkey);
                        changed = true;
                    }
                }
                if (compound.size() == 0) {
                    itemComponentsTag.remove(key);
                    changed = true;
                }
            } else {
                itemComponentsTag.remove(key);
                changed = true;
            }
        }
        return changed;
    }
}
