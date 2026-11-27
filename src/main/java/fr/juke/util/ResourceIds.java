package fr.juke.util;

import net.minecraft.resources.ResourceKey;

/**
 * 26.x renamed {@code ResourceKey#location()} to {@code ResourceKey#identifier()}.
 * Keeping the difference here means the rest of the code is shared.
 */
public final class ResourceIds {
    private ResourceIds() {
    }

    public static String of(ResourceKey<?> key) {
        return key.identifier().toString();
    }
}
