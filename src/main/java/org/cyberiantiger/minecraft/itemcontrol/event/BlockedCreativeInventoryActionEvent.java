package org.cyberiantiger.minecraft.itemcontrol.event;

import de.cubeside.nmsutils.nbt.CompoundTag;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.cyberiantiger.minecraft.itemcontrol.config.Action;

public class BlockedCreativeInventoryActionEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Action action;
    private final CompoundTag itemTag;
    private final String itemTagString;

    public BlockedCreativeInventoryActionEvent(Player who, Action action, CompoundTag itemTag, String itemTagString) {
        super(who);
        this.action = action;
        this.itemTag = itemTag;
        this.itemTagString = itemTagString;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Action getAction() {
        return action;
    }

    public CompoundTag getItemTag() {
        return itemTag;
    }

    public String getItemTagString() {
        return itemTagString;
    }
}
