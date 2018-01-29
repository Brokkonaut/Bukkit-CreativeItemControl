/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.itemcontrol.items;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cyberiantiger.minecraft.nbt.CompoundTag;
import org.cyberiantiger.minecraft.nbt.ListTag;
import org.cyberiantiger.minecraft.nbt.MojangsonParser;
import org.cyberiantiger.minecraft.nbt.Tag;
import org.cyberiantiger.minecraft.nbt.TagTuple;
import org.cyberiantiger.minecraft.nbt.TagType;

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
                parsedTags = new HashSet<CompoundTag>(tags.size());
                for (String tag : tags) {
                    try {
                        MojangsonParser parser = new MojangsonParser(new StringReader(tag));
                        TagTuple<?> result = parser.parse();
                        CompoundTag compoundTag = (CompoundTag) result.getValue();
                        parsedTags.add(compoundTag);

                        // accept books with lower enchantment levels too
                        if (compoundTag.containsKey("StoredEnchantments", TagType.LIST)) {
                            ListTag enchList = compoundTag.getList("StoredEnchantments");
                            Tag<?>[] enchArray = enchList.getValue();
                            if (enchArray.length == 1) {
                                if (enchArray[0] instanceof CompoundTag) {
                                    CompoundTag ench = (CompoundTag) enchArray[0];
                                    if (ench.containsKey("lvl", TagType.SHORT)) {
                                        int level = ench.getShort("lvl") - 1;
                                        while (level >= 1) {
                                            parser = new MojangsonParser(new StringReader(tag));
                                            result = parser.parse();
                                            CompoundTag compoundTag2 = (CompoundTag) result.getValue();
                                            ListTag enchList2 = compoundTag2.getList("StoredEnchantments");
                                            Tag<?>[] enchArray2 = enchList2.getValue();
                                            CompoundTag ench2 = (CompoundTag) enchArray2[0];
                                            ench2.setShort("lvl", (short) level);
                                            parsedTags.add(compoundTag2);

                                            level -= 1;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return parsedTags;
    }
}
