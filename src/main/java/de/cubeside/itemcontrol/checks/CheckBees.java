package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckBees implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:bees");

    private int maxBees;
    private boolean allowEntityData;
    private boolean allowAllEntities;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        maxBees = ConfigUtil.getOrCreate(data, "max_bees", 0);
        allowEntityData = ConfigUtil.getOrCreate(data, "allow_entity_data", false);
        allowAllEntities = ConfigUtil.getOrCreate(data, "allow_all_entities", false);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;

        ListTag beesList = itemComponentsTag.getList(key);
        if (beesList != null) {
            changed |= filterBees(beesList);
            if (beesList.isEmpty()) {
                itemComponentsTag.remove(key);
                changed = true;
            }
        } else {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }

    private boolean filterBees(ListTag beesList) {
        boolean changed = false;
        for (int i = beesList.size() - 1; i >= maxBees; i--) {
            beesList.remove(i);
            changed = true;
        }
        for (int i = beesList.size() - 1; i >= 0; i--) {
            CompoundTag beeCompound = beesList.getCompound(i);
            if (beeCompound != null) {
                CompoundTag entityData = beeCompound.getCompound("entity_data");
                if (entityData != null) {
                    if (!allowEntityData) {
                        for (String key : new ArrayList<>(entityData.getAllKeys())) {
                            if (!key.equals("id")) {
                                entityData.remove(key);
                                changed = true;
                            }
                        }
                    }
                    if (!allowAllEntities) {
                        String id = entityData.getString("id");
                        if (!"bee".equals(id) && !"minecraft:bee".equals(id)) {
                            beesList.remove(i);
                            changed = true;
                        }
                    }
                } else {
                    beesList.remove(i);
                    changed = true;
                }
            } else {
                beesList.remove(i);
                changed = true;
            }
        }
        return changed;
    }
}
