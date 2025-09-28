package io.github.mikeychowy.jazzicon;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/** Small utility class to handle anything regarding names */
public final class NameUtils {
    private NameUtils() {}

    /**
     * Get initials from a name, the initial will be taken with spaces (' ') as the separator <br>
     * <br>
     * Examples:
     *
     * <pre>
     *     - John Smith -> JS
     *     - Melinda -> M
     *     - Daniel Radcliffe -> RF
     *     - Ricardo de Santa -> RDS
     *     - Miguel Jimenez-Matamoros -> MJ
     *     - {@code " "} -> ?
     *     - {@code ""} -> ?
     *     - {@code null} -> ?
     * </pre>
     *
     * <p>By the examples above, we can see that any names using dashes (-) or other such separator aren't taken into
     * account for the resulting initial. <br>
     * Also, any names with a word separator indicating 'of' such as 'de' in Latin based names will be taken as the
     * initial as well.
     *
     * @param name The full name you want to get the initials of
     * @return the initial of the name, or "?" if the supplied name is blank, empty or null
     */
    public static String getInitials(@Nullable String name) {
        String[] safeInputs = StringUtils.split(StringUtils.trimToEmpty(name), ' ');
        String initial = Arrays.stream(safeInputs)
                .filter(StringUtils::isNotBlank)
                .map(word -> word.substring(0, 1).toUpperCase())
                .limit(3)
                .collect(Collectors.joining());

        return StringUtils.defaultIfBlank(initial, "?");
    }
}
