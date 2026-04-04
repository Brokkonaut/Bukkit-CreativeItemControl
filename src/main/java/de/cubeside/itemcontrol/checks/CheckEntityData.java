package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.ItemChecker;
import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckEntityData implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:entity_data");

    private boolean allow;

    private boolean allowPaintings;

    private boolean allowItemFrames;

    private boolean allowItemsInItemFrames;

    private boolean allowArmorStands;

    private boolean allowItemsInArmorStands;

    private static final Set<String> ALLOWED_ITEM_FRAME_KEYS = new HashSet<>(Arrays.asList("id", "ItemDropChance", "ItemRotation", "Invisible", "Fixed", "Silent", "Invulnerable", "Glowing", "Tags"));

    private static final Set<String> ALLOWED_ARMOR_STAND_KEYS = new HashSet<>(Arrays.asList("id", "AbsorptionAmount", "Air", "ArmorItems", "active_effects", "attributes", "Brain", "CustomName", "CustomNameVisible", "data", "DeathTime", "DisabledSlots", "equipment", "fall_distance", "FallFlying",
            "Fire", "Glowing", "HasVisualFire", "HandItems", "Health", "HurtByTimestamp", "HurtTime", "Invisible", "Invulnerable", "Marker", "Motion", "NoBasePlate", "NoGravity", "OnGround", "Pose", "PortalCooldown", "Rotation", "ShowArms", "Silent", "Small", "Tags", "TicksFrozen"));

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", false);
        allowPaintings = ConfigUtil.getOrCreate(data, "allowPaintings", true);
        allowItemFrames = ConfigUtil.getOrCreate(data, "allowItemFrames", false);
        allowItemsInItemFrames = ConfigUtil.getOrCreate(data, "allowItemsInItemFrames", false);
        allowArmorStands = ConfigUtil.getOrCreate(data, "allowArmorStands", false);
        allowItemsInArmorStands = ConfigUtil.getOrCreate(data, "allowItemsInArmorStands", false);
    }

    @Override
    public boolean enforce(GroupConfig group, Material material, CompoundTag itemComponentsTag, String key, CheckData data) {
        boolean changed = false;
        CompoundTag entityData = itemComponentsTag.getCompound(key);
        if (material == Material.PAINTING && entityData != null) {
            if (allowPaintings) {
                String id = entityData.getString("id");
                if (id == null || !(id.equals("minecraft:painting") || id.equals("painting"))) {
                    itemComponentsTag.remove(key);
                    changed = true;
                } else {
                    String variant = entityData.getString("variant");
                    NamespacedKey variantKey = variant == null ? null : NamespacedKey.fromString(variant);
                    if (variantKey == null || RegistryAccess.registryAccess().getRegistry(RegistryKey.PAINTING_VARIANT).get(variantKey) == null) {
                        itemComponentsTag.remove(key);
                        changed = true;
                    } else {
                        for (String s : entityData.getAllKeys()) {
                            if (!s.equals("id") && !s.equals("variant")) {
                                entityData.remove(s);
                                changed = true;
                            }
                        }
                    }
                }
                return changed;
            }
        }
        if ((material == Material.ITEM_FRAME || material == Material.GLOW_ITEM_FRAME) && entityData != null) {
            if (allowItemFrames) {
                String id = entityData.getString("id");
                if (id == null || !(id.equals("minecraft:item_frame") || id.equals("item_frame") || id.equals("minecraft:glow_item_frame") || id.equals("glow_item_frame"))) {
                    itemComponentsTag.remove(key);
                    changed = true;
                } else {
                    for (String s : entityData.getAllKeys()) {
                        if (s.equals("Item")) {
                            if (!allowItemsInItemFrames) {
                                entityData.remove(s);
                                changed = true;
                            } else {
                                CompoundTag itemStack = entityData.getCompound(s);
                                if (itemStack != null) {
                                    Boolean result = ItemChecker.filterItem(itemStack, group, data);
                                    changed |= result != Boolean.FALSE;
                                    if (result == null) {
                                        entityData.remove(s);
                                    }
                                }
                            }
                        } else if (!ALLOWED_ITEM_FRAME_KEYS.contains(s)) {
                            entityData.remove(s);
                            changed = true;
                        }
                    }
                }
                return changed;
            }
        }
        if (material == Material.ARMOR_STAND && entityData != null) {
            if (allowArmorStands) {
                String id = entityData.getString("id");
                if (id == null || !(id.equals("minecraft:armor_stand") || id.equals("armor_stand"))) {
                    itemComponentsTag.remove(key);
                    changed = true;
                } else {
                    for (String s : entityData.getAllKeys()) {
                        if (s.equals("ArmorItems") || s.equals("HandItems") || s.equals("equipment")) {
                            if (!allowItemsInArmorStands) {
                                entityData.remove(s);
                                changed = true;
                            } else {
                                ListTag itemList = entityData.getList(s);
                                if (itemList != null) {
                                    for (int i = itemList.size() - 1; i >= 0; i--) {
                                        CompoundTag stack = itemList.getCompound(i);
                                        if (stack == null) {
                                            itemList.remove(i);
                                            changed = true;
                                        } else {
                                            Boolean result = ItemChecker.filterItem(stack, group, data);
                                            changed |= result != Boolean.FALSE;
                                            if (result == null) {
                                                itemList.remove(i);
                                            }
                                        }
                                    }
                                    if (itemList.isEmpty()) {
                                        entityData.remove(s);
                                        changed = true;
                                    }
                                } else {
                                    entityData.remove(s);
                                    changed = true;
                                }
                            }
                        } else if (!ALLOWED_ARMOR_STAND_KEYS.contains(s)) {
                            entityData.remove(s);
                            changed = true;
                        }
                    }
                }
                return changed;
            }

        }
        if (!allow) {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }
}
