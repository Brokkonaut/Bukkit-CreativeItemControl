package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckInstrument implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:instrument");

    private boolean allowCustom;

    private boolean allowCustomTextFormating;

    private int allowCustomNameMaxLength;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allowCustom = ConfigUtil.getOrCreate(data, "allow_custom", true);
        allowCustomTextFormating = ConfigUtil.getOrCreate(data, "allow_custom_text_formating", false);
        allowCustomNameMaxLength = ConfigUtil.getOrCreate(data, "allow_custom_name_max_length", 4000);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        if (!allowCustom && itemComponentsTag.getString(key) == null) {
            itemComponentsTag.remove(key);
            changed = true;
        } else {
            CompoundTag compound = itemComponentsTag.getCompound(key);
            if (compound != null) {
                if (BaseCheckName.enforce(compound, "description", true, allowCustomTextFormating, allowCustomNameMaxLength, group.getMaxComponentExpansions())) {
                    changed = true;
                }
            }
        }
        return changed;
    }
}
