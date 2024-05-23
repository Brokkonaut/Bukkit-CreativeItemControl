package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.Main;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;

public class CheckAttributeModifiers implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:attribute_modifiers");

    private HashSet<NamespacedKey> allowed = new HashSet<>();
    private boolean allowAll;
    private boolean allowHidden;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allowAll = ConfigUtil.getOrCreate(data, "allow_all", false);
        allowHidden = ConfigUtil.getOrCreate(data, "allow_hidden", false);
        allowed.clear();
        for (String s : ConfigUtil.getOrCreate(data, "allow", List.of())) {
            NamespacedKey key = NamespacedKey.fromString(s);
            if (key == null || Registry.ATTRIBUTE.get(key) == null) {
                Main.getInstance().getLogger().warning("Invalid attribute modifier: " + s);
            } else {
                allowed.add(key);
            }
        }
    }

    @Override
    public boolean enforce(Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        CompoundTag compound = itemComponentsTag.getCompound(key);
        if (compound != null) {
            if (!allowHidden) {
                if (compound.containsKey("show_in_tooltip")) {
                    compound.remove("show_in_tooltip");
                    changed = true;
                }
            }
            if (allowAll) {
                return changed;
            }
            ListTag modifiersList = compound.getList("modifiers");
            if (modifiersList != null) {
                changed |= filterModifiers(modifiersList);
                if (modifiersList.isEmpty()) {
                    itemComponentsTag.remove(key);
                    changed = true;
                }
            }
        } else {
            ListTag modifiersList = itemComponentsTag.getList(key);
            if (modifiersList != null) {
                changed |= filterModifiers(modifiersList);
                if (modifiersList.isEmpty()) {
                    itemComponentsTag.remove(key);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean filterModifiers(ListTag modifiersList) {
        boolean changed = false;
        for (int i = modifiersList.size() - 1; i >= 0; i--) {
            CompoundTag tag = modifiersList.getCompound(i);
            if (tag == null) {
                modifiersList.remove(i);
                changed = true;
            } else {
                String s = tag.getString("type");
                if (s == null) {
                    modifiersList.remove(i);
                    changed = true;
                } else {
                    NamespacedKey key = NamespacedKey.fromString(s);
                    if (key == null || !allowed.contains(key)) {
                        modifiersList.remove(i);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
}
