package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
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
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;

        String customNameJson = itemComponentsTag.getString(key);
        if (customNameJson != null && allow) {
            try {
                BaseComponent component = ComponentSerializer.deserialize(customNameJson);
                String plain = ChatColor.stripColor(component.toLegacyText());
                if (plain.length() > maxLength) {
                    itemComponentsTag.remove(key);
                    changed = true;
                } else if (!allowFormating) {
                    itemComponentsTag.setString(key, ComponentSerializer.toString(new TextComponent(plain)));
                    changed = true;
                }
            } catch (IllegalArgumentException e) {
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
