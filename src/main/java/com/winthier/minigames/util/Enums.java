package com.winthier.minigames.util;

import com.winthier.minigames.MinigamesPlugin;

/**
 * Utility class helping parsing enum types with the simple yet
 * transformations and error checking.
 */
public class Enums {
    public static <T extends Enum<T>> T parseEnum(Class<T> enumType, String name) {
        return parseEnum(enumType, name, null);
    }

    public static <T extends Enum<T>> T parseEnum(Class<T> enumType, String name, T defaultValue) {
        T result;
        try {
            result = Enum.valueOf(enumType, name.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException iae) {
            MinigamesPlugin.getInstance().getLogger().warning("Invalid " + enumType.getName() + " enum name: " + name);
            for (int i = 0; i < 2; ++i) {
                StackTraceElement ste = iae.getStackTrace()[i];
                String msg = String.format("[%d] %s.%s() in %s line %d", i + 1, ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber());
                MinigamesPlugin.getInstance().getLogger().warning(msg);
            }
            result = defaultValue;
        }
        return result;
    } 
}
