package de.cubeside.itemcontrol.checks;

import org.bukkit.NamespacedKey;

public class CheckItemName extends BaseCheckName {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:item_name");

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }
}
