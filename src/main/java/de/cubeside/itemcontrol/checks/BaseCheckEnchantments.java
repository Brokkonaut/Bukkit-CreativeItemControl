package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.Main;
import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

public abstract class BaseCheckEnchantments implements ComponentCheck {
    private static final Registry<Enchantment> ENCHANTMENT_REGISTY = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

    protected HashMap<NamespacedKey, Integer> maxLevels = new HashMap<>();
    protected boolean allowHidden;
    protected boolean allowOnAllItems;

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, getComponentKey().asMinimalString());
        allowOnAllItems = ConfigUtil.getOrCreate(data, "allow_on_all_items", false);
        allowHidden = ConfigUtil.getOrCreate(data, "allow_hidden", false);
        maxLevels.clear();
        ENCHANTMENT_REGISTY.forEach(e -> maxLevels.put(e.getKey(), e.getMaxLevel()));
        ConfigurationSection overrideMapLevelSection = ConfigUtil.getOrCreateSection(data, "override_max_level");
        for (String s : overrideMapLevelSection.getKeys(false)) {
            NamespacedKey key = NamespacedKey.fromString(s);
            if (key == null || ENCHANTMENT_REGISTY.get(key) == null) {
                Main.getInstance().getLogger().warning("Invalid enchantment: " + s);
            } else {
                int level = ConfigUtil.getOrCreate(overrideMapLevelSection, s, 0);
                maxLevels.put(key, level);
            }
        }
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        return enforceEnchantmentLevels(material, itemComponentsTag, key, false);
    }

    private boolean enforceEnchantmentLevels(Material material, CompoundTag itemComponentsTag, String key, boolean inLevelsTag) {
        boolean changed = false;
        CompoundTag compound = itemComponentsTag.getCompound(key);
        if (compound != null) {
            int minSize = 0;
            for (String e : new ArrayList<>(compound.getAllKeys())) {
                if (!inLevelsTag && e.equals("levels")) {
                    changed |= enforceEnchantmentLevels(material, compound, e, true);
                } else if (!inLevelsTag && e.equals("show_in_tooltip")) {
                    if (!allowHidden) {
                        compound.remove(e);
                        changed = true;
                    } else {
                        minSize++;
                    }
                } else {
                    NamespacedKey enchantmentKey = NamespacedKey.fromString(e);
                    Integer maxLevel = enchantmentKey == null ? null : maxLevels.get(enchantmentKey);
                    if (enchantmentKey == null || maxLevel == null) {
                        compound.remove(e);
                        changed = true;
                    } else {
                        // real enchantment
                        int level = compound.getInt(e);
                        if (level > maxLevel || level < 1 || isInvalidOnItem(enchantmentKey, material)) {
                            compound.remove(e);
                            changed = true;
                        }
                    }
                }
            }
            if (compound.size() <= minSize) {
                itemComponentsTag.remove(key);
                changed = true;
            }
        }
        return changed;
    }

    protected boolean isInvalidOnItem(NamespacedKey enchantmentKey, Material material) {
        return false;
    }
}
