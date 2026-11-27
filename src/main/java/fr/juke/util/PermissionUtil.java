package fr.juke.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

/**
 * Permission checks differ between 1.21.x ({@code hasPermission(int)}) and 26.x
 * ({@code permissions().hasPermission(...)}). Keeping the difference here means every
 * other file stays identical across branches.
 */
public final class PermissionUtil {
    private PermissionUtil() {
    }

    public static boolean isOperator(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }
}
