package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckConsumable implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:consumable");

    private boolean allow;
    private boolean allowSounds;
    private boolean allowEffects;
    private boolean allowTeleport;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", false);
        allowSounds = ConfigUtil.getOrCreate(data, "allow_sounds", false);
        allowEffects = ConfigUtil.getOrCreate(data, "allow_effects", false);
        allowTeleport = ConfigUtil.getOrCreate(data, "allow_teleport", false);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;

        CompoundTag compound = itemComponentsTag.getCompound(key);
        if (allow && compound != null) {
            if (!allowSounds && compound.containsKey("sound")) {
                compound.remove("sound");
                changed = true;
            }
            ListTag consumeEffects = compound.getList("on_consume_effects");
            if (consumeEffects != null) {
                int count = consumeEffects.size();
                for (int i = count - 1; i >= 0; i--) {
                    CompoundTag consumeEffect = consumeEffects.getCompound(i);
                    String type = consumeEffect.getString("type");
                    if ("apply_effects".equals(type) || "remove_effects".equals(type) || "clear_all_effects".equals(type)) {
                        if (!allowEffects) {
                            consumeEffects.remove(i);
                            changed = true;
                        }
                    } else if ("teleport_randomly".equals(type)) {
                        if (!allowTeleport) {
                            consumeEffects.remove(i);
                            changed = true;
                        }
                    } else if ("play_sound".equals(type)) {
                        if (!allowSounds) {
                            consumeEffects.remove(i);
                            changed = true;
                        }
                    } else {
                        consumeEffects.remove(i);
                        changed = true;
                    }
                }
                if (changed && consumeEffects.isEmpty()) {
                    compound.remove("on_consume_effects");
                }
            }
        } else {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }
}
