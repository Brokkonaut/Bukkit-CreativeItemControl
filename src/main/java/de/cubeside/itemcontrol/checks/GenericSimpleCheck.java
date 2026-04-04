package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class GenericSimpleCheck implements ComponentCheck {
    private final NamespacedKey key;
    protected final boolean defaultValue;

    protected boolean allow;

    protected GenericSimpleCheck(NamespacedKey key, boolean defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public static Supplier<? extends GenericSimpleCheck> createFor(String key) {
        return createFor(key, false);
    }

    public static Supplier<? extends GenericSimpleCheck> createFor(String key, boolean defaultValue) {
        return () -> new GenericSimpleCheck(NamespacedKey.fromString(key), defaultValue);
    }

    @Override
    public NamespacedKey getComponentKey() {
        return key;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, key.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", defaultValue);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key, CheckData data) {
        boolean changed = false;
        if (!allow) {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }
}
