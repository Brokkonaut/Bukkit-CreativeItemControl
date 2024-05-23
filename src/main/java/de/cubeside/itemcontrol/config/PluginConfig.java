package de.cubeside.itemcontrol.config;

import de.cubeside.itemcontrol.Main;
import de.cubeside.itemcontrol.util.ConfigUtil;
import java.util.ArrayList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PluginConfig {
    private GroupConfig defaultGroup;
    private ArrayList<GroupConfig> groups;
    private String unavailableMessage;
    private String tooLargeMessage;

    public PluginConfig(Main main, YamlConfiguration yamlConfig) {
        ConfigurationSection messagesSection = yamlConfig.getConfigurationSection("messages");
        if (messagesSection != null) {
            unavailableMessage = messagesSection.getString("unavailable");
            tooLargeMessage = messagesSection.getString("tooLarge");
        }
        ConfigurationSection groupsSection = ConfigUtil.getOrCreateSection(yamlConfig, "groups");
        ConfigurationSection defaultGroupSection = ConfigUtil.getOrCreateSection(groupsSection, "default");
        defaultGroup = new GroupConfig(main, "default", defaultGroupSection);
        groups = new ArrayList<>();
        for (String groupName : new ArrayList<>(groupsSection.getKeys(false))) {
            if (!groupName.equals("default")) {
                ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
                if (groupSection != null) {
                    groups.add(new GroupConfig(main, groupName, groupSection));
                }
            }
        }
        groups.sort((g1, g2) -> Integer.compare(g2.getPriority(), g1.getPriority()));
    }

    public GroupConfig getGroup(Player player) {
        for (GroupConfig group : groups) {
            if (player.hasPermission(group.getPermission())) {
                return group;
            }
        }
        return defaultGroup;
    }

    public String getUnavailableMessage() {
        return unavailableMessage;
    }

    public String getTooLargeMessage() {
        return tooLargeMessage;
    }
}
