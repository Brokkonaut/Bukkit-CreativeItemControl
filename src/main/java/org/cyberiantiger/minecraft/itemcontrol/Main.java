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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
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
import org.cyberiantiger.minecraft.nbt.CompoundTag;
import org.cyberiantiger.minecraft.unsafe.CBShim;
import org.cyberiantiger.minecraft.unsafe.NBTTools;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

/**
 *
 * @author antony
 */
public class Main extends JavaPlugin implements Listener {
    private static final String PERMISSION_BYPASS = "creativeitemcontrol.bypass";
    private static final String PERMISSION_BLACKLIST_BYPASS = "creativeitemcontrol.blacklist.*";
    private static final String PERMISSION_MENU_PREFIX = "creativeitemcontrol.menu.";
    private static final String PERMISSION_BLACKLIST_PREFIX = "creativeitemcontrol.blacklist.";
    private static final ItemStack EMPTY_CURSOR = new ItemStack(Material.AIR, 0);
    private static final String CONFIG = "config.yml";
    private static final String ITEMS = "items.yml";

    // Key is a player, can't be bothered to use generics to hide this type erasure.
    private Map<Object, PlayerState> playerStates = new WeakHashMap<Object, PlayerState>();
    private NBTTools tools;
    private Config config;
    private ItemGroups itemGroups;

    private boolean copyDefault(String source, String dest) {
        File destFile = new File(getDataFolder(), dest);
        if (!destFile.exists()) {
            try {
                destFile.getParentFile().mkdirs();
                InputStream in = getClass().getClassLoader().getResourceAsStream(source);
                if (in != null) {
                    try {
                        OutputStream out = new FileOutputStream(destFile);
                        try {
                            ByteStreams.copy(in, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                    return true;
                }
            } catch (IOException ex) {
                getLogger().log(Level.WARNING, "Error copying default " + dest, ex);
            }
        }
        return false;
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
            Yaml configLoader = new Yaml(new CustomClassLoaderConstructor(Config.class, getClass().getClassLoader()));
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
            Yaml configLoader = new Yaml(new CustomClassLoaderConstructor(ItemGroups.class, getClass().getClassLoader()));
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
        tools = CBShim.createShim(NBTTools.class, this);
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

    private PlayerState getPlayerState(Object player) {
        PlayerState result = playerStates.get(player);
        if (result == null) {
            playerStates.put(player, result = new PlayerState());
        }
        return result;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreativeEvent(InventoryCreativeEvent e) {
        HumanEntity whoClicked = e.getWhoClicked();
        if (whoClicked instanceof Player && whoClicked.getGameMode() == GameMode.CREATIVE) {
            if (whoClicked.hasPermission(PERMISSION_BYPASS)) {
                return;
            }
            PlayerState state = getPlayerState(whoClicked);
            if (!state.testClick()) {
                Action action = config.getOnRateLimit();
                performAction(config.getOnRateLimit(), (Player) whoClicked, null);
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
            CompoundTag clickedTag;
            boolean emptyCursor = cursor.isSimilar(EMPTY_CURSOR);
            if (emptyCursor) {
                clickedTag = tools.readItemStack(e.getCurrentItem());
            } else {
                clickedTag = tools.readItemStack(cursor);
            }
            if (clickedTag != null) {
                if (!whoClicked.hasPermission(PERMISSION_BLACKLIST_BYPASS) && !checkBlacklist(whoClicked, clickedTag)) {
                    e.setCancelled(true);
                    return;
                }
                if (!emptyCursor && !cursor.isSimilar(expectedCursor) && !isInInventory(whoClicked.getInventory(), cursor) && !isAroundPlayer(whoClicked, cursor) && !checkMenuAccess(whoClicked, clickedTag)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    private boolean isAroundPlayer(HumanEntity player, ItemStack cursor) {
        List<Entity> nearby = player.getNearbyEntities(6, 6, 6);
        for (Entity e : nearby) {
            if (e instanceof ItemFrame) {
                if (isSimilar(((ItemFrame) e).getItem(), cursor)) {
                    return true;
                }
            } else if (e instanceof ArmorStand) {
                ArmorStand as = (ArmorStand) e;
                if (isSimilar(as.getBoots(), cursor)) {
                    return true;
                }
                if (isSimilar(as.getLeggings(), cursor)) {
                    return true;
                }
                if (isSimilar(as.getChestplate(), cursor)) {
                    return true;
                }
                if (isSimilar(as.getHelmet(), cursor)) {
                    return true;
                }
                if (isSimilar(as.getItemInHand(), cursor)) {
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
        return item != null && cursor != null && item.isSimilar(cursor);
    }

    private boolean isInInventory(PlayerInventory inventory, ItemStack cursor) {
        return inventory.containsAtLeast(cursor, 1);
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
        CompoundTag itemTag = tools.readItemStack(item.getItemStack());
        if (!checkBlacklist(p, itemTag)) {
            e.setCancelled(true);
            item.remove();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        LivingEntity ent = e.getEntity();
        if (!(ent instanceof Player)) {
            return;
        }
        Player p = (Player) ent;
        if (p.hasPermission(PERMISSION_BLACKLIST_BYPASS)) {
            return;
        }
        Item item = e.getItem();
        CompoundTag itemTag = tools.readItemStack(item.getItemStack());
        if (!checkBlacklist(p, itemTag)) {
            e.setCancelled(true);
            item.remove();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorInventoryCreative(InventoryCreativeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        HumanEntity whoClicked = e.getWhoClicked();
        if (whoClicked instanceof Player && whoClicked.getGameMode() == GameMode.CREATIVE) {
            PlayerState state = getPlayerState(whoClicked);
            ItemStack stack = e.getCursor();
            if (EMPTY_CURSOR.isSimilar(stack)) {
                state.setLastItem(e.getCurrentItem());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorInventoryClose(InventoryCloseEvent e) {
        getPlayerState(e.getPlayer()).setLastItem(null);
    }

    private boolean checkMenuAccess(HumanEntity whoClicked, CompoundTag itemTag) {
        boolean found = false;
        boolean hasAccess = false;
        String itemId = itemTag.getString("id");
        Short damage = itemTag.getShort("Damage");
        CompoundTag tag = itemTag.getCompound("tag");
        if (itemId != null) {
            if (config.getWhitelist().contains(itemId)) {
                found = true;
                hasAccess = true;
            } else {
                for (Map.Entry<String, ItemGroup> e : itemGroups.getGroups().entrySet()) {
                    String name = e.getKey();
                    ItemGroup group = e.getValue();
                    ItemType type = group.getItems().get(itemId);
                    if (type != null) {
                        if (type.getDamage().contains(damage)) {
                            if (tag == null || type.getParsedTags().contains(tag)) {
                                found = true;
                                if (whoClicked.hasPermission(PERMISSION_MENU_PREFIX + name)) {
                                    hasAccess = true;
                                }
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
        if (!found) {
            Action action = config.getUnavailable();
            if (action.isBlock()) {
                performAction(action, (Player) whoClicked, itemTag);
                return hasAccess;
            }
        }
        if (!hasAccess) {
            Action action = config.getNopermission();
            if (action.isBlock()) {
                performAction(action, (Player) whoClicked, itemTag);
                return hasAccess;
            }
        }
        return true;
    }

    private boolean checkBlacklist(HumanEntity whoClicked, CompoundTag itemTag) {
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
                performAction(blacklistAction, (Player) whoClicked, itemTag);
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
}
