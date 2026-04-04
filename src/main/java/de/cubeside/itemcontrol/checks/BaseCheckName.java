package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.ComponentExpansionLimiter;
import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public abstract class BaseCheckName implements ComponentCheck {
    private int maxLength;
    private boolean allow;
    private boolean allowFormating;

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, getComponentKey().asMinimalString());
        maxLength = ConfigUtil.getOrCreate(data, "max_length", 40);
        allow = ConfigUtil.getOrCreate(data, "allow", false);
        allowFormating = ConfigUtil.getOrCreate(data, "allow_formating", false);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key, CheckData data) {
        return enforce(itemComponentsTag, key, allow, allowFormating, maxLength, group.getMaxComponentExpansions());
    }

    public static boolean enforce(CompoundTag parentTag, String key, boolean allow, boolean allowFormating, int maxLength, int maxComponentExpansions) {
        if (!parentTag.containsKey(key)) {
            return false;
        }
        boolean changed = false;
        Component component = parentTag.getTextComponent(key);
        if (component != null && allow) {
            try {
                if (!ComponentExpansionLimiter.checkExpansions(component, maxComponentExpansions)) {
                    parentTag.remove(key);
                    changed = true;
                } else {
                    String plain = PlainTextComponentSerializer.plainText().serialize(component);
                    if (plain.length() > maxLength) {
                        parentTag.remove(key);
                        changed = true;
                    } else if (!allowFormating) {
                        parentTag.setTextComponent(key, Component.text(plain));
                        changed = true;
                    }
                }
            } catch (IllegalArgumentException e) {
                parentTag.remove(key);
                changed = true;
            }
        } else {
            parentTag.remove(key);
            changed = true;
        }
        return changed;
    }
}
