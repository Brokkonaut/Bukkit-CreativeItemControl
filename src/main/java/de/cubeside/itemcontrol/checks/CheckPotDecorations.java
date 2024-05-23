package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;

public class CheckPotDecorations implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:pot_decorations");

    private boolean allow;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", true);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;

        ListTag sheardList = itemComponentsTag.getList(key);
        if (allow && sheardList != null) {
            while (sheardList.size() > 4) {
                sheardList.remove(sheardList.size() - 1);
                changed = true;
            }
            for (int i = sheardList.size() - 1; i >= 0; i--) {
                String s = sheardList.getString(i);
                if (s != null) {
                    NamespacedKey sheardKey = NamespacedKey.fromString(s);
                    Material m = sheardKey == null ? Registry.MATERIAL.get(sheardKey) : null;
                    if (m == null || (m != Material.BRICK && !Tag.ITEMS_DECORATED_POT_SHERDS.isTagged(m))) {
                        sheardList.remove(i);
                        changed = true;
                    }
                } else {
                    sheardList.remove(i);
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
