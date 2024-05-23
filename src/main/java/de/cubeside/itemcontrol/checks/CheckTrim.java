package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckTrim implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:trim");

    private boolean allow;
    private boolean allowHidden;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", true);
        allowHidden = ConfigUtil.getOrCreate(data, "allow_hidden", false);
    }

    @Override
    public boolean enforce(Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        CompoundTag compound = itemComponentsTag.getCompound(key);
        if (!allow || compound == null) {
            itemComponentsTag.remove(key);
            changed = true;
        } else {
            if (!allowHidden && compound.containsKey("show_in_tooltip")) {
                compound.remove("show_in_tooltip");
                changed = true;
            }
        }
        return changed;
    }
}
