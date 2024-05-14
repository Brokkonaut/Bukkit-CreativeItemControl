/*
 * Copyright 2015 Antony Riley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cyberiantiger.minecraft.itemcontrol;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import de.cubeside.nmsutils.NMSUtils;
import de.cubeside.nmsutils.nbt.CompoundTag;
import de.cubeside.nmsutils.nbt.ListTag;
import de.cubeside.nmsutils.nbt.TagType;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyberiantiger.minecraft.itemcontrol.config.Action;
import org.cyberiantiger.minecraft.itemcontrol.config.Blacklist;
import org.cyberiantiger.minecraft.itemcontrol.config.Config;
import org.cyberiantiger.minecraft.itemcontrol.event.BlockedCreativeInventoryActionEvent;
import org.cyberiantiger.minecraft.itemcontrol.items.ItemGroup;
import org.cyberiantiger.minecraft.itemcontrol.items.ItemGroups;
import org.cyberiantiger.minecraft.itemcontrol.items.ItemType;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 *
 * @author antony
 */
public class Main extends JavaPlugin implements Listener {
    private static final String PERMISSION_BYPASS = "creativeitemcontrol.bypass";
    private static final String PERMISSION_BLACKLIST_BYPASS = "creativeitemcontrol.blacklist.*";
    private static final String PERMISSION_MENU_PREFIX = "creativeitemcontrol.menu.";
    private static final String PERMISSION_BLACKLIST_PREFIX = "creativeitemcontrol.blacklist.";
    private static final ItemStack EMPTY_CURSOR = new ItemStack(Material.AIR);
    private static final String CONFIG = "config.yml";
    private static final String ITEMS = "items.yml";
    private static final Set<Material> BANNER_ITEMS = Collections.unmodifiableSet(new HashSet<>(List.of(
            Material.BLACK_BANNER,
            Material.BLUE_BANNER,
            Material.BROWN_BANNER,
            Material.CYAN_BANNER,
            Material.GRAY_BANNER,
            Material.GREEN_BANNER,
            Material.LIGHT_BLUE_BANNER,
            Material.LIGHT_GRAY_BANNER,
            Material.LIME_BANNER,
            Material.MAGENTA_BANNER,
            Material.ORANGE_BANNER,
            Material.PINK_BANNER,
            Material.PURPLE_BANNER,
            Material.RED_BANNER,
            Material.WHITE_BANNER,
            Material.YELLOW_BANNER)));

    private Map<UUID, PlayerState> playerStates = new HashMap<>();
    private NMSUtils tools;
    private Config config;
    private ItemGroups itemGroups;
    private static Main instance;

    public Main() {
        instance = this;
    }

    public static Main getInstance() {
        return instance;
    }

    private File getDataFile(String name) {
        return new File(getDataFolder(), name);
    }

