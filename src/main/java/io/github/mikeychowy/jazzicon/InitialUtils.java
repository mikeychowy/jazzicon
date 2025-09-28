package io.github.mikeychowy.jazzicon;

import com.machinezoo.noexception.Exceptions;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Small utility class to handle anything regarding names */
public final class InitialUtils {
    /** The icon with initials generation error message */
    private static final String ICON_WITH_INITIALS_GENERATION_ERROR_MESSAGE =
            "error while generating icon with initials";

    private InitialUtils() {}

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

    /**
     * Generate the JazzIcon to a {@link Writer} with initials of the supplied name on top of the colorful icon.
     *
     * @param generator the JazzIcon generator, must not be null
     * @param name the name for the icon with initials on top to be generated of
     * @param out the {@link Writer} to be appended into
     * @param initialClasses the classes to be inserted to the initial
     * @param initialStyles the styles to be inserted to the initial
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public static void generateIconWithInitialsToWriter(
            @NonNull JazzIcon generator,
            @NonNull String name,
            @NonNull Writer out,
            @NonNull List<String> initialClasses,
            @NonNull List<String> initialStyles) {
        Exceptions.wrap(e -> new JazzIconGenerationException(ICON_WITH_INITIALS_GENERATION_ERROR_MESSAGE, e))
                .run(() -> {
                    var initials = InitialUtils.getInitials(name);
                    generator.generateIconToWriter(
                            name,
                            out,
                            Exceptions.wrap(e -> new JazzIconGenerationException(
                                            "error while generating icon with initials", e))
                                    .consumer(w -> {
                                        w.append("<text ");
                                        if (!initialClasses.isEmpty()) {
                                            w.append("class=\"");
                                            w.append(String.join(" ", initialClasses));
                                            w.append("\" ");
                                        }
                                        if (!initialStyles.isEmpty()) {
                                            w.append("style=\"");
                                            w.append(String.join(" ", initialStyles));
                                            w.append("\" ");
                                        }
                                        w.append(
                                                "x=\"50%\" y=\"50%\" text-anchor=\"middle\" dominant-baseline=\"middle\" class=\"fill-white font-bold text-[30px] font-sans\">");
                                        w.append(initials);
                                        w.append("</text>");
                                    }));
                });
    }

    /**
     * Generate the JazzIcon to a {@link Writer} with initials of the supplied name on top of the colorful icon. This is
     * a convenience method when you have no classes or styles to be inserted to the initial's element
     *
     * @param generator the JazzIcon generator, must not be null
     * @param name the name for the icon with initials on top to be generated of
     * @param out the {@link Writer} to be appended into
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public static void generateIconWithInitialsToWriter(
            @NonNull JazzIcon generator, @NonNull String name, @NonNull Writer out) {
        generateIconWithInitialsToWriter(generator, name, out, List.of(), List.of());
    }

    /**
     * Generate the JazzIcon to an {@link OutputStream} with initials of the supplied name on top of the colorful icon.
     *
     * @param generator the JazzIcon generator, must not be null
     * @param name the name for the icon with initials on top to be generated of
     * @param outputStream the {@link OutputStream} to be appended into
     * @param initialClasses the classes to be inserted to the initial
     * @param initialStyles the styles to be inserted to the initial
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public static void generateIconWithInitialsToStream(
            @NonNull JazzIcon generator,
            @NonNull String name,
            @NonNull OutputStream outputStream,
            @NonNull List<String> initialClasses,
            @NonNull List<String> initialStyles) {
        Exceptions.wrap(e -> new JazzIconGenerationException(ICON_WITH_INITIALS_GENERATION_ERROR_MESSAGE, e))
                .run(() -> {
                    try (OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                        generateIconWithInitialsToWriter(generator, name, osw, initialClasses, initialStyles);
                        osw.flush();
                    }
                });
    }

    /**
     * Generate the JazzIcon to an {@link OutputStream} with initials of the supplied name on top of the colorful icon.
     * This is a convenience method when you have no classes or styles to be inserted to the initial's element
     *
     * @param generator the JazzIcon generator, must not be null
     * @param name the name for the icon with initials on top to be generated of
     * @param outputStream the {@link OutputStream} to be appended into
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public static void generateIconWithInitialsToStream(
            @NonNull JazzIcon generator, @NonNull String name, @NonNull OutputStream outputStream) {
        generateIconWithInitialsToStream(generator, name, outputStream, List.of(), List.of());
    }

    /**
     * Generate the JazzIcon with initials of the supplied name on top of the colorful icon.
     *
     * @param generator the JazzIcon generator, must not be null
     * @param name the name for the icon with initials on top to be generated of
     * @param initialClasses the classes to be inserted to the initial
     * @param initialStyles the styles to be inserted to the initial
     * @return the generated icon with initials of the name on top
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public static String generateIconWithInitials(
            @NonNull JazzIcon generator,
            @NonNull String name,
            @NonNull List<String> initialClasses,
            @NonNull List<String> initialStyles)
            throws JazzIconGenerationException {
        return Exceptions.wrap(e -> new JazzIconGenerationException(ICON_WITH_INITIALS_GENERATION_ERROR_MESSAGE, e))
                .get(() -> {
                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        generateIconWithInitialsToStream(generator, name, outputStream, initialClasses, initialStyles);
                        return outputStream.toString(StandardCharsets.UTF_8);
                    }
                });
    }

    /**
     * Generate the JazzIcon with initials of the supplied name on top of the colorful icon. <br>
     * This is a convenience method when you have no classes or styles to be inserted to the initial's element
     *
     * @param generator the JazzIcon generator, must not be null
     * @param name the name for the icon with initials on top to be generated of
     * @return the generated icon with initials of the name on top
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public static String generateIconWithInitials(@NonNull JazzIcon generator, @NonNull String name)
            throws JazzIconGenerationException {
        return generateIconWithInitials(generator, name, List.of(), List.of());
    }
}
