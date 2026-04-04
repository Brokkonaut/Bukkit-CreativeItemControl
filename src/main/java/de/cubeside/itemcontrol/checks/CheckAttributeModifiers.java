package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.Main;
import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import java.util.ArrayList;
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

    private boolean allowModifyDisplay;
    private int displayMaxCustomNameLength;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allowAll = ConfigUtil.getOrCreate(data, "allow_all", false);
        allowModifyDisplay = ConfigUtil.getOrCreate(data, "allow_modify_display", false);
        displayMaxCustomNameLength = ConfigUtil.getOrCreate(data, "display_max_custom_name_length", 1000);
        allowed.clear();
        boolean rewriteAllow = false;
        for (String s : ConfigUtil.getOrCreate(data, "allow", List.of())) {
            if (s.startsWith("generic.")) {
                s = s.substring(8);
                rewriteAllow = true;
            }
            NamespacedKey key = NamespacedKey.fromString(s);
            if (key == null || Registry.ATTRIBUTE.get(key) == null) {
                Main.getInstance().getLogger().warning("Invalid attribute modifier: " + s);
            } else {
                allowed.add(key);
            }
        }
        if (rewriteAllow) {
            ArrayList<String> allow = new ArrayList<>();
            for (String s : ConfigUtil.getOrCreate(data, "allow", List.of())) {
                if (s.startsWith("generic.")) {
                    s = s.substring(8);
                }
                allow.add(s);
                data.set("allow", allow);
                Main.getInstance().saveConfig();
            }
        }
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key, CheckData data) {
        boolean changed = false;
        ListTag modifiersList = itemComponentsTag.getList(key);
        if (modifiersList != null) {
            if (!allowAll) {
                changed |= filterModifiers(group, modifiersList);
            }
            if (modifiersList.isEmpty()) {
                itemComponentsTag.remove(key);
                changed = true;
            }
        } else {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }

    private boolean filterModifiers(GroupConfig group, ListTag modifiersList) {
        boolean changed = false;
        for (int i = modifiersList.size() - 1; i >= 0; i--) {
            CompoundTag tag = modifiersList.getCompound(i);
            if (tag == null) {
                modifiersList.remove(i);
                changed = true;
            } else {
                if (tag.containsKey("display")) {
                    if (!allowModifyDisplay) {
                        tag.remove("display");
                        changed = true;
                    } else if (displayMaxCustomNameLength <= 0) {
                        CompoundTag displayTag = tag.getCompound("display", false);
                        if (displayTag != null && (displayTag.containsKey("value") || "override".equals(displayTag.getString("type")))) {
                            tag.remove("display");
                            changed = true;
                        }
                    } else {
                        CompoundTag displayTag = tag.getCompound("display", false);
                        if (displayTag != null && displayTag.containsKey("value")) {
                            if (BaseCheckName.enforce(displayTag, "value", true, true, displayMaxCustomNameLength, group.getMaxComponentExpansions())) {
                                changed = true;
                            }
                        }
                    }
                }
                String s = tag.getString("type");
                if (s == null) {
                    modifiersList.remove(i);
                    changed = true;
                } else if (!allowAll) {
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
