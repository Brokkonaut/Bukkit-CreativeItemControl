package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckEntityData implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:entity_data");

    private boolean allow;

    private boolean allowPaintings;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", false);
        allowPaintings = ConfigUtil.getOrCreate(data, "allowPaintings", true);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        CompoundTag entityData = itemComponentsTag.getCompound(key);
        if (material == Material.PAINTING && entityData != null) {
            if (allowPaintings) {
                String id = entityData.getString("id");
                if (id == null || !(id.equals("minecraft:painting") || id.equals("painting"))) {
                    itemComponentsTag.remove(key);
                    changed = true;
                } else {
                    String variant = entityData.getString("variant");
                    NamespacedKey variantKey = variant == null ? null : NamespacedKey.fromString(variant);
                    if (variantKey == null || RegistryAccess.registryAccess().getRegistry(RegistryKey.PAINTING_VARIANT).get(variantKey) == null) {
                        itemComponentsTag.remove(key);
                        changed = true;
                    } else {
                        for (String s : entityData.getAllKeys()) {
                            if (!s.equals("id") && !s.equals("variant")) {
                                entityData.remove(s);
                                changed = true;
                            }
                        }
                    }
                }
                return changed;
            }
        }
        if (!allow) {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }
}