    private Reader openFile(File file) throws IOException {
        return new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), Charsets.UTF_8);
    }

    private Reader openDataFile(String file) throws IOException {
        return openFile(getDataFile(file));
    }

    private Reader openResource(String resource) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            throw new FileNotFoundException(resource);
        }
        return new InputStreamReader(new BufferedInputStream(in), Charsets.UTF_8);
    }

    private void loadConfig() {
        config = new Config();
        try {
            Yaml configLoader = new Yaml(new CustomClassLoaderConstructor(Config.class, getClass().getClassLoader(), new LoaderOptions()));
            configLoader.setBeanAccess(BeanAccess.FIELD);
            config = configLoader.loadAs(openDataFile(CONFIG), Config.class);
            Permission parent = getServer().getPluginManager().getPermission(PERMISSION_BLACKLIST_BYPASS);
            if (parent == null) {
                parent = new Permission(PERMISSION_BLACKLIST_BYPASS);
                getServer().getPluginManager().addPermission(parent);
            }
            for (String s : config.getBlacklist().keySet()) {
                Permission child = getServer().getPluginManager().getPermission(PERMISSION_BLACKLIST_PREFIX + s);
                if (child == null) {
                    child = new Permission(PERMISSION_BLACKLIST_PREFIX + s);
                    getServer().getPluginManager().addPermission(child);
                    child.addParent(parent, true);
                }
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Error loading config.yml", ex);
            getLogger().severe("Your config.yml has fatal errors, using defaults.");
        } catch (YAMLException ex) {
            getLogger().log(Level.SEVERE, "Error loading config.yml", ex);
            getLogger().severe("Your config.yml has fatal errors, using defaults.");
        }
    }

    private void loadItems() {
        itemGroups = new ItemGroups();
        try {
            Yaml configLoader = new Yaml(new CustomClassLoaderConstructor(ItemGroups.class, getClass().getClassLoader(), new LoaderOptions()));
            configLoader.setBeanAccess(BeanAccess.FIELD);
            itemGroups = configLoader.loadAs(openResource(ITEMS), ItemGroups.class);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Error loading config.yml", ex);
            getLogger().severe("Your config.yml has fatal errors, using defaults.");
        } catch (YAMLException ex) {
            getLogger().log(Level.SEVERE, "Error loading config.yml", ex);
            getLogger().severe("Your config.yml has fatal errors, using defaults.");
        }

    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        tools = NMSUtils.createInstance(this);
        saveDefaultConfig();
        loadConfig();
        loadItems();
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        loadConfig();
        playerStates.clear();
        sender.sendMessage("CreativeItemControl " + getDescription().getVersion() + " reloaded.");
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
            if (!state.testClick()) {
                Action action = config.getOnRateLimit();
                performAction(config.getOnRateLimit(), player, null);
                if (action.isBlock()) {
                    e.setCancelled(true);
                    return;
                }
            }
            ItemStack expectedCursor = state.getLastItem();
            if (expectedCursor == null) {
                expectedCursor = EMPTY_CURSOR;
            }
            ItemStack cursor = e.getCursor();
            if (cursor == null || cursor.getType() == Material.AIR) {
                return;
            }
            CompoundTag clickedTag = tools.getMiscUtils().getItemStackNbt(cursor);
            // getLogger().info("Expected: " + tools.readItemStack(expectedCursor));
            // getLogger().info("Got: " + clickedTag);

            if (clickedTag != null) {
                if (!player.hasPermission(PERMISSION_BLACKLIST_BYPASS) && !checkBlacklist(player, clickedTag)) {
                    e.setCancelled(true);
                    return;
                }
                if (!cursor.isSimilar(expectedCursor) && !isInInventory(player.getInventory(), cursor) && !isAroundPlayer(player, cursor, clickedTag) && !checkMenuAccess(player, clickedTag)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
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
        if (cursor.getType() == Material.PLAYER_HEAD) {
            CompoundTag headData = cursorTag.getCompound("tag");
            if (headData != null) {
                CompoundTag skullOwnerData = headData.getCompound("SkullOwner");
                if (skullOwnerData != null && headData.size() == 1) {
                    Location playerLocation = player.getLocation();
                    int playerX = playerLocation.getBlockX();
                    int playerZ = playerLocation.getBlockZ();
                    int minChunkX = (playerX - 7) >> 4;
                    int maxChunkX = (playerX + 7) >> 4;
                    int minChunkZ = (playerZ - 7) >> 4;
                    int maxChunkZ = (playerZ + 7) >> 4;
                    Location blockLocation = playerLocation.clone();
                    for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                        for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                            Chunk chunk = playerLocation.getWorld().getChunkAt(chunkX, chunkZ);
                            for (BlockState bs : chunk.getTileEntities()) {
                                if (bs instanceof Skull) {
                                    bs.getLocation(blockLocation);
                                    if (blockLocation.distanceSquared(playerLocation) < 7 * 7) {
                                        CompoundTag skullTag = tools.getMiscUtils().getTileEntityNbt(bs.getBlock());
                                        if (skullTag != null) {
                                            CompoundTag existingSkullOwnerData = skullTag.getCompound("SkullOwner");
                                            if (existingSkullOwnerData != null && existingSkullOwnerData.equals(skullOwnerData)) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (BANNER_ITEMS.contains(cursor.getType())) {
            CompoundTag tagData = cursorTag.getCompound("tag");
            if (tagData != null) {
                CompoundTag tagBlockEntityTag = tagData.getCompound("BlockEntityTag");
                if (tagBlockEntityTag != null) {
                    ListTag patternsTag = tagBlockEntityTag.getList("Patterns");
                    Location playerLocation = player.getLocation();
                    int playerX = playerLocation.getBlockX();
                    int playerZ = playerLocation.getBlockZ();
                    int minChunkX = (playerX - 7) >> 4;
                    int maxChunkX = (playerX + 7) >> 4;
                    int minChunkZ = (playerZ - 7) >> 4;
                    int maxChunkZ = (playerZ + 7) >> 4;
                    Location blockLocation = playerLocation.clone();
                    for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                        for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                            Chunk chunk = playerLocation.getWorld().getChunkAt(chunkX, chunkZ);
                            for (BlockState bs : chunk.getTileEntities()) {
                                if (bs instanceof Banner) {
                                    bs.getLocation(blockLocation);
                                    if (blockLocation.distanceSquared(playerLocation) < 7 * 7) {
                                        CompoundTag bannerTag = tools.getMiscUtils().getTileEntityNbt(bs.getBlock());
                                        if (bannerTag != null) {
                                            ListTag existingPatternsTag = bannerTag.getList("Patterns");
                                            if (Objects.equal(existingPatternsTag, patternsTag)) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isSimilar(ItemStack item, ItemStack cursor) {
        return item != null && cursor != null && item.isSimilar(cursor);
    }

    private boolean isInInventory(PlayerInventory inventory, ItemStack cursor) {
        for (ItemStack contentStack : inventory.getContents()) {
            if (contentStack != null && contentStack.isSimilar(cursor)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission(PERMISSION_BLACKLIST_BYPASS)) {
            return;
        }
        PlayerState state = getPlayerState(p);
        if (!state.testClick()) {
            Action action = config.getOnRateLimit();
            performAction(config.getOnRateLimit(), p, null);
            if (action.isBlock()) {
                e.setCancelled(true);
                return;
            }
        }
        Item item = e.getItemDrop();
        CompoundTag itemTag = tools.getMiscUtils().getItemStackNbt(item.getItemStack());
        if (!checkBlacklist(p, itemTag)) {
            e.setCancelled(true);
            item.remove();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player player) {
            if (player.hasPermission(PERMISSION_BLACKLIST_BYPASS)) {
                return;
            }
            Item item = e.getItem();
            CompoundTag itemTag = tools.getMiscUtils().getItemStackNbt(item.getItemStack());
            if (!checkBlacklist(player, itemTag)) {
                e.setCancelled(true);
                item.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission(PERMISSION_BLACKLIST_BYPASS)) {
            return;
        }

        ItemStack item = e.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        CompoundTag itemTag = tools.getMiscUtils().getItemStackNbt(item);
        if (!checkBlacklist(p, itemTag)) {
            e.setCancelled(true);
        }
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

    private boolean checkMenuAccess(Player whoClicked, CompoundTag itemTag) {
        boolean found = false;
        boolean hasAccess = false;
        String itemId = itemTag.containsKey("id", TagType.STRING) ? itemTag.getString("id") : null;
        CompoundTag tag = itemTag.containsKey("tag", TagType.COMPOUND) ? itemTag.getCompound("tag") : null;
        if (itemId != null) {
            if (config.getWhitelist().contains(itemId)) {
                found = true;
                hasAccess = true;
            } else {
                if (tag == null) {
                    found = true;
                    hasAccess = true;
                } else {
                    for (Map.Entry<String, ItemGroup> e : itemGroups.getGroups().entrySet()) {
                        String name = e.getKey();
                        ItemGroup group = e.getValue();
                        ItemType type = group.getItems().get(itemId);
                        if (type != null) {
                            if (tag != null && tag.containsKey("Damage", TagType.INT)) {
                                tag.remove("Damage");
                                if (tag.size() == 0) {
                                    tag = null;
                                }
                            }
                            if (tag == null || type.getParsedTags().contains(tag)) {
                                found = true;
                                if (whoClicked.hasPermission(PERMISSION_MENU_PREFIX + name)) {
                                    hasAccess = true;
                                }
                            }
                        }
                    }
                    if (!hasAccess) {
                        if (config.getAllowedItems().contains(itemId) && tag == null) {
                            found = true;
                            hasAccess = true;
                        }
                    }
                }
            }
        }
        if (!found) {
            Action action = config.getUnavailable();
            if (action.isBlock()) {
                performAction(action, whoClicked, itemTag);
                return hasAccess;
            }
        }
        if (!hasAccess) {
            Action action = config.getNopermission();
            if (action.isBlock()) {
                performAction(action, whoClicked, itemTag);
                return hasAccess;
            }
        }
        return true;
    }

    private boolean checkBlacklist(Player whoClicked, CompoundTag itemTag) {
        Action blacklistAction = config.getBlacklisted();
        if (!blacklistAction.isBlock()) {
            return true;
        }
        String itemId = itemTag.getString("id");
        if (itemId == null) {
            return false;
        }
        for (Map.Entry<String, Blacklist> e : config.getBlacklist().entrySet()) {
            String name = e.getKey();
            Blacklist value = e.getValue();
            if (!whoClicked.hasPermission(PERMISSION_BLACKLIST_PREFIX + name) && value.getItems().contains(itemId)) {
                performAction(blacklistAction, whoClicked, itemTag);
                return false;
            }
        }
        return true;
    }

    private void performAction(Action action, Player player, CompoundTag itemTag) {
        String name = player.getName();
        String id = itemTag == null ? null : itemTag.getString("id");
        String fullItem = itemTag == null ? null : itemTag.toString();
        String item = fullItem;
        if (item != null && item.length() > 5000) {
            item = item.substring(0, 5000) + " + [" + (item.length() - 5000) + " bytes]";
        }
        if (action.getMessage() != null) {
            player.sendMessage(String.format(action.getMessage(), name, id, item));
        }
        if (action.getBroadcastMessage() != null) {
            getServer().broadcast(String.format(action.getBroadcastMessage(), name, id, item), action.getBroadcastPermission());
        }
        for (String s : action.getCommands()) {
            getServer().dispatchCommand(getServer().getConsoleSender(), String.format(s, name, id, item));
        }
        getServer().getPluginManager().callEvent(new BlockedCreativeInventoryActionEvent(player, action, itemTag, fullItem));
    }

    private class PlayerState {
        private ItemStack lastItem;
        private final long[] clickTimes;
        private int clickTimesOffset;

        public PlayerState() {
            if (config.getRateLimit() > 0) {
                clickTimesOffset = 0;
                clickTimes = new long[config.getRateLimit()];
                Arrays.fill(clickTimes, Long.MIN_VALUE);
            } else {
                clickTimes = null;
            }
        }

        public ItemStack getLastItem() {
            return lastItem;
        }

        public void setLastItem(ItemStack lastItem) {
            this.lastItem = lastItem;
        }

        public boolean testClick() {
            if (clickTimes != null) {
                long now = System.nanoTime();
                clickTimes[clickTimesOffset++] = now;
                if (clickTimesOffset >= clickTimes.length) {
                    clickTimesOffset = 0;
                }
                return clickTimes[clickTimesOffset] < now - TimeUnit.SECONDS.toNanos(config.getRateLimitTime());
            }
            return true;
        }
    }

    public NMSUtils getTools() {
        return tools;
    }
}
