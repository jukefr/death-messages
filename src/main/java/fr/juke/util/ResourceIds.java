package fr.juke.util;

import net.minecraft.resources.ResourceKey;

/**
 * Minecraft 1.21.1 uses {@code ResourceKey#location()}; 1.21.11 and 26.x use
 * {@code ResourceKey#identifier()}. Keeping the difference here means the rest of the
 * code is shared.
 */
public final class ResourceIds {
    private ResourceIds() {
    }

    public static String of(ResourceKey<?> key) {
        return key.identifier().toString();
    }
}
