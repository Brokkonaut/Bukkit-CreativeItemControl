/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.itemcontrol.items;

import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import de.cubeside.nmsutils.nbt.TagType;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cyberiantiger.minecraft.itemcontrol.Main;

/**
 *
 * @author antony
 */
public class ItemType {
    private static final Set<Short> DEFAULT_DAMAGE = Collections.singleton(Short.valueOf((short) 0));
    private Set<Short> damage = DEFAULT_DAMAGE;
    private List<String> tags = Collections.emptyList();
    private transient Set<CompoundTag> parsedTags = null;

    public Set<Short> getDamage() {
        return damage;
    }

    public Set<CompoundTag> getParsedTags() {
        if (parsedTags == null) {
            if (tags.isEmpty()) {
                parsedTags = Collections.emptySet();
            } else {
                parsedTags = new HashSet<>(tags.size());
                for (String tag : tags) {
                    try {
                        CompoundTag compoundTag = Main.getInstance().getTools().getNbtUtils().parseString(tag);
                        parsedTags.add(compoundTag);

                        // accept books with lower enchantment levels too
                        if (compoundTag.containsKey("StoredEnchantments", TagType.LIST)) {
                            ListTag enchList = compoundTag.getList("StoredEnchantments");
                            if (enchList.size() == 1 && enchList.getElementType() == TagType.COMPOUND) {
                                CompoundTag ench = enchList.getCompound(0);
                                if (ench.containsKey("lvl", TagType.SHORT)) {
                                    int level = ench.getShort("lvl") - 1;
                                    while (level >= 1) {
                                        CompoundTag compoundTag2 = Main.getInstance().getTools().getNbtUtils().parseString(tag);
                                        ListTag enchList2 = compoundTag2.getList("StoredEnchantments");
                                        enchList2.getCompound(0).setShort("lvl", (short) level);
                                        parsedTags.add(compoundTag2);

                                        level -= 1;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return parsedTags;
    }
}
