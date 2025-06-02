package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckWrittenBookContent implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:written_book_content");

    private boolean allow;
    // private boolean allowAnyAuthor;

    private boolean allowFormating;

    private int pageTextMaxLength;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", false);
        // allowAnyAuthor = ConfigUtil.getOrCreate(data, "allow_any_author", false);
        allowFormating = ConfigUtil.getOrCreate(data, "allow_formating", false);
        pageTextMaxLength = ConfigUtil.getOrCreate(data, "page_text_max_length", 10000);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        CompoundTag compound = itemComponentsTag.getCompound(key);
        if (!allow || compound == null) {
            itemComponentsTag.remove(key);
            changed = true;
        } else {
            ListTag pages = compound.getList("pages");
            if (pages != null) {
                int pageCount = pages.size();
                while (pageCount > 100) {
                    pages.remove(pageCount - 1);
                    changed = true;
                    pageCount--;
                }
                for (int i = 0; i < pageCount; i++) {
                    CompoundTag page = pages.getCompound(i);
                    if (page != null) {
                        if (BaseCheckName.enforce(page, "raw", true, allowFormating, pageTextMaxLength, group.getMaxComponentExpansions())) {
                            changed = true;
                        }
                        if (BaseCheckName.enforce(page, "filtered", true, allowFormating, pageTextMaxLength, group.getMaxComponentExpansions())) {
                            changed = true;
                        }
                    }
                }
            }
        }
        // else if (!allowAnyAuthor) {
        // String author = compound.getString("author");
        // if()
        // }
        return changed;
    }
}
