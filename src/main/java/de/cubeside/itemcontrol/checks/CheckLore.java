package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.ComponentExpansionLimiter;
import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckLore implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:lore");

    private int maxLines;
    private int maxLength;
    private boolean allow;
    private boolean allowFormating;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, getComponentKey().asMinimalString());
        maxLines = ConfigUtil.getOrCreate(data, "max_lines", 40);
        maxLength = ConfigUtil.getOrCreate(data, "max_length", 40);
        allow = ConfigUtil.getOrCreate(data, "allow", false);
        allowFormating = ConfigUtil.getOrCreate(data, "allow_formating", false);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;

        ListTag loreTag = itemComponentsTag.getList(key);
        if (allow && loreTag != null) {
            for (int i = loreTag.size() - 1; i >= 0; i--) {
                String customNameJson = loreTag.getString(i);
                if (customNameJson != null) {
                    try {
                        BaseComponent component = ComponentSerializer.deserialize(customNameJson);
                        if (!ComponentExpansionLimiter.checkExpansions(component, group.getMaxComponentExpansions())) {
                            loreTag.remove(i);
                            changed = true;
                        } else {
                            String plain = ChatColor.stripColor(component.toLegacyText());
                            if (plain.length() > maxLength) {
                                loreTag.remove(i);
                                changed = true;
                            } else if (!allowFormating) {
                                loreTag.setString(i, ComponentSerializer.toString(new TextComponent(plain)));
                                changed = true;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        loreTag.remove(i);
                        changed = true;
                    }
                } else {
                    loreTag.remove(i);
                    changed = true;
                }
            }
            while (loreTag.size() > maxLines && loreTag.size() > 0) {
                loreTag.remove(loreTag.getInt(loreTag.size() - 1));
                changed = true;
            }
            if (loreTag.size() == 0) {
                itemComponentsTag.remove(key);
                changed = true;
            }
        } else {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }
}
