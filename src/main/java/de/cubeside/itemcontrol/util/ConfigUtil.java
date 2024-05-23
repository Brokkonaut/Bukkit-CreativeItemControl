package de.cubeside.itemcontrol.util;

import de.cubeside.itemcontrol.Main;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigUtil {
    public static ConfigurationSection getOrCreateSection(ConfigurationSection parent, String name) {
        ConfigurationSection section = parent.getConfigurationSection(name);
        if (section == null) {
            section = parent.createSection(name);
            Main.getInstance().saveConfig();
        }
        return section;
    }

    public static boolean getOrCreate(ConfigurationSection parent, String name, boolean defaultValue) {
        if (!parent.contains(name)) {
            parent.set(name, defaultValue);
            Main.getInstance().saveConfig();
        }
        return parent.getBoolean(name);
    }

    public static int getOrCreate(ConfigurationSection parent, String name, int defaultValue) {
        if (!parent.contains(name)) {
            parent.set(name, defaultValue);
            Main.getInstance().saveConfig();
        }
        return parent.getInt(name);
    }

    public static List<String> getOrCreate(ConfigurationSection parent, String name, List<String> defaultValue) {
        if (!parent.contains(name)) {
            parent.set(name, new ArrayList<>(defaultValue));
            Main.getInstance().saveConfig();
        }
        return parent.getStringList(name);
    }
}
