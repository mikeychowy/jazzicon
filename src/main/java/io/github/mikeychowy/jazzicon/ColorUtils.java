package io.github.mikeychowy.jazzicon;

import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/** Small utility class to handle anything regarding colors */
public final class ColorUtils {
    private static final Pattern HEX_COLOR_PATTERN =
            Pattern.compile("^#?([A-Fa-f0-9]{3}|[A-Fa-f0-9]{4}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$");

    private ColorUtils() {}

    /**
     * Check if a string is a valid hex color. Accepts 3, 4, 6, or 8 hex digits, with or without leading '#'.
     *
     * @param input the supposed hex color string
     * @return whether the input is a valid hex color
     */
    public static boolean isValidHexColor(@Nullable String input) {
        if (input == null) {
            return false;
        }
        return HEX_COLOR_PATTERN.matcher(input).matches();
    }
}
