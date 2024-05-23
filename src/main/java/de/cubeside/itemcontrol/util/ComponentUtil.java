package de.cubeside.itemcontrol.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ComponentUtil {
    public static TextComponent color(String text, ChatColor color) {
        TextComponent component = new TextComponent(text);
        component.setColor(color);
        return component;
    }

    public static <T extends BaseComponent> T color(T component, ChatColor color) {
        component.setColor(color);
        return component;
    }

}
