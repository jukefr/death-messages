package fr.juke.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

/**
 * Minecraft 1.21.1 uses {@code hasPermission(int)}; 1.21.11 and 26.x use
 * {@code permissions().hasPermission(...)}. Keeping the difference here means every
 * other file stays identical across branches.
 */
public final class PermissionUtil {
    private PermissionUtil() {
    }

    public static boolean isOperator(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }
}
