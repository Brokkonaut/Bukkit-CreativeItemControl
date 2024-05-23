package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckBannerPatterns implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:banner_patterns");

    private int maxPatterns;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        maxPatterns = ConfigUtil.getOrCreate(data, "max_patterns", 6);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        ListTag patternList = itemComponentsTag.getList(key);
        if (patternList != null) {
            changed |= filterPatterns(patternList);
            if (patternList.isEmpty()) {
                itemComponentsTag.remove(key);
                changed = true;
            }
        } else {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }

    private boolean filterPatterns(ListTag patternList) {
        boolean changed = false;
        for (int i = patternList.size() - 1; i >= maxPatterns; i--) {
            patternList.remove(i);
            changed = true;
        }
        return changed;
    }
}
