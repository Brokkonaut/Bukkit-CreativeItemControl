package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckWrittenBookContent implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:written_book_content");

    private boolean allow;
    // private boolean allowAnyAuthor;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", false);
        // allowAnyAuthor = ConfigUtil.getOrCreate(data, "allow_any_author", false);
    }

    @Override
    public boolean enforce(Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        CompoundTag compound = itemComponentsTag.getCompound(key);
        if (!allow || compound == null) {
            itemComponentsTag.remove(key);
            changed = true;
        }
        // else if (!allowAnyAuthor) {
        // String author = compound.getString("author");
        // if()
        // }
        return changed;
    }
}
