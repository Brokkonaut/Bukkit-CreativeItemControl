package de.cubeside.itemcontrol.config;

import de.cubeside.itemcontrol.Main;
import de.cubeside.itemcontrol.checks.CheckAttributeModifiers;
import de.cubeside.itemcontrol.checks.CheckBannerPatterns;
import de.cubeside.itemcontrol.checks.CheckBaseColor;
import de.cubeside.itemcontrol.checks.CheckBees;
import de.cubeside.itemcontrol.checks.CheckBundleContents;
import de.cubeside.itemcontrol.checks.CheckCanBreak;
import de.cubeside.itemcontrol.checks.CheckCanPlaceOn;
import de.cubeside.itemcontrol.checks.CheckChargedProjectiles;
import de.cubeside.itemcontrol.checks.CheckConsumable;
import de.cubeside.itemcontrol.checks.CheckContainer;
import de.cubeside.itemcontrol.checks.CheckContainerLoot;
import de.cubeside.itemcontrol.checks.CheckCustomData;
import de.cubeside.itemcontrol.checks.CheckCustomModelData;
import de.cubeside.itemcontrol.checks.CheckCustomName;
import de.cubeside.itemcontrol.checks.CheckDamage;
import de.cubeside.itemcontrol.checks.CheckDamageResistant;
import de.cubeside.itemcontrol.checks.CheckDeathProtection;
import de.cubeside.itemcontrol.checks.CheckDebugStickState;
import de.cubeside.itemcontrol.checks.CheckDyedColor;
import de.cubeside.itemcontrol.checks.CheckEnchantable;
import de.cubeside.itemcontrol.checks.CheckEnchantmentGlintOverride;
import de.cubeside.itemcontrol.checks.CheckEnchantments;
import de.cubeside.itemcontrol.checks.CheckEntityData;
import de.cubeside.itemcontrol.checks.CheckEquippable;
import de.cubeside.itemcontrol.checks.CheckFireworkExplosion;
import de.cubeside.itemcontrol.checks.CheckFireworks;
import de.cubeside.itemcontrol.checks.CheckFood;
import de.cubeside.itemcontrol.checks.CheckGlider;
import de.cubeside.itemcontrol.checks.CheckInstrument;
import de.cubeside.itemcontrol.checks.CheckIntangibleProjectile;
import de.cubeside.itemcontrol.checks.CheckItemModel;
import de.cubeside.itemcontrol.checks.CheckItemName;
import de.cubeside.itemcontrol.checks.CheckJukeboxPlayable;
import de.cubeside.itemcontrol.checks.CheckLock;
import de.cubeside.itemcontrol.checks.CheckLodestoneTracker;
import de.cubeside.itemcontrol.checks.CheckLore;
import de.cubeside.itemcontrol.checks.CheckMapColor;
import de.cubeside.itemcontrol.checks.CheckMapDecorations;
import de.cubeside.itemcontrol.checks.CheckMapId;
import de.cubeside.itemcontrol.checks.CheckMaxDamage;
import de.cubeside.itemcontrol.checks.CheckMaxStackSize;
import de.cubeside.itemcontrol.checks.CheckNoteBlockSound;
import de.cubeside.itemcontrol.checks.CheckOminousBottleAmplifier;
import de.cubeside.itemcontrol.checks.CheckPotDecorations;
import de.cubeside.itemcontrol.checks.CheckPotionContents;
import de.cubeside.itemcontrol.checks.CheckProfile;
import de.cubeside.itemcontrol.checks.CheckRarity;
import de.cubeside.itemcontrol.checks.CheckRecipes;
import de.cubeside.itemcontrol.checks.CheckRepairCost;
import de.cubeside.itemcontrol.checks.CheckRepairable;
import de.cubeside.itemcontrol.checks.CheckStoredEnchantments;
import de.cubeside.itemcontrol.checks.CheckSuspiciousStewEffects;
import de.cubeside.itemcontrol.checks.CheckTooltipDisplay;
import de.cubeside.itemcontrol.checks.CheckUnbreakable;
import de.cubeside.itemcontrol.checks.CheckUseCooldown;
import de.cubeside.itemcontrol.checks.CheckUseRemainder;
import de.cubeside.itemcontrol.checks.CheckWritableBookContent;
import de.cubeside.itemcontrol.checks.CheckWrittenBookContent;
import de.cubeside.itemcontrol.checks.ComponentCheck;
import de.cubeside.itemcontrol.checks.GenericCheckVariant;
import de.cubeside.itemcontrol.checks.GenericSimpleCheck;
import de.cubeside.itemcontrol.util.ConfigUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;

