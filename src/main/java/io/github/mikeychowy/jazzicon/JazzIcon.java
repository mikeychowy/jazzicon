package io.github.mikeychowy.jazzicon;

import com.github.ajalt.colormath.RenderCondition;
import com.github.ajalt.colormath.model.HSV;
import com.github.ajalt.colormath.model.RGB;
import com.machinezoo.noexception.Exceptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.hipparchus.random.RandomGenerator;
import org.hipparchus.random.Well512a;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JazzIcon generator, no more boring Jdenticons for Java, now we can bring funky new colors to the JVM. <br>
 * ALL REFERENCE (non-static) OPERATIONS ARE GUARANTEED TO BE THREAD-SAFE BY THE USE OF {@link ReentrantLock}, EVEN THE
 * GETTERS. <br>
 * <br>
 * SO IF YOU WANT TO WIRE THIS AS A SINGLETON IN YOUR USAGE,YOU'RE WELCOME TO
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class JazzIcon {
    /** Default count of shapes to be generated */
    public static final int DEFAULT_SHAPE_COUNT = 4;
    /** Default wobble for randomness purpose when shifting colors */
    public static final int DEFAULT_WOBBLE = 30;
    /** Default theme of the icon */
    public static final ColorPalettes DEFAULT_BASE_COLORS = ColorPalettes.JAZZ_ICON;
    /** Default allow list for padding in case supplied seed text's length is lesser than 3 */
    public static final String DEFAULT_ALLOWED_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    /** String format pattern to take only 3 points of a decimal: 2.5483 -> 2.548 */
    protected static final String THREE_POINTS_DECIMAL_FORMAT = "%.3f";
    /** String format pattern to take only 1 point of a decimal: 2.13 -> 2.1 */
    protected static final String ONE_POINT_DECIMAL_FORMAT = "%.1f";
    /** The icon generation error message */
    private static final String ICON_GENERATION_ERROR_MESSAGE = "error while generating icon";

    /** The logger */
    private static final Logger log = LoggerFactory.getLogger(JazzIcon.class);
    /** Lock to make sure operations are thread-safe */
    protected final ReentrantLock lock = new ReentrantLock(true);
    /** List of classes which will be inserted into "class" attribute of the generated svg */
    protected final List<String> svgClasses = new ArrayList<>();
    /** List of styles which will be inserted into "style" attribute of the generated svg */
    protected final List<String> svgStyles = new ArrayList<>();
    /** the count of the shapes to be generated, MUST be > 0 */
    protected int shapeCount;
    /** the wobble used for color rotating into hue shift, randomness purpose, MUST be > 0 */
    protected int wobble;
    /** the {@link ColorPalettes} to be used for the theme of the icon */
    protected ColorPalettes baseColors;
    /** allow list for characters to be randomly picked during seed text padding */
    protected String allowedCharactersForPaddingText;
    /** the {@link RandomGenerator} to be used to generate random values for JazzIcon calculation. */
    protected RandomGenerator randomGenerator;

    /** Default constructor using all default values. For the less adventurous of us. */
    public JazzIcon() {
        this(DEFAULT_SHAPE_COUNT, DEFAULT_WOBBLE, DEFAULT_BASE_COLORS, DEFAULT_ALLOWED_CHARACTERS, new Well512a());
    }

    /**
     * The real constructor of JazzIcon generator, control all the parameters yourself.
     *
     * @param shapeCount the count of the shapes to be generated, MUST be > 0
     * @param wobble the wobble used for color rotating into hue shift, randomness purpose, MUST be > 0
     * @param baseColors the {@link ColorPalettes} to be used for the theme of the icon
     * @param allowedCharactersForPaddingText allow list for characters to be randomly picked during seed text padding
     *     (when supplied seed text's length is lesser or equals 3)
     * @param randomGenerator the {@link RandomGenerator} to be used to generate random values for JazzIcon calculation.
     *     {@link Well512a} is the default
     */
    public JazzIcon(
            int shapeCount,
            int wobble,
            ColorPalettes baseColors,
            String allowedCharactersForPaddingText,
            RandomGenerator randomGenerator) {
        if (shapeCount <= 0) {
            throw new IllegalArgumentException("shapeCount must be > 0");
        }
        this.shapeCount = shapeCount;

        if (wobble <= 0) {
            throw new IllegalArgumentException("wobble must be > 0");
        }
        this.wobble = wobble;

        if (Objects.isNull(baseColors)) {
            throw new IllegalArgumentException("baseColors must not be null");
        }
        this.baseColors = baseColors;

        if (StringUtils.isBlank(allowedCharactersForPaddingText)) {
            throw new IllegalArgumentException("allowedCharacters must not be just blanks, an empty string or null");
        }
        this.allowedCharactersForPaddingText = allowedCharactersForPaddingText;

        if (shapeCount + 1 > baseColors.getColors().size()) {
            throw new IllegalArgumentException(
                    "Insufficient base colors, shapeCount list size must be higher than shapeCount + 1");
        }

        if (Objects.isNull(randomGenerator)) {
            throw new IllegalArgumentException("randomGenerator must not be null");
        }
        this.randomGenerator = randomGenerator;
    }

    /**
     * Rotates the supplied color using hueShift to specify its position on the color wheel.
     *
     * @param hexColor the original hex of the color to be rotated
     * @param hueShift the hueShift for the color on the color wheel, default logic for this is {@code (30 *
     *     randomlyGeneratedPosition) - (wobble / 2.0F)}
     * @return the rotated color hex
     */
    protected static String rotateColor(@NonNull String hexColor, double hueShift) {
        RGB rgb = RGB.Companion.invoke(hexColor);
        HSV hsv = rgb.toHSV();
        double newHue = ((hsv.getH() + hueShift) % 360.0);
        if (newHue < 0) {
            newHue += 360.0;
        }
        HSV rotated = new HSV(newHue, hsv.getS(), hsv.getV(), hsv.getAlpha());
        return rotated.toSRGB().toHex(true, RenderCondition.NEVER);
    }

    /**
     * Convenience method to pipe the generated svg as a base64 data url
     *
     * @param jazzIconSvg the generated JazzIcon svg
     * @return the JazzIcon as base64 data url
     */
    public static String generateDataUrl(@NonNull String jazzIconSvg) {
        String encoded = Base64.getEncoder().encodeToString(jazzIconSvg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + encoded;
    }

    /**
     * The convenience Builder helper for JazzIcon.
     *
     * @return the builder
     */
    public static JazzIconBuilder builder() {
        return new JazzIconBuilder();
    }

    /**
     * Generate the next transformation of the svg shape, changes the shape's position and rotation.
     *
     * @param index the index of the shape for which the transform will be generated, extra randomness
     * @param out the {@link Writer} to append the transform into
     * @throws IOException if anything goes wrong when appending the generated transform to the {@link Writer}.
     */
    protected void nextTransform(int index, @NonNull Writer out) throws IOException {
        try {
            lock.lock();
            double firstRotation = randomGenerator.nextDouble();
            double boost = randomGenerator.nextDouble();
            double secondRotation = randomGenerator.nextDouble();
            double angle = 2 * Math.PI * firstRotation;
            double velocity = (100 * (index + boost)) / shapeCount;
            double x = Math.cos(angle) * velocity;
            double y = Math.sin(angle) * velocity;
            double r = firstRotation * 360 + secondRotation * 180;

            out.append("translate(%s %s) rotate(%s 50 50)"
                    .formatted(
                            String.format(Locale.US, THREE_POINTS_DECIMAL_FORMAT, x),
                            String.format(Locale.US, THREE_POINTS_DECIMAL_FORMAT, y),
                            String.format(Locale.US, ONE_POINT_DECIMAL_FORMAT, r)));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Randomly pick the next color to be used from the list of baseColors rotated using hueShift.
     *
     * @param rotatedColors the list of colors that has been rotated according to the hueShift, MUST BE MUTABLE, WILL
     *     REMOVE THE PICKED COLOR FROM THE LIST, IF YOU HAVE AN IMMUTABLE LIST, COPY THE VALUES FROM IT USING
     *     {@code new ArrayList()}
     * @param out the {@link Writer} to append the picked color into
     * @throws IOException if anything goes wrong when appending the randomly picked color to the {@link Writer}.
     */
    protected void nextColor(@NonNull List<String> rotatedColors, @NonNull Writer out) throws IOException {
        try {
            lock.lock();
            // waste a cycle for extra randomness, spicy!
            randomGenerator.nextDouble();
            var position = randomGenerator.nextDouble();
            int index = (int) Math.floor((rotatedColors.size() - 1) * position);
            log.debug("nextColor: initial index={}, initial position={}", index, position);
            while (index >= rotatedColors.size() || index < 0) {
                log.debug("nextColor: index is way out of the list, regenerating");
                position = randomGenerator.nextDouble();
                index = (int) Math.floor((rotatedColors.size() - 1) * position);
            }
            log.debug("nextColor: final index={}, final position={}", index, position);

            var color = rotatedColors.remove(index);
            log.debug("nextColor: color from list = '{}'", color);
            if (StringUtils.isBlank(color) || !ColorUtils.isValidHexColor(color)) {
                log.debug(
                        "nextColor: color from list is invalid, either blank or not a valid hex color, outputting white");
                color = "#FFFFFF";
            }
            out.append(color);
        } finally {
            lock.unlock();
        }
    }

    /**
     * The svg classes to be appended to a generated JazzIcon.
     *
     * @return The svg classes
     */
    public List<String> getSvgClasses() {
        try {
            lock.lock();
            return svgClasses;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add the supplied svg class to internal list of svg classes.
     *
     * @param svgClass the svg class to be added to internal list of svg classes
     */
    public void addSvgClass(@NonNull String svgClass) {
        try {
            lock.lock();
            this.svgClasses.add(svgClass);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add the supplied svg classes to internal list of svg classes.
     *
     * @param svgClasses the svg classes to be added to internal list of svg classes
     */
    public void addSvgClasses(@NonNull String... svgClasses) {
        try {
            lock.lock();
            this.svgClasses.addAll(
                    Arrays.stream(svgClasses).filter(StringUtils::isNotBlank).toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add the supplied svg classes to internal list of svg classes.
     *
     * @param svgClasses the svg classes to be added to internal list of svg classes
     */
    public void addSvgClasses(@NonNull List<String> svgClasses) {
        try {
            lock.lock();
            this.svgClasses.addAll(
                    svgClasses.stream().filter(StringUtils::isNotBlank).toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the supplied svg class from internal list of svg classes. <br>
     * If no such class are found (either one of or all elements in the supplied parameters), this operation is
     * effectively a no-op.
     *
     * @param svgClass the svg class to be removed from internal list of svg classes
     */
    public void removeSvgClass(@NonNull String svgClass) {
        try {
            lock.lock();
            this.svgClasses.remove(svgClass);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the supplied svg classes from internal list of svg classes. <br>
     * If no such classes are found (either one of or all elements in the supplied parameters), this operation is
     * effectively a no-op.
     *
     * @param svgClasses the svg classes to be removed from internal list of svg classes
     */
    public void removeSvgClasses(@NonNull String... svgClasses) {
        try {
            lock.lock();
            this.svgClasses.removeAll(
                    Arrays.stream(svgClasses).filter(StringUtils::isNotBlank).toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the supplied svg classes from internal list of svg classes. <br>
     * If no such classes are found (either one of or all elements in the supplied list), this operation is effectively
     * a no-op.
     *
     * @param svgClasses the svg classes to be removed from internal list of svg classes
     */
    public void removeSvgClasses(@NonNull List<String> svgClasses) {
        try {
            lock.lock();
            this.svgClasses.removeAll(
                    svgClasses.stream().filter(StringUtils::isNotBlank).toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * The svg styles to be appended to a generated JazzIcon.
     *
     * @return The svg styles
     */
    public List<String> getSvgStyles() {
        try {
            lock.lock();
            return svgStyles;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add the supplied svg style to internal list of svg styles.
     *
     * @param svgStyle the svg style to be added to internal list of svg styles
     */
    public void addSvgStyle(@NonNull String svgStyle) {
        try {
            lock.lock();
            this.svgStyles.add(svgStyle);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add the supplied svg styles to internal list of svg styles.
     *
     * @param svgStyles the svg styles to be added to internal list of svg styles
     */
    public void addSvgStyles(@NonNull String... svgStyles) {
        try {
            lock.lock();
            this.svgStyles.addAll(
                    Arrays.stream(svgStyles).filter(StringUtils::isNotBlank).toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add the supplied svg styles to internal list of svg styles.
     *
     * @param svgStyles the svg styles to be added to internal list of svg styles
     */
    public void addSvgStyles(@NonNull List<String> svgStyles) {
        try {
            lock.lock();
            this.svgStyles.addAll(
                    svgStyles.stream().filter(StringUtils::isNotBlank).toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the supplied svg style from internal list of svg styles. <br>
     * If no such style is found, this operation is effectively a no-op.
     *
     * @param svgStyle the svg style to be removed from internal list of svg styles
     */
    public void removeSvgStyle(@NonNull String svgStyle) {
        try {
            lock.lock();
            this.svgStyles.remove(svgStyle);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the supplied svg styles from internal list of svg styles. <br>
     * If no such styles are found (either one of or all elements in the supplied parameters), this operation is
     * effectively a no-op.
     *
     * @param svgStyles the svg styles to be removed from internal list of svg styles
     */
    public void removeSvgStyles(@NonNull String... svgStyles) {
        try {
            lock.lock();
            this.svgStyles.removeAll(
                    Arrays.stream(svgStyles).filter(StringUtils::isNotBlank).toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the supplied svg styles from internal list of svg styles. <br>
     * If no such styles are found (either one of or all elements in the supplied list), this operation is effectively a
     * no-op.
     *
     * @param svgStyles the svg styles to be removed from internal list of svg styles
     */
    public void removeSvgStyles(@NonNull List<@NonNull String> svgStyles) {
        try {
            lock.lock();
            this.svgStyles.removeAll(
                    svgStyles.stream().filter(StringUtils::isNotBlank).toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Generate the actual shape after randomly picking which of the 3 shapes will be generated this round. <br>
     * <br>
     * For each of the shape type, further randomize each of its values
     *
     * @param index the current index of the shape count to be generated, for extra randomness in transforms
     * @param shapeType the randomly picked shape type, either of rectangle, circle, or polygon
     * @param mutableRotatedColors the list of colors that has been rotated according to the hueShift
     * @param out {@link Writer} to append shapes into.
     * @throws IOException if anything goes wrong when generating the shapes.
     */
    protected void createShape(
            int index, @NonNull ShapeType shapeType, @NonNull List<String> mutableRotatedColors, @NonNull Writer out)
            throws IOException {
        log.debug("creating shape number: {},  picked shape: {}", index, shapeType);
        if (ShapeType.CIRCLE.equals(shapeType)) {
            log.debug("creating shape circle, appending head");
            log.debug("creating shape circle, creating cx");
            out.append("<circle cx=\"");
            out.append(String.valueOf(randomGenerator.nextInt(101)));
            out.append("\" ");

            log.debug("creating shape circle, creating cy");
            out.append("cy=\"");
            out.append(String.valueOf(randomGenerator.nextInt(101)));
            out.append("\" ");

            log.debug("creating shape circle, creating r, picking integers inclusive of 20 ~ 55");
            out.append("r=\"");
            out.append(String.valueOf(randomGenerator.nextInt(34) + 20));
        } else if (ShapeType.POLYGON.equals(shapeType)) {
            log.debug("creating shape polygon, appending head");
            log.debug("creating shape polygon, creating points");
            out.append("<polygon points=\"");
            // first point
            log.debug("creating shape polygon, creating first point");
            out.append(String.valueOf(randomGenerator.nextInt(101)));
            out.append(",");
            // first coordinates
            log.debug("creating shape polygon, creating first coordinates");
            out.append(String.valueOf(randomGenerator.nextInt(101)));
            out.append(" ");
            out.append(String.valueOf(randomGenerator.nextInt(101)));
            out.append(",");
            // second coordinates
            log.debug("creating shape polygon, creating second coordinates");
            out.append(String.valueOf(randomGenerator.nextInt(101)));
            out.append(" ");
            out.append(String.valueOf(randomGenerator.nextInt(101)));
            out.append(",");
            // third coordinates
            log.debug("creating shape polygon, creating third coordinates");
            out.append(String.valueOf(randomGenerator.nextInt(101)));
            out.append(" ");
            out.append(String.valueOf(randomGenerator.nextInt(101)));
            out.append(",");
            // second point
            log.debug("creating shape polygon, creating second point");
            out.append(String.valueOf(randomGenerator.nextInt(101)));
        } else {
            log.debug("creating shape rect");
            out.append("<rect x=\"0\" y=\"0\" width=\"100%\" height=\"100%");
        }

        log.debug("generating transform");
        out.append("\" transform=\"");
        nextTransform(index, out);
        log.debug("generating random color");
        out.append("\" fill=\"");
        nextColor(mutableRotatedColors, out);
        out.append("\" />");
    }

    /**
     * Generate the shapes {@code shapeCount} times and write them into the supplied {@link Writer}. <br>
     * For now, only generate rectangles of randomized colors and positions.
     *
     * @param rotatedColors the list of colors that has been rotated according to the hueShift
     * @param out {@link Writer} to append shapes into.
     * @throws IOException if anything goes wrong when generating the shapes.
     */
    protected void createShapes(@NonNull List<String> rotatedColors, @NonNull Writer out) throws IOException {
        try {
            lock.lock();
            log.debug("Creating shapes for rotated colors: {}", rotatedColors);
            List<String> mutableRotatedColors = new ArrayList<>(rotatedColors);

            // first line
            log.debug("creating base shape");
            out.append("<rect x=\"0\" y=\"0\" width=\"100%\" height=\"100%\" fill=\"");
            log.debug("selecting random color for base shape");
            nextColor(mutableRotatedColors, out);
            out.append("\" />");

            log.debug("creating {} shapes", shapeCount);
            for (int i = 0; i < shapeCount; i++) {
                ShapeType shapeType = ShapeType.vals[randomGenerator.nextInt(ShapeType.vals.length)];
                createShape(i, shapeType, mutableRotatedColors, out);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get random string from allow list of characters for padding, length of the string is decided by the parameter
     *
     * @param length the length of the random string to be generated
     * @return the random string with the specified length, from the allow list for padding
     */
    @SuppressWarnings("SameParameterValue")
    protected String randomStringFromAllowedChars(int length) {
        try {
            lock.lock();
            StringBuilder sb = new StringBuilder(length);
            int n = allowedCharactersForPaddingText.length();
            for (int i = 0; i < length; i++) {
                int idx = randomGenerator.nextInt(n);
                sb.append(allowedCharactersForPaddingText.charAt(idx));
            }
            return sb.toString();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Generate the seed from your supplied text. <br>
     * <br>
     * Take the substring of the text from the 3rd character until the 11th (or until the end of the text if length
     * lesser than 11). Parse the resulting substring using {@link Long#parseUnsignedLong(String, int)} and return the
     * result. <br>
     * If any {@link NumberFormatException} occurs, return the text's hashCode instead.
     *
     * @param safeText the text to generate seed from, must be safe to use (not blank AND length > 3)
     * @return the seed from the text, either through calculation, or the hashCode
     */
    protected long tryBestGetSeedFromText(@NonNull String safeText) {
        try {
            lock.lock();
            try {
                return Long.parseUnsignedLong(safeText.substring(2, Math.min(10, safeText.length())), 16);
            } catch (NumberFormatException e) {
                log.debug("Could not parse text '{}' to Long, falling back to hashCode", safeText, e);
                // fallback to hash if not hex
                return safeText.hashCode();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Generate a JazzIcon to a {@link Writer}, with an optional body interceptor
     *
     * @param text the text to be the seed of the icon
     * @param out a {@link Writer} to write the icon into
     * @param svgBodyInterceptor optional body interceptor, in case you want to insert your own elements to the middle
     *     of the icon body, or anything else to do. Optional, you can pass null. <br>
     *     <br>
     *     <strong><u>DISCLAIMER: ANYTHING YOU DO IN THE CONSUMER TO THE SVG IS NOT GUARANTEED TO BE SAFE, I TAKE NO
     *     RESPONSIBILITY FOR YOUR OPERATION(S)</u></strong>
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public void generateIconToWriter(
            @NonNull String text, @NonNull Writer out, @Nullable Consumer<Writer> svgBodyInterceptor)
            throws JazzIconGenerationException {
        Exceptions.wrap(e -> new JazzIconGenerationException(
                        "An error has been encountered while trying to generate icon to writer", e))
                .run(() -> {
                    try {
                        // lock the generation, ensuring actual randomized seed and other values
                        // remain constant for the round
                        // this is almost the same effect as synchronized, with different semantics
                        // and quite some different implementation inside
                        lock.lock();
                        log.debug("original text: {}", text);
                        String safeText = StringUtils.trimToEmpty(text);
                        if (safeText.length() <= 3) {
                            log.debug(
                                    "original text is too short, padding 6 characters to left and right (respectively) from allow list: {}",
                                    allowedCharactersForPaddingText);
                            safeText = randomStringFromAllowedChars(6) + safeText + randomStringFromAllowedChars(6);
                        }
                        log.debug("safe to use text: {}", safeText);

                        long seed = tryBestGetSeedFromText(safeText);
                        randomGenerator.setSeed(seed);
                        log.debug("random generator seed: {}", seed);

                        double position = randomGenerator.nextDouble();
                        log.debug("random position: {}", position);
                        double hueShift = (30 * position) - (wobble / 2.0F);
                        log.debug("hue shift: {}", hueShift);

                        List<String> rotatedColors = baseColors.getColors().stream()
                                .map(base -> rotateColor(base, hueShift))
                                .toList();
                        log.debug("rotated colors: {}", rotatedColors);

                        // append head
                        log.debug("appending head");
                        out.append("<svg ");
                        if (!svgClasses.isEmpty()) {
                            log.debug("svg classes are not empty, appending: {}", svgClasses);
                            out.append("class=\"")
                                    .append(String.join(" ", svgClasses))
                                    .append("\" ");
                        }
                        if (!svgStyles.isEmpty()) {
                            log.debug("svg styles are not empty, appending: {}", svgStyles);
                            out.append("style=\"")
                                    .append(String.join(" ", svgStyles))
                                    .append("\" ");
                        }
                        out.append("xmlns=\"http://www.w3.org/2000/svg\" x=\"0\" y=\"0\" viewBox=\"0 0 100 100\">");

                        createShapes(rotatedColors, out);

                        // in case we need to add other shapes or whatever before appending tail
                        if (Objects.nonNull(svgBodyInterceptor)) {
                            log.debug("writer consumer is set, accepting");
                            log.debug(
                                    "DISCLAIMER: ANYTHING YOU DO IN THE CONSUMER TO THE SVG IS NOT GUARANTEED TO BE SAFE, I TAKE NO RESPONSIBILITY FOR YOUR OPERATION(S)");
                            svgBodyInterceptor.accept(out);
                        }

                        // append tail
                        log.debug("appending tail");
                        out.append("</svg>");
                    } finally {
                        lock.unlock();
                    }
                });
    }

    /**
     * Generate a JazzIcon to an {@link OutputStream}, with an optional body interceptor
     *
     * @param text the text to be the seed of the icon
     * @param outputStream the {@link OutputStream} to write the icon into
     * @param svgBodyInterceptor optional body interceptor, in case you want to insert your own elements to the middle
     *     of the icon body, or anything else to do. Optional, you can pass null. <br>
     *     <br>
     *     <strong><u>DISCLAIMER: ANYTHING YOU DO IN THE CONSUMER TO THE SVG IS NOT GUARANTEED TO BE SAFE, I TAKE NO
     *     RESPONSIBILITY FOR YOUR OPERATION(S)</u></strong>
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public void generateIconToStream(
            @NonNull String text, @NonNull OutputStream outputStream, @Nullable Consumer<Writer> svgBodyInterceptor)
            throws JazzIconGenerationException {
        try {
            lock.lock();
            Exceptions.wrap(e -> new JazzIconGenerationException(
                            "An error has been encountered while trying to generate icon to stream", e))
                    .run(() -> {
                        try (OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                            generateIconToWriter(text, osw, svgBodyInterceptor);
                            osw.flush();
                        }
                    });
        } finally {
            lock.unlock();
        }
    }

    /**
     * Generate a JazzIcon to an {@link OutputStream}
     *
     * @param text the text to be the seed of the icon
     * @param outputStream the {@link OutputStream} to write the icon into
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public void generateIconToStream(@NonNull String text, @NonNull OutputStream outputStream)
            throws JazzIconGenerationException {
        generateIconToStream(text, outputStream, null);
    }

    /**
     * Generate a JazzIcon directly to a String, with an optional body interceptor
     *
     * @param text the text to be the seed of the icon
     * @param svgBodyInterceptor optional body interceptor, in case you want to insert your own elements to the middle
     *     of the icon body, or anything else to do. Optional, you can pass null. <br>
     *     <br>
     *     <strong><u>DISCLAIMER: ANYTHING YOU DO IN THE CONSUMER TO THE SVG IS NOT GUARANTEED TO BE SAFE, I TAKE NO
     *     RESPONSIBILITY FOR YOUR OPERATION(S)</u></strong>
     * @return the SVG string of the JazzIcon
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public String generateIcon(@NonNull String text, @Nullable Consumer<Writer> svgBodyInterceptor)
            throws JazzIconGenerationException {
        try {
            lock.lock();
            return Exceptions.wrap(e -> new JazzIconGenerationException(ICON_GENERATION_ERROR_MESSAGE, e))
                    .get(() -> {
                        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                            generateIconToStream(text, outputStream, svgBodyInterceptor);
                            return outputStream.toString(StandardCharsets.UTF_8);
                        }
                    });
        } finally {
            lock.unlock();
        }
    }

    /**
     * Generate a JazzIcon directly to a String
     *
     * @param text the text to be the seed of the icon
     * @return the SVG string of the JazzIcon
     * @throws JazzIconGenerationException if anything goes wrong when generating the icon.
     */
    public String generateIcon(@NonNull String text) {
        return generateIcon(text, null);
    }

    /**
     * The shape count to be generated in the icon
     *
     * @return the shape count to be generated in the icon
     */
    public int getShapeCount() {
        try {
            lock.lock();
            return shapeCount;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the shape count to be generated in the icon. <br>
     * By default, {@code 4} is used so the image is unique and colorful enough
     *
     * @param shapeCount the new {@link ColorPalettes} shape count to be generated in the icon.
     * @return This class for fluent style API
     * @throws IllegalArgumentException if the supplied shapeCount param is lesser or equals 0 OR when the requested
     *     {@code shapeCount + 1} is lower than the {@code baseColor}'s set size
     */
    public JazzIcon setShapeCount(int shapeCount) throws IllegalArgumentException {
        try {
            lock.lock();
            if (shapeCount <= 0) {
                throw new IllegalArgumentException("shapeCount must be > 0");
            }
            if (shapeCount + 1 > baseColors.getColors().size()) {
                throw new IllegalArgumentException(
                        "Insufficient base colors, shapeCount list size must be higher than shapeCount + 1");
            }
            this.shapeCount = shapeCount;
            return this;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The wobbles to the hue shift of the color rotator
     *
     * @return the wobbles to the hue shift of the color rotator
     */
    public int getWobble() {
        try {
            lock.lock();
            return wobble;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the wobbles to the hue shift of the color rotator. <br>
     * By default, {@code 30} is used
     *
     * @param wobble the new {@link ColorPalettes} to be used by JazzIcon
     * @return This class for fluent style API
     * @throws IllegalArgumentException if the supplied wobble param is lesser or equals 0
     */
    public JazzIcon setWobble(int wobble) throws IllegalArgumentException {
        try {
            lock.lock();
            if (wobble <= 0) {
                throw new IllegalArgumentException("wobble must be > 0");
            }
            this.wobble = wobble;
            return this;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The {@link ColorPalettes} to be used by JazzIcon.
     *
     * @return the {@link ColorPalettes} to be used by JazzIcon.
     */
    @NonNull public ColorPalettes getBaseColors() {
        try {
            lock.lock();
            return baseColors;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the {@link ColorPalettes} to be used by JazzIcon. <br>
     * By default, {@link ColorPalettes#JAZZ_ICON} is used
     *
     * @param baseColors the new {@link ColorPalettes} to be used by JazzIcon
     * @return This class for fluent style API
     * @throws IllegalArgumentException if the supplied baseColors param is null OR when the requested {@code shapeCount
     *     + 1} is lower than the {@code baseColor}'s set size
     */
    public JazzIcon setBaseColors(ColorPalettes baseColors) throws IllegalArgumentException {
        try {
            lock.lock();
            if (Objects.isNull(baseColors)) {
                throw new IllegalArgumentException("baseColors must not be null");
            }
            if (shapeCount + 1 > baseColors.getColors().size()) {
                throw new IllegalArgumentException(
                        "Insufficient base colors, shapeCount list size must be higher than shapeCount + 1");
            }
            this.baseColors = baseColors;
            return this;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The characters allow list for padding seed text when seed text lesser or equals 3
     *
     * @return the characters allow list for padding seed text when seed text lesser or equals 3
     */
    @NonNull public String getAllowedCharactersForPaddingText() {
        try {
            lock.lock();
            return allowedCharactersForPaddingText;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the characters allow list to be used for padding seed text if seed text's length lesser or equals 3. <br>
     * By default, {@code "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"} is used
     *
     * @param allowedCharactersForPaddingText the new characters allow list for padding seed text
     * @return This class for fluent style API
     * @throws IllegalArgumentException if the supplied param is blank (spaces only/empty/null)
     */
    public JazzIcon setAllowedCharactersForPaddingText(String allowedCharactersForPaddingText)
            throws IllegalArgumentException {
        try {
            lock.lock();
            if (StringUtils.isBlank(allowedCharactersForPaddingText)) {
                throw new IllegalArgumentException(
                        "allowedCharacters must not be just blanks, an empty string or null");
            }
            this.allowedCharactersForPaddingText = allowedCharactersForPaddingText;
            return this;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The {@link RandomGenerator} used by JazzIcon
     *
     * @return the {@link RandomGenerator} used by JazzIcon
     */
    @NonNull public RandomGenerator getRandomGenerator() {
        try {
            lock.lock();
            return randomGenerator;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the {@link RandomGenerator} to be used by JazzIcon. <br>
     * By default, {@link Well512a} is used
     *
     * @param randomGenerator the new {@link RandomGenerator} to be used by JazzIcon
     * @return This class for fluent style API
     * @throws IllegalArgumentException if the supplied randomGenerator param is null
     */
    public JazzIcon setRandomGenerator(RandomGenerator randomGenerator) throws IllegalArgumentException {
        try {
            lock.lock();
            if (Objects.isNull(randomGenerator)) {
                throw new IllegalArgumentException("randomGenerator must not be null");
            }
            this.randomGenerator = randomGenerator;
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JazzIcon jazzIcon = (JazzIcon) o;
        try {
            lock.lock();
            return shapeCount == jazzIcon.shapeCount
                    && wobble == jazzIcon.wobble
                    && baseColors.equals(jazzIcon.baseColors)
                    && randomGenerator.getClass().equals(jazzIcon.randomGenerator.getClass());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int hashCode() {
        try {
            lock.lock();
            int result = shapeCount;
            result = 31 * result + wobble;
            result = 31 * result + baseColors.hashCode();
            result = 31 * result + randomGenerator.getClass().hashCode();
            return result;
        } finally {
            lock.unlock();
        }
    }

    protected enum ShapeType {
        RECTANGLE,
        CIRCLE,
        POLYGON,
        ;
        protected static final ShapeType[] vals = values();
    }

    /** Convenience Builder Style helper for JazzIcon class creation */
    @SuppressWarnings("ClassCanBeRecord")
    public static class JazzIconBuilder {
        /** The immutable JazzIcon reference for the builder */
        private final JazzIcon jazzIcon;

        /**
         * Use your own JazzIcon reference
         *
         * @param jazzIcon the JazzIcon reference
         */
        public JazzIconBuilder(JazzIcon jazzIcon) {
            this.jazzIcon = jazzIcon;
        }

        /** Use a default JazzIcon reference */
        public JazzIconBuilder() {
            this(new JazzIcon());
        }

        /**
         * Change only the shape count
         *
         * @param shapeCount the count of the shapes to be generated, MUST be > 0
         * @return the builder
         */
        public JazzIconBuilder withShapeCount(int shapeCount) {
            jazzIcon.setShapeCount(shapeCount);
            return this;
        }

        /**
         * Change the wobble used for color rotating into hue shift
         *
         * @param wobble the wobble used for color rotating into hue shift, randomness purpose, MUST be > 0
         * @return the builder
         */
        public JazzIconBuilder withWobble(int wobble) {
            jazzIcon.setWobble(wobble);
            return this;
        }

        /**
         * Change the {@link ColorPalettes} to be used for the theme of the icon
         *
         * @param baseColors the {@link ColorPalettes} to be used for the theme of the icon
         * @return the builder
         */
        public JazzIconBuilder withBaseColors(ColorPalettes baseColors) {
            jazzIcon.setBaseColors(baseColors);
            return this;
        }

        /**
         * Change the allowedCharactersForPaddingText allow list for characters to be randomly picked during seed text
         * padding
         *
         * @param allowedCharacters allowedCharactersForPaddingText allow list for characters to be randomly picked
         *     during seed text padding (when supplied seed text's length is lesser or equals 3)
         * @return the builder
         */
        public JazzIconBuilder withAllowedCharactersForPaddingText(String allowedCharacters) {
            jazzIcon.setAllowedCharactersForPaddingText(allowedCharacters);
            return this;
        }

        /**
         * Change the {@link RandomGenerator} to be used to generate random values for JazzIcon calculation.
         *
         * @param randomGenerator the {@link RandomGenerator} to be used to generate random values for JazzIcon
         *     calculation.
         * @return the builder
         */
        public JazzIconBuilder withRandomGenerator(RandomGenerator randomGenerator) {
            jazzIcon.setRandomGenerator(randomGenerator);
            return this;
        }

        /**
         * Finalize building
         *
         * @return the final JazzIcon
         */
        public JazzIcon build() {
            return jazzIcon;
        }
    }
}
