package de.cubeside.itemcontrol;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;

public final class ComponentExpansionLimiter {
    private ComponentExpansionLimiter() {
    }

    private static final Pattern TRANSLATION_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    public static boolean checkExpansions(Component component, long maxExpansions) {
        if (maxExpansions < 0) {
            return true;
        }
        try {
            checkExpansionsInternal(component, maxExpansions);
            return true;
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
        }
        return false;
    }

    private static long checkExpansionsInternal(Component component, long maxExpansions) throws IllegalArgumentException {
        long expansions = 0;
        if (component instanceof TranslatableComponent translatable) {
            expansions += getTranslationExpansions(translatable, maxExpansions);
        }
        HoverEvent<?> hoverEvent = component.hoverEvent();
        if (hoverEvent != null) {
            if (hoverEvent.action() == Action.SHOW_TEXT) {
                Component text = ((Component) hoverEvent.value());
                if (text != null) {
                    expansions += checkExpansionsInternal(text, maxExpansions);
                }
            }
        }

        List<Component> extra = component.children();
        if (extra != null) {
            for (Component extraComponent : extra) {
                expansions += checkExpansionsInternal(extraComponent, maxExpansions);
            }
        }
        if (expansions > maxExpansions) {
            throw new IllegalArgumentException("Too many component expansions!");
        }
        return expansions;
    }

    private static long getTranslationExpansions(TranslatableComponent component, long maxExpansions) throws IllegalArgumentException {
        long expansionsFallback = 0;
        String fallbackString = component.fallback();
        if (fallbackString != null) {
            HashMap<TranslationArgument, Integer> expansionCounts = new HashMap<>();
            Matcher matcher = TRANSLATION_PATTERN.matcher(fallbackString);
            int position = 0;
            int i = 0;
            while (matcher.find(position)) {
                int start = matcher.start();
                position = matcher.end();

                String formatCode = matcher.group(2);
                String formatString = fallbackString.substring(start, position);
                if (!("%".equals(formatCode) && "%%".equals(formatString))) {
                    if (!"s".equals(formatCode)) {
                        throw new IllegalArgumentException("Unsupported format: '" + formatString + "'");
                    }
                    String withIndex = matcher.group(1);

                    TranslationArgument withComponent = component.arguments().get(withIndex != null ? Integer.parseInt(withIndex) - 1 : i++);
                    expansionCounts.merge(withComponent, 1, Integer::sum);
                }
            }
            for (Entry<TranslationArgument, Integer> e : expansionCounts.entrySet()) {
                expansionsFallback += (checkExpansionsInternal(e.getKey().asComponent(), maxExpansions) + 1) * e.getValue();
            }
        }
        // assume one use for every argument for builtin translations
        long expansions = 0;
        for (TranslationArgument e : component.arguments()) {
            expansions += (checkExpansionsInternal(e.asComponent(), maxExpansions) + 1);
        }
        // we do not know which one is used so expect the worst
        return Math.max(expansions, expansionsFallback);
    }
}
