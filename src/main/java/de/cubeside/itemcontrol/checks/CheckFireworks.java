package de.cubeside.itemcontrol.checks;

import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckFireworks implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:fireworks");

    private boolean allow;
    private int maxExposions;
    private int maxFlightTime;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", true);
        maxExposions = ConfigUtil.getOrCreate(data, "max_exposions", 6);
        maxFlightTime = ConfigUtil.getOrCreate(data, "max_flight_time", 3);
    }

    @Override
    public boolean enforce(Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        CompoundTag fireworksCompound = itemComponentsTag.getCompound(key);
        if (allow && fireworksCompound != null) {
            ListTag explosions = fireworksCompound.getList("explosions");
            if (explosions != null) {
                changed |= filterExplosions(explosions);
            }
            byte flightDuration = itemComponentsTag.getByte("flight_duration");
            if (flightDuration > maxFlightTime) {
                itemComponentsTag.remove("flight_duration");
                changed = true;
            }
        } else {
            itemComponentsTag.remove(key);
            changed = true;
        }
        return changed;
    }

    private boolean filterExplosions(ListTag list) {
        boolean changed = false;
        for (int i = list.size() - 1; i >= maxExposions; i--) {
            list.remove(i);
            changed = true;
        }
        return changed;
    }
}
