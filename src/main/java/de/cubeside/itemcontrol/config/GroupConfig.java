package de.cubeside.itemcontrol.config;

import de.cubeside.itemcontrol.Main;
import de.cubeside.itemcontrol.checks.CheckAttributeModifiers;
import de.cubeside.itemcontrol.checks.CheckBannerPatterns;
import de.cubeside.itemcontrol.checks.CheckBaseColor;
import de.cubeside.itemcontrol.checks.CheckBees;
import de.cubeside.itemcontrol.checks.CheckBlockEntityData;
import de.cubeside.itemcontrol.checks.CheckBlockState;
import de.cubeside.itemcontrol.checks.CheckBucketEntityData;
import de.cubeside.itemcontrol.checks.CheckBundleContents;
import de.cubeside.itemcontrol.checks.CheckCanBreak;
import de.cubeside.itemcontrol.checks.CheckCanPlaceOn;
import de.cubeside.itemcontrol.checks.CheckChargedProjectiles;
import de.cubeside.itemcontrol.checks.CheckContainer;
import de.cubeside.itemcontrol.checks.CheckContainerLoot;
import de.cubeside.itemcontrol.checks.CheckCustomData;
import de.cubeside.itemcontrol.checks.CheckCustomModelData;
import de.cubeside.itemcontrol.checks.CheckCustomName;
import de.cubeside.itemcontrol.checks.CheckDamage;
import de.cubeside.itemcontrol.checks.CheckDebugStickState;
import de.cubeside.itemcontrol.checks.CheckDyedColor;
import de.cubeside.itemcontrol.checks.CheckEnchantmentGlintOverride;
import de.cubeside.itemcontrol.checks.CheckEnchantments;
import de.cubeside.itemcontrol.checks.CheckEntityData;
import de.cubeside.itemcontrol.checks.CheckFireResistant;
import de.cubeside.itemcontrol.checks.CheckFireworkExplosion;
import de.cubeside.itemcontrol.checks.CheckFireworks;
import de.cubeside.itemcontrol.checks.CheckFood;
import de.cubeside.itemcontrol.checks.CheckHideAdditionalTooltip;
import de.cubeside.itemcontrol.checks.CheckHideTooltip;
import de.cubeside.itemcontrol.checks.CheckInstrument;
import de.cubeside.itemcontrol.checks.CheckIntangibleProjectile;
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
import de.cubeside.itemcontrol.checks.CheckStoredEnchantments;
import de.cubeside.itemcontrol.checks.CheckSuspiciousStewEffects;
import de.cubeside.itemcontrol.checks.CheckTool;
import de.cubeside.itemcontrol.checks.CheckTrim;
import de.cubeside.itemcontrol.checks.CheckUnbreakable;
import de.cubeside.itemcontrol.checks.CheckWritableBookContent;
import de.cubeside.itemcontrol.checks.CheckWrittenBookContent;
import de.cubeside.itemcontrol.checks.ComponentCheck;
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
            CheckAttributeModifiers::new,
            CheckBannerPatterns::new,
            CheckBaseColor::new,
            CheckBees::new,
            CheckBlockEntityData::new,
            CheckBlockState::new,
            CheckBucketEntityData::new,
            CheckBundleContents::new,
            CheckCanBreak::new,
            CheckCanPlaceOn::new,
            CheckChargedProjectiles::new,
            CheckContainer::new,
            CheckContainerLoot::new,
            CheckCustomData::new,
            CheckCustomModelData::new,
            CheckCustomName::new,
            CheckDamage::new,
            CheckDebugStickState::new,
            CheckDyedColor::new,
            CheckEnchantmentGlintOverride::new,
            CheckEnchantments::new,
            CheckEntityData::new,
            CheckFireResistant::new,
            CheckFireworkExplosion::new,
            CheckFireworks::new,
            CheckFood::new,
            CheckHideAdditionalTooltip::new,
            CheckHideTooltip::new,
            CheckInstrument::new,
            CheckIntangibleProjectile::new,
            CheckItemName::new,
            CheckJukeboxPlayable::new,
            CheckLock::new,
            CheckLodestoneTracker::new,
            CheckLore::new,
            CheckMapColor::new,
            CheckMapDecorations::new,
            CheckMapId::new,
            CheckMaxDamage::new,
            CheckMaxStackSize::new,
            CheckNoteBlockSound::new,
            CheckOminousBottleAmplifier::new,
            CheckPotDecorations::new,
            CheckPotionContents::new,
            CheckProfile::new,
            CheckRarity::new,
            CheckRecipes::new,
            CheckRepairCost::new,
            CheckStoredEnchantments::new,
            CheckSuspiciousStewEffects::new,
            CheckTool::new,
            CheckTrim::new,
            CheckUnbreakable::new,
            CheckWritableBookContent::new,
            CheckWrittenBookContent::new,
    };

    private String permission;
    private int priority;
    private int maxItemSizeBytes;
    private Set<Material> forbiddenItems;
    private boolean allowAllComponents;
    private HashMap<NamespacedKey, ComponentCheck> checks;

    public GroupConfig(Main main, String name, ConfigurationSection section) {
        permission = "creativeitemcontrol.group." + name.toLowerCase();
        priority = name.equals("default") ? 0 : ConfigUtil.getOrCreate(section, "priority", 0);
        maxItemSizeBytes = ConfigUtil.getOrCreate(section, "max_item_size_bytes", -1);
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

    public Set<Material> getForbiddenItems() {
        return forbiddenItems;
    }

    public boolean isAllowAllComponents() {
        return allowAllComponents;
    }

    public ComponentCheck getComponentHandler(NamespacedKey id) {
        return checks.get(id);
    }
}
