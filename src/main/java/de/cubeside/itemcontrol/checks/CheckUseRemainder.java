package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.ItemChecker;
import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckUseRemainder implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:use_remainder");

    private boolean allow;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", false);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;

        CompoundTag compound = itemComponentsTag.getCompound(key);
        if (allow && compound != null) {
            Boolean result = ItemChecker.filterItem(compound, group);
            changed |= result != Boolean.FALSE;
            if (result == null) {
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
