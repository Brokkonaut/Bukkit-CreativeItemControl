package de.cubeside.itemcontrol.checks;

import org.bukkit.NamespacedKey;

public class CheckCustomName extends BaseCheckName {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:custom_name");

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }
}
