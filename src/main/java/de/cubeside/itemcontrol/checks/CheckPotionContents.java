package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckPotionContents implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:potion_contents");

    private boolean allow;
    private boolean customColor;
    private boolean customEffects;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", true);
        customColor = ConfigUtil.getOrCreate(data, "custom_color", false);
        customEffects = ConfigUtil.getOrCreate(data, "custom_effects", false);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        CompoundTag compound = itemComponentsTag.getCompound(key);
        if (!allow || (compound == null && itemComponentsTag.getString(key) == null)) {
            itemComponentsTag.remove(key);
            changed = true;
        } else if (compound != null) {
            if (!customColor && compound.containsKey("custom_color")) {
                compound.remove("custom_color");
                changed = true;
            }
            if (!customEffects && compound.containsKey("custom_effects")) {
                compound.remove("custom_effects");
                changed = true;
            }
        }
        return changed;
    }
}