public class GroupConfig {
    @SuppressWarnings("unchecked")
    private static Supplier<ComponentCheck>[] COMPONENT_CHECKS = new Supplier[] {
            GenericSimpleCheck.createFor("attack_range"),
            CheckAttributeModifiers::new,
            CheckBannerPatterns::new,
            CheckBaseColor::new,
            CheckBees::new,
            GenericSimpleCheck.createFor("block_entity_data"),
            GenericSimpleCheck.createFor("block_state", true),
            GenericSimpleCheck.createFor("blocks_attacks"),
            GenericSimpleCheck.createFor("break_sound"),
            GenericSimpleCheck.createFor("bucket_entity_data", true),
            CheckBundleContents::new,
            CheckCanBreak::new,
            CheckCanPlaceOn::new,
            CheckChargedProjectiles::new,
            CheckConsumable::new,
            CheckContainer::new,
            CheckContainerLoot::new,
            CheckCustomData::new,
            CheckCustomModelData::new,
            CheckCustomName::new,
            CheckDamage::new,
            CheckDamageResistant::new,
            GenericSimpleCheck.createFor("damage_type"),
            CheckDeathProtection::new,
            CheckDebugStickState::new,
            GenericSimpleCheck.createFor("dye"),
            CheckDyedColor::new,
            CheckEnchantable::new,
            CheckEnchantmentGlintOverride::new,
            CheckEnchantments::new,
            CheckEntityData::new,
            CheckEquippable::new,
            CheckFireworkExplosion::new,
            CheckFireworks::new,
            CheckFood::new,
            CheckGlider::new,
            CheckInstrument::new,
            CheckIntangibleProjectile::new,
            CheckItemModel::new,
            CheckItemName::new,
            CheckJukeboxPlayable::new,
            GenericSimpleCheck.createFor("kinetic_weapon"),
            CheckLock::new,
            CheckLodestoneTracker::new,
            CheckLore::new,
            CheckMapColor::new,
            CheckMapDecorations::new,
            CheckMapId::new,
            CheckMaxDamage::new,
            CheckMaxStackSize::new,
            GenericSimpleCheck.createFor("minimum_attack_charge"),
            CheckNoteBlockSound::new,
            CheckOminousBottleAmplifier::new,
            GenericSimpleCheck.createFor("piercing_weapon"),
            CheckPotDecorations::new,
            CheckPotionContents::new,
            GenericSimpleCheck.createFor("potion_duration_scale"),
            GenericSimpleCheck.createFor("provides_banner_patterns"),
            GenericSimpleCheck.createFor("provides_trim_material"),
            CheckProfile::new,
            CheckRarity::new,
            CheckRecipes::new,
            CheckRepairable::new,
            CheckRepairCost::new,
            CheckStoredEnchantments::new,
            CheckSuspiciousStewEffects::new,
            GenericSimpleCheck.createFor("swing_animation"),
            GenericSimpleCheck.createFor("tool"),
            CheckTooltipDisplay::new,
            GenericSimpleCheck.createFor("tooltip_style"),
            GenericSimpleCheck.createFor("trim", true),
            CheckUnbreakable::new,
            CheckUseCooldown::new,
            GenericSimpleCheck.createFor("use_effects"),
            CheckUseRemainder::new,
            GenericSimpleCheck.createFor("weapon"),
            CheckWritableBookContent::new,
            CheckWrittenBookContent::new,

            GenericCheckVariant.createFor("axolotl/variant"),
            GenericCheckVariant.createFor("cat/collar"),
            GenericCheckVariant.createFor("cat/variant"),
            GenericCheckVariant.createFor("chicken/variant"),
            GenericCheckVariant.createFor("cow/variant"),
            GenericCheckVariant.createFor("fox/variant"),
            GenericCheckVariant.createFor("frog/variant"),
            GenericCheckVariant.createFor("horse/variant"),
            GenericCheckVariant.createFor("llama/variant"),
            GenericCheckVariant.createFor("mooshroom/variant"),
            GenericCheckVariant.createFor("painting/variant"),
            GenericCheckVariant.createFor("parrot/variant"),
            GenericCheckVariant.createFor("pig/variant"),
            GenericCheckVariant.createFor("rabbit/variant"),
            GenericCheckVariant.createFor("salmon/size"),
            GenericCheckVariant.createFor("sheep/color"),
            GenericCheckVariant.createFor("shulker/color"),
            GenericCheckVariant.createFor("tropical_fish/base_color"),
            GenericCheckVariant.createFor("tropical_fish/pattern"),
            GenericCheckVariant.createFor("tropical_fish/pattern_color"),
            GenericCheckVariant.createFor("villager/variant"),
            GenericCheckVariant.createFor("wolf/collar"),
            GenericCheckVariant.createFor("wolf/sound_variant"),
            GenericCheckVariant.createFor("wolf/variant")
    };

