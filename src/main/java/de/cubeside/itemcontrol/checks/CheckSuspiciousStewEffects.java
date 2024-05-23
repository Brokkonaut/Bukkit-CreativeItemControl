package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckSuspiciousStewEffects implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:suspicious_stew_effects");

    private boolean allow;
    private int maxDuration;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", true);
        maxDuration = ConfigUtil.getOrCreate(data, "max_duration", 160);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        ListTag list = itemComponentsTag.getList(key);
        if (!allow || list == null) {
            itemComponentsTag.remove(key);
            changed = true;
        } else {
            for (int i = list.size() - 1; i >= 0; i--) {
                CompoundTag compound = list.getCompound(i);
                if (compound == null) {
                    list.remove(i);
                } else {
                    int duration = compound.getInt("duration", 0);
                    if (maxDuration > 0 && duration > maxDuration) {
                        compound.remove("duration");
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
}
