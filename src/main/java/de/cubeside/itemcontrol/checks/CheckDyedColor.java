package de.cubeside.itemcontrol.checks;

import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckDyedColor implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:dyed_color");

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
    }

    @Override
    public boolean enforce(Material material, CompoundTag itemComponentsTag, String key) {
        return false;
    }
}
