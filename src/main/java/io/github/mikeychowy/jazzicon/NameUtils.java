package io.github.mikeychowy.jazzicon;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public final class NameUtils {
    private NameUtils() {}

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