    private String permission;
    private int priority;
    private int maxItemSizeBytes;
    private int maxComponentExpansions;
    private int maxItemExpansions;
    private Set<Material> forbiddenItems;
    private boolean allowAllComponents;
    private HashMap<NamespacedKey, ComponentCheck> checks;

    public GroupConfig(Main main, String name, ConfigurationSection section) {
        permission = "creativeitemcontrol.group." + name.toLowerCase();
        priority = name.equals("default") ? 0 : ConfigUtil.getOrCreate(section, "priority", 0);
        maxItemSizeBytes = ConfigUtil.getOrCreate(section, "max_item_size_bytes", -1);
        maxComponentExpansions = ConfigUtil.getOrCreate(section, "max_component_expansions", 32);
        maxItemExpansions = ConfigUtil.getOrCreate(section, "max_item_expansions", 28);
        forbiddenItems = new HashSet<>();
        for (String s : ConfigUtil.getOrCreate(section, "forbidden_items", List.of())) {
            NamespacedKey itemKey = NamespacedKey.fromString(s);
            if (itemKey == null) {
                main.getLogger().warning("Unknown item in " + name + ".forbidden_items: " + s);
            } else {
                Material m = Registry.MATERIAL.get(itemKey);
                if (m.isItem() && !m.isAir()) {
                    forbiddenItems.add(m);
                } else {
                    main.getLogger().warning("Unknown item in " + name + ".forbidden_items: " + s);
                }
            }
        }
        allowAllComponents = ConfigUtil.getOrCreate(section, "allow_all_components", false);

        checks = new LinkedHashMap<>();
        ConfigurationSection componentsSection = ConfigUtil.getOrCreateSection(section, "components");

        for (Supplier<ComponentCheck> constr : COMPONENT_CHECKS) {
            ComponentCheck check = constr.get();
            ComponentCheck old = checks.put(check.getComponentKey(), check);
            check.loadConfig(componentsSection);
            if (old != null) {
                throw new RuntimeException("Duplicate ComponentCheck: " + old.getComponentKey() + " - " + old.getClass().getName() + " and " + check.getClass().getName());
            }
        }
    }

    public String getPermission() {
        return permission;
    }

    public int getPriority() {
        return priority;
    }

    public int getMaxItemSizeBytes() {
        return maxItemSizeBytes;
    }

    public int getMaxComponentExpansions() {
        return maxComponentExpansions;
    }

    public Set<Material> getForbiddenItems() {
        return forbiddenItems;
    }

    public boolean isAllowAllComponents() {
        return allowAllComponents;
    }

    public ComponentCheck getComponentHandler(NamespacedKey id) {
        return checks.get(id);
    }

    public int getMaxItemExpansions() {
        return maxItemExpansions;
    }
}
