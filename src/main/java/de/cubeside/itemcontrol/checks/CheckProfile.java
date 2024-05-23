package de.cubeside.itemcontrol.checks;

import com.google.gson.Gson;
import de.cubeside.itemcontrol.util.ConfigUtil;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class CheckProfile implements ComponentCheck {
    private static final NamespacedKey KEY = NamespacedKey.fromString("minecraft:profile");

    private boolean allow;

    @Override
    public NamespacedKey getComponentKey() {
        return KEY;
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        ConfigurationSection data = ConfigUtil.getOrCreateSection(section, KEY.asMinimalString());
        allow = ConfigUtil.getOrCreate(data, "allow", true);
    }

    @Override
    public boolean enforce(Material material, CompoundTag itemComponentsTag, String key) {
        boolean changed = false;
        CompoundTag compound = itemComponentsTag.getCompound(key);
        if (!allow) {
            itemComponentsTag.remove(key);
            changed = true;
        } else if (compound != null) {
            boolean nameOrId = false;
            String name = compound.getString("name");
            if (name != null && !isValidPlayerName(name)) {
                compound.remove("name");
                changed = true;
            } else if (name != null) {
                nameOrId = true;
            }
            int[] id = compound.getIntArray("id");
            if (id != null && id.length != 4) {
                compound.remove("id");
                changed = true;
            } else if (id != null) {
                nameOrId = true;
            }
            ListTag properties = compound.getList("properties");
            if (!nameOrId) {
                itemComponentsTag.remove(key);
                changed = true;
            } else if (properties != null) {
                for (int i = properties.size() - 1; i >= 0; i--) {
                    CompoundTag property = properties.getCompound(i);
                    if (property == null || !"textures".equals(property.getString("name"))) {
                        properties.remove(i);
                        changed = true;
                    } else {
                        String value = property.getString("value");
                        if (value == null) {
                            properties.remove(i);
                            changed = true;
                        } else {
                            boolean valid = false;
                            try {
                                String valueString = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                                LinkedHashMap<?, ?> obj = new Gson().fromJson(valueString, LinkedHashMap.class);
                                if (obj.get("textures") instanceof Map<?, ?> m) {
                                    if (m.get("SKIN") instanceof Map<?, ?> skin) {
                                        if (skin.get("url") instanceof String skinUrl) {
                                            URL url = new java.net.URI(skinUrl).toURL();
                                            if (url.toString().startsWith("http://textures.minecraft.net/texture/")) {
                                                valid = true;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // ignored
                            }
                            if (!valid) {
                                properties.remove(i);
                                changed = true;
                            }
                        }
                    }
                }
                if (properties.isEmpty()) {
                    compound.remove("properties");
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static boolean isValidPlayerName(String name) {
        return name.length() <= 16 && name.chars().filter(c -> c <= 32 || c >= 127).findAny().isEmpty();
    }
}
