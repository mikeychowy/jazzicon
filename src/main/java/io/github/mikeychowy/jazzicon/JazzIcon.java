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
import java.security.SecureRandom;
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

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class JazzIcon {
    public static final RandomGenerator DEFAULT_RANDOM_GENERATOR = new Well512a();
    public static final int DEFAULT_SHAPE_COUNT = 4;
    public static final int DEFAULT_WOBBLE = 30;
    public static final ColorPalettes DEFAULT_BASE_COLORS = ColorPalettes.JAZZ_ICON;
    public static final String DEFAULT_ALLOWED_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    static final SecureRandom SECURE_RANDOM = new SecureRandom();
    static final String THREE_POINTS_DECIMAL_FORMAT = "%.3f";
    static final String ONE_POINT_DECIMAL_FORMAT = "%.1f";
    private static final Logger log = LoggerFactory.getLogger(JazzIcon.class);
    final ReentrantLock lock = new ReentrantLock(true);
    final List<String> svgClasses = new ArrayList<>();
    final List<String> svgStyles = new ArrayList<>();
    int shapeCount;
    int wobble;
    ColorPalettes baseColors;
    String allowedCharactersForPaddingText;
    RandomGenerator randomGenerator;

    public JazzIcon() {
        this(
                DEFAULT_SHAPE_COUNT,
                DEFAULT_WOBBLE,
                DEFAULT_BASE_COLORS,
                DEFAULT_ALLOWED_CHARACTERS,
                DEFAULT_RANDOM_GENERATOR);
    }

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

    public static String generateDataUrl(@NonNull String jazzIconSvg) {
        String encoded = Base64.getEncoder().encodeToString(jazzIconSvg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + encoded;
    }

    public static JazzIconBuilder builder() {
        return new JazzIconBuilder();
    }

    protected void nextTransform(int index, @NonNull Writer out) throws IOException {
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
    }

    protected void nextColor(@NonNull List<String> rotatedColors, @NonNull Writer out) throws IOException {
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
            log.debug("nextColor: color from list is invalid, either blank or not a valid hex color, outputting white");
            color = "#FFFFFF";
        }
        out.append(color);
    }

    public List<String> getSvgClasses() {
        return svgClasses;
    }

    public void addSvgClass(@NonNull String svgClass) {
        this.svgClasses.add(svgClass);
    }

    public void addSvgClasses(@NonNull String... svgClasses) {
        this.svgClasses.addAll(
                Arrays.stream(svgClasses).filter(StringUtils::isNotBlank).toList());
    }

    public void addSvgClasses(@NonNull List<String> svgClasses) {
        this.svgClasses.addAll(
                svgClasses.stream().filter(StringUtils::isNotBlank).toList());
    }

    public void removeSvgClass(@NonNull String svgClass) {
        this.svgClasses.remove(svgClass);
    }

    public void removeSvgClasses(@NonNull String... svgClasses) {
        this.svgClasses.removeAll(
                Arrays.stream(svgClasses).filter(StringUtils::isNotBlank).toList());
    }

    public void removeSvgClasses(@NonNull List<String> svgClasses) {
        this.svgClasses.removeAll(
                svgClasses.stream().filter(StringUtils::isNotBlank).toList());
    }

    public List<String> getSvgStyles() {
        return svgStyles;
    }

    public void addSvgStyle(@NonNull String svgStyle) {
        this.svgStyles.add(svgStyle);
    }

    public void addSvgStyles(@NonNull String... svgStyles) {
        this.svgStyles.addAll(
                Arrays.stream(svgStyles).filter(StringUtils::isNotBlank).toList());
    }

    public void addSvgStyles(@NonNull List<String> svgStyles) {
        this.svgStyles.addAll(svgStyles.stream().filter(StringUtils::isNotBlank).toList());
    }

    public void removeSvgStyle(@NonNull String svgStyle) {
        this.svgStyles.remove(svgStyle);
    }

    public void removeSvgStyles(@NonNull String... svgStyles) {
        this.svgStyles.removeAll(
                Arrays.stream(svgStyles).filter(StringUtils::isNotBlank).toList());
    }

    public void removeSvgStyles(@NonNull List<String> svgStyles) {
        this.svgStyles.removeAll(
                svgStyles.stream().filter(StringUtils::isNotBlank).toList());
    }

    protected void createShapes(@NonNull List<String> rotatedColors, @NonNull Writer out) throws IOException {
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
            log.debug("creating shape number {}", i);
            out.append("<rect x=\"0\" y=\"0\" width=\"100%\" height=\"100%\" transform=\"");
            nextTransform(i, out);
            out.append("\" fill=\"");
            nextColor(mutableRotatedColors, out);
            out.append("\" />");
        }
    }

    @SuppressWarnings("SameParameterValue")
    String randomStringFromAllowedChars(int length) {
        StringBuilder sb = new StringBuilder(length);
        int n = allowedCharactersForPaddingText.length();
        for (int i = 0; i < length; i++) {
            int idx = SECURE_RANDOM.nextInt(n);
            sb.append(allowedCharactersForPaddingText.charAt(idx));
        }
        return sb.toString();
    }

    // no need to test, convenience method only, delegates to the actual method
    @ExcludeGeneratedOrSpecialCaseFromCoverage
    public void generateIconToStream(@NonNull String text, @NonNull OutputStream outputStream)
            throws JazzIconGenerationException {
        generateIconToStream(text, outputStream, null);
    }

    public void generateIconToStream(
            @NonNull String text, @NonNull OutputStream outputStream, @Nullable Consumer<Writer> svgBodyInterceptor)
            throws JazzIconGenerationException {
        Exceptions.wrap(e -> new JazzIconGenerationException(
                        "An error has been encountered while trying to generate icon to stream", e))
                .run(() -> {
                    try (OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                        generateIconToWriter(text, osw, svgBodyInterceptor);
                        osw.flush();
                    }
                });
    }

    private long tryBestGetSeedFromText(@NonNull String safeText) {
        try {
            return Long.parseUnsignedLong(safeText.substring(2, Math.min(10, safeText.length())), 16);
        } catch (NumberFormatException e) {
            log.debug("Could not parse text '{}' to Long, falling back to hashCode", safeText, e);
            // fallback to hash if not hex
            return safeText.hashCode();
        }
    }

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
                                    "original text is too short, padding 6 characters to left and right (respectively) from allow"
                                            + " list: {}",
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

                        // in case needs to add other shapes or whatever before appending tail
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

    public String generateIcon(@NonNull String text, @Nullable Consumer<Writer> svgBodyInterceptor)
            throws JazzIconGenerationException {
        return Exceptions.wrap(e -> new JazzIconGenerationException("error while generating icon", e))
                .get(() -> {
                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        generateIconToStream(text, outputStream, svgBodyInterceptor);
                        return outputStream.toString(StandardCharsets.UTF_8);
                    }
                });
    }

    public String generateIcon(@NonNull String text) {
        return generateIcon(text, null);
    }

    public String generateIconWithInitials(@NonNull String name) throws JazzIconGenerationException {
        return Exceptions.wrap(e -> new JazzIconGenerationException("error while generating icon", e))
                .get(() -> {
                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        var initials = NameUtils.getInitials(name);
                        generateIconToStream(
                                name,
                                outputStream,
                                Exceptions.wrap(e -> new JazzIconGenerationException(
                                                "error while generating icon with initials", e))
                                        .consumer(out -> {
                                            out.append(
                                                    "<text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dominant-baseline=\"middle\" class=\"fill-white font-bold text-[30px] font-sans\">");
                                            out.append(initials);
                                            out.append("</text>");
                                        }));
                        return outputStream.toString(StandardCharsets.UTF_8);
                    }
                });
    }

    public int getShapeCount() {
        return shapeCount;
    }

    public JazzIcon setShapeCount(int shapeCount) {
        if (shapeCount <= 0) {
            throw new IllegalArgumentException("shapeCount must be > 0");
        }
        if (shapeCount + 1 > baseColors.getColors().size()) {
            throw new IllegalArgumentException(
                    "Insufficient base colors, shapeCount list size must be higher than shapeCount + 1");
        }
        this.shapeCount = shapeCount;
        return this;
    }

    public int getWobble() {
        return wobble;
    }

    public JazzIcon setWobble(int wobble) {
        if (wobble <= 0) {
            throw new IllegalArgumentException("wobble must be > 0");
        }
        this.wobble = wobble;
        return this;
    }

    @NonNull
    public ColorPalettes getBaseColors() {
        return baseColors;
    }

    public JazzIcon setBaseColors(ColorPalettes baseColors) {
        if (Objects.isNull(baseColors)) {
            throw new IllegalArgumentException("baseColors must not be null");
        }
        if (shapeCount + 1 > baseColors.getColors().size()) {
            throw new IllegalArgumentException(
                    "Insufficient base colors, shapeCount list size must be higher than shapeCount + 1");
        }
        this.baseColors = baseColors;
        return this;
    }

    @NonNull
    public String getAllowedCharactersForPaddingText() {
        return allowedCharactersForPaddingText;
    }

    public JazzIcon setAllowedCharactersForPaddingText(String allowedCharactersForPaddingText) {
        if (StringUtils.isBlank(allowedCharactersForPaddingText)) {
            throw new IllegalArgumentException("allowedCharacters must not be just blanks, an empty string or null");
        }
        this.allowedCharactersForPaddingText = allowedCharactersForPaddingText;
        return this;
    }

    @NonNull
    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    public JazzIcon setRandomGenerator(RandomGenerator randomGenerator) {
        if (Objects.isNull(randomGenerator)) {
            throw new IllegalArgumentException("randomGenerator must not be null");
        }
        this.randomGenerator = randomGenerator;
        return this;
    }

    public record JazzIconBuilder(JazzIcon jazzIcon) {
        public JazzIconBuilder() {
            this(new JazzIcon());
        }

        public JazzIconBuilder withShapeCount(int shapeCount) {
            jazzIcon.setShapeCount(shapeCount);
            return this;
        }

        public JazzIconBuilder withWobble(int wobble) {
            jazzIcon.setWobble(wobble);
            return this;
        }

        public JazzIconBuilder withBaseColors(ColorPalettes baseColors) {
            jazzIcon.setBaseColors(baseColors);
            return this;
        }

        public JazzIconBuilder withAllowedCharactersForPaddingText(String allowedCharacters) {
            jazzIcon.setAllowedCharactersForPaddingText(allowedCharacters);
            return this;
        }

        public JazzIconBuilder withRandomGenerator(RandomGenerator randomGenerator) {
            jazzIcon.setRandomGenerator(randomGenerator);
            return this;
        }

        public JazzIcon build() {
            return jazzIcon;
        }
    }
}
