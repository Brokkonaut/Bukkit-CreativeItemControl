package de.cubeside.itemcontrol.checks;

import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public interface ComponentCheck {
    public NamespacedKey getComponentKey();

    public void loadConfig(ConfigurationSection section);

    public boolean enforce(Material material, CompoundTag itemTag, String key);
}
