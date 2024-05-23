package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckMapDecorations implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:map_decorations");

    private boolean allow;
    private int maxDecorations;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", true);
        maxDecorations = ConfigUtil.getOrCreate(data, "max_decorations", 6);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        CompoundTag decoCompound = itemComponentsTag.getCompound(key);
        if (allow && maxDecorations > 0 && decoCompound != null) {
            ArrayList<String> keys = new ArrayList<>(decoCompound.getAllKeys());
            if (keys.size() > maxDecorations) {
                for (int i = keys.size() - 1; i >= maxDecorations; i--) {
                    decoCompound.remove(keys.get(i));
                    changed = true;
                }
            }
        } else {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }
}
