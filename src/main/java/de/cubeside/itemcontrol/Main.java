package de.cubeside.itemcontrol;

import de.cubeside.itemcontrol.config.GroupConfig;
import de.cubeside.itemcontrol.config.PluginConfig;
import de.cubeside.itemcontrol.util.ComponentUtil;
import de.cubeside.nmsutils.NMSUtils;
import de.cubeside.nmsutils.nbt.CompoundTag;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    private static final String PERMISSION_BYPASS = "creativeitemcontrol.bypass";
    private static final ItemStack EMPTY_CURSOR = new ItemStack(Material.AIR);
    private File configFile;
    YamlConfiguration yamlConfig;

    private Map<UUID, PlayerState> playerStates = new HashMap<>();
    private NMSUtils tools;

    private PluginConfig pluginConfig;

    private static Main instance;

    public Main() {
        instance = this;
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        tools = NMSUtils.createInstance(this);
        configFile = new File(getDataFolder(), "config.yml");
        if (configFile.isFile() && getConfig().contains("allowedItems")) {
            File configBackupFile = new File(getDataFolder(), "config.backup.yml");
            if (configBackupFile.exists()) {
                configBackupFile.delete();
            }
            configFile.renameTo(configBackupFile);
        }
        saveDefaultConfig();
        reloadConfig();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        yamlConfig = new YamlConfiguration();
        try {
            yamlConfig.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "Could not load config file: " + e.getMessage());
        }
        pluginConfig = new PluginConfig(this, yamlConfig);
    }

    @Override
    public void saveConfig() {
        try {
            yamlConfig.save(configFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        reloadConfig();
        playerStates.clear();
        sender.sendMessage(ComponentUtil.color("CreativeItemControl reloaded.", ChatColor.GREEN));
        return true;
    }

    // Notes on InventoryCreativeEvent
    // Action is always PLACE_ALL
    // If cursor is EMPTY_CURSOR, item is picked up.
    // If cursor is not EMPTY_CURSOR, one or more items is dropped (spawned in).

    private PlayerState getPlayerState(Player player) {
        return playerStates.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerState());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreativeEvent(InventoryCreativeEvent e) {
        if (e.getWhoClicked() instanceof Player player && player.getGameMode() == GameMode.CREATIVE) {
            if (player.hasPermission(PERMISSION_BYPASS)) {
                return;
            }
            PlayerState state = getPlayerState(player);

            ItemStack expectedCursor = state.getLastItem();
            if (expectedCursor == null) {
                expectedCursor = EMPTY_CURSOR;
            }
            ItemStack cursor = e.getCursor();
            Material m = cursor == null ? Material.AIR : cursor.getType();
            if (m == Material.AIR) {
                return;
            }

            CompoundTag clickedTag = tools.getNbtUtils().getItemStackNbt(cursor);

            // always allowed
            if (cursor.isSimilar(expectedCursor) || isInInventory(player.getInventory(), cursor) || isAroundPlayer(player, cursor, clickedTag)) {
                return;
            }

            GroupConfig group = pluginConfig.getGroup(player);

            if (group.getForbiddenItems().contains(m)) {
                if (pluginConfig.getUnavailableMessage() != null) {
                    player.sendMessage(ComponentUtil.color(pluginConfig.getUnavailableMessage().replace("$itemtype$", m.getKey().asMinimalString()), ChatColor.DARK_RED));
                }
                e.setCancelled(true);
                return;
            }

            byte[] serialized = tools.getNbtUtils().writeBinary(clickedTag);
            if (group.getMaxItemSizeBytes() >= 0 && serialized.length > group.getMaxItemSizeBytes()) {
                if (pluginConfig.getTooLargeMessage() != null) {
                    player.sendMessage(ComponentUtil.color(pluginConfig.getTooLargeMessage().replace("$itemtype$", m.getKey().asMinimalString()), ChatColor.DARK_RED));
                }
                e.setCancelled(true);
                return;
            }

            getLogger().info("Got: " + clickedTag);
            if (clickedTag != null) {
                getLogger().info("String: " + tools.getNbtUtils().writeString(clickedTag));
                Boolean modified = ItemChecker.filterItem(clickedTag, group);
                if (modified == null) {
                    if (pluginConfig.getUnavailableMessage() != null) {
                        player.sendMessage(ComponentUtil.color(pluginConfig.getUnavailableMessage().replace("$itemtype$", m.getKey().asMinimalString()), ChatColor.DARK_RED));
                    }
                    e.setCancelled(true);
                    return;
                }

                getLogger().info("Result: " + tools.getNbtUtils().writeString(clickedTag));
                if (modified) {
                    ItemStack newStack = tools.getNbtUtils().createItemStack(clickedTag);
                    if (newStack == null) {
                        newStack = new ItemStack(Material.AIR);
                    }
                    e.setCursor(newStack);
                }
            }
        }
    }

    private boolean isInInventory(PlayerInventory inventory, ItemStack cursor) {
        for (ItemStack contentStack : inventory.getContents()) {
            if (isSimilar(contentStack, cursor)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAroundPlayer(Player player, ItemStack cursor, CompoundTag cursorTag) {
        List<Entity> nearby = player.getNearbyEntities(6, 6, 6);
        for (Entity e : nearby) {
            if (e instanceof ItemFrame) {
                if (isSimilar(((ItemFrame) e).getItem(), cursor)) {
                    return true;
                }
            } else if (e instanceof ArmorStand) {
                ArmorStand as = (ArmorStand) e;
                if (isSimilar(as.getEquipment().getBoots(), cursor)) {
                    return true;
                }
                if (isSimilar(as.getEquipment().getLeggings(), cursor)) {
                    return true;
                }
                if (isSimilar(as.getEquipment().getChestplate(), cursor)) {
                    return true;
                }
                if (isSimilar(as.getEquipment().getHelmet(), cursor)) {
                    return true;
                }
                if (isSimilar(as.getEquipment().getItemInMainHand(), cursor)) {
                    return true;
                }
                if (isSimilar(as.getEquipment().getItemInOffHand(), cursor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSimilar(ItemStack item, ItemStack cursor) {
        return item == null ? cursor == null : item.isSimilar(cursor);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorInventoryCreative(InventoryCreativeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getWhoClicked() instanceof Player player && player.getGameMode() == GameMode.CREATIVE) {
            PlayerState state = getPlayerState(player);
            state.setLastItem(e.getCurrentItem());
            // getLogger().info("Set Expected: " + tools.readItemStack(e.getCurrentItem()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerStates.remove(e.getPlayer().getUniqueId());
    }

    private class PlayerState {
        private ItemStack lastItem;

        public PlayerState() {
        }

        public ItemStack getLastItem() {
            return lastItem;
        }

        public void setLastItem(ItemStack lastItem) {
            this.lastItem = lastItem;
        }
    }

    public NMSUtils getTools() {
        return tools;
    }
}
