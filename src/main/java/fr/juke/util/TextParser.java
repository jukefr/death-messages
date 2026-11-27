package fr.juke.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Map;

public final class TextParser {
    private TextParser() {
    }

    /**
     * Replaces {@code ${name}} placeholders. Unknown placeholders are left untouched.
     */
    public static String expand(String template, Map<String, String> placeholders) {
        String output = template == null ? "" : template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            output = output.replace("${" + entry.getKey() + "}", value);
        }
        return output;
    }

    /**
     * Parses both {@code &} and {@code §} legacy formatting codes into a vanilla component.
     */
    public static Component parse(String input) {
        String text = input == null ? "" : input;
        MutableComponent result = Component.empty();

        ChatFormatting color = null;
        boolean obfuscated = false;
        boolean bold = false;
        boolean strikethrough = false;
        boolean underline = false;
        boolean italic = false;
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < text.length(); ) {
            char current = text.charAt(i);
            if ((current == '§' || current == '&') && i + 1 < text.length()) {
                ChatFormatting formatting = ChatFormatting.getByCode(Character.toLowerCase(text.charAt(i + 1)));
                if (formatting != null) {
                    append(result, buffer, color, obfuscated, bold, strikethrough, underline, italic);
                    if (formatting == ChatFormatting.RESET) {
                        color = null;
                        obfuscated = false;
                        bold = false;
                        strikethrough = false;
                        underline = false;
                        italic = false;
                    } else if (formatting.ordinal() <= ChatFormatting.WHITE.ordinal()) {
                        color = formatting;
                    } else if (formatting == ChatFormatting.OBFUSCATED) {
                        obfuscated = true;
                    } else if (formatting == ChatFormatting.BOLD) {
                        bold = true;
                    } else if (formatting == ChatFormatting.STRIKETHROUGH) {
                        strikethrough = true;
                    } else if (formatting == ChatFormatting.UNDERLINE) {
                        underline = true;
                    } else if (formatting == ChatFormatting.ITALIC) {
                        italic = true;
                    }
                    i += 2;
                    continue;
                }
            }
            buffer.append(current);
            i++;
        }

        append(result, buffer, color, obfuscated, bold, strikethrough, underline, italic);
        return result;
    }

    private static void append(MutableComponent target, StringBuilder buffer, ChatFormatting color,
                               boolean obfuscated, boolean bold, boolean strikethrough,
                               boolean underline, boolean italic) {
        if (buffer.length() == 0) {
            return;
        }

        Style style = Style.EMPTY;
        if (color != null) {
            style = style.withColor(color);
        }
        if (obfuscated) {
            style = style.applyFormat(ChatFormatting.OBFUSCATED);
        }
        if (bold) {
            style = style.applyFormat(ChatFormatting.BOLD);
        }
        if (strikethrough) {
            style = style.applyFormat(ChatFormatting.STRIKETHROUGH);
        }
        if (underline) {
            style = style.applyFormat(ChatFormatting.UNDERLINE);
        }
        if (italic) {
            style = style.applyFormat(ChatFormatting.ITALIC);
        }

        target.append(Component.literal(buffer.toString()).withStyle(style));
        buffer.setLength(0);
    }
}
