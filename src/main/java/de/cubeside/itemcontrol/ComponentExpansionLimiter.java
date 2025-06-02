package de.cubeside.itemcontrol;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.TranslationRegistry;

public final class ComponentExpansionLimiter {
    private ComponentExpansionLimiter() {
    }

    private static final Pattern TRANSLATION_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    public static boolean checkExpansions(BaseComponent component, long maxExpansions) {
        try {
            checkExpansionsInternal(component, maxExpansions);
            return true;
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
        }
        return false;
    }

    private static long checkExpansionsInternal(BaseComponent component, long maxExpansions) throws IllegalArgumentException {
        long expansions = 0;
        if (component instanceof TranslatableComponent translatable) {
            expansions += getTranslationExpansions(translatable, maxExpansions);
        }
        HoverEvent hoverEvent = component.getHoverEvent();
        if (hoverEvent != null) {
            List<Content> contents = hoverEvent.getContents();
            if (contents != null) {
                for (Content content : contents) {
                    if (content instanceof Text text) {
                        if (text.getValue() instanceof BaseComponent contentComponent) {
                            expansions += checkExpansionsInternal(contentComponent, maxExpansions);
                        } else if (text.getValue() instanceof BaseComponent[] contentComponents) {
                            for (BaseComponent contentComponent : contentComponents) {
                                expansions += checkExpansionsInternal(contentComponent, maxExpansions);
                            }
                        }
                    }
                }
            }
        }

        List<BaseComponent> extra = component.getExtra();
        if (extra != null) {
            for (BaseComponent extraComponent : extra) {
                expansions += checkExpansionsInternal(extraComponent, maxExpansions);
            }
        }
        if (expansions > maxExpansions) {
            throw new IllegalArgumentException("Too many component expansions!");
        }
        return expansions;
    }

    private static long getTranslationExpansions(TranslatableComponent component, long maxExpansions) throws IllegalArgumentException {

        HashMap<BaseComponent, Integer> expansionCounts = new HashMap<>();

        String trans = TranslationRegistry.INSTANCE.translate(component.getTranslate());

        if (trans.equals(component.getTranslate()) && component.getFallback() != null) {
            trans = component.getFallback();
        }

        Matcher matcher = TRANSLATION_PATTERN.matcher(trans);
        int position = 0;
        int i = 0;
        while (matcher.find(position)) {
            position = matcher.end();

            String formatCode = matcher.group(2);
            switch (formatCode.charAt(0)) {
                case 's':
                case 'd':
                    String withIndex = matcher.group(1);

                    BaseComponent withComponent = component.getWith().get(withIndex != null ? Integer.parseInt(withIndex) - 1 : i++);
                    expansionCounts.put(withComponent, expansionCounts.computeIfAbsent(withComponent, c -> 0) + 1);
                    break;
            }
        }
        long expansions = 0;
        for (Entry<BaseComponent, Integer> e : expansionCounts.entrySet()) {
            expansions += (checkExpansionsInternal(e.getKey(), maxExpansions) + 1) * e.getValue();
        }
        return expansions;
    }
}
