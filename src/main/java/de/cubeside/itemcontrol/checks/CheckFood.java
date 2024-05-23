package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.ItemChecker;
import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckFood implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:food");

    private boolean allow;
    private boolean allowConvertsTo;
    private boolean allowEffects;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", false);
        allowConvertsTo = ConfigUtil.getOrCreate(data, "allow_converts_to", false);
        allowEffects = ConfigUtil.getOrCreate(data, "allow_effects", false);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;

        CompoundTag compound = itemComponentsTag.getCompound(key);
        if (allow && compound != null) {
            CompoundTag convertsStack = compound.getCompound("using_converts_to");
            if (convertsStack != null) {
                if (!allowConvertsTo) {
                    compound.remove("using_converts_to");
                    changed = true;
                } else {
                    Boolean result = ItemChecker.filterItem(convertsStack, group);
                    changed |= result != Boolean.FALSE;
                    if (result == null) {
                        compound.remove("using_converts_to");
                    }
                }
            }
            if (!allowEffects && compound.containsKey("effects")) {
                compound.remove("effects");
                changed = true;
            }
        } else {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }
}
