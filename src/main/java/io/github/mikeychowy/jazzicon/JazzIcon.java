package io.github.mikeychowy.jazzicon;

import com.github.ajalt.colormath.RenderCondition;
import com.github.ajalt.colormath.model.HSV;
import com.github.ajalt.colormath.model.RGB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.hipparchus.random.RandomGenerator;
import org.hipparchus.random.Well512a;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unused"})
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
    private final ReentrantLock lock = new ReentrantLock(true);
    private final List<String> svgClasses = new ArrayList<>();
    private final List<String> svgStyles = new ArrayList<>();
    private int shapeCount;
    private int wobble;
    private ColorPalettes baseColors;
    private String allowedCharacters;
    private RandomGenerator randomGenerator;

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
            String allowedCharacters,
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
        if (StringUtils.isBlank(allowedCharacters)) {
            throw new IllegalArgumentException("allowedCharacters is required");
        }

        if (shapeCount + 1 > baseColors.getColors().size()) {
            throw new IllegalArgumentException(
                    "Insufficient base colors, shape count list size must be higher than shape count + 1");
        }

        this.allowedCharacters = allowedCharacters;
        if (Objects.isNull(randomGenerator)) {
            throw new IllegalArgumentException("randomGenerator must not be null");
        }
        this.randomGenerator = randomGenerator;
    }

    private static String rotateColor(@NonNull String hexColor, double hueShift) {
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

    private void nextTransform(int index, Writer out) throws IOException {
        double firstRotation = randomGenerator.nextDouble();
        double boost = randomGenerator.nextDouble();
        double secondRotation = randomGenerator.nextDouble();
        double angle = 2 * Math.PI * firstRotation;
        double velocity = (100 * (index + boost)) / shapeCount;
        double x = Math.cos(angle) * velocity;
        double y = Math.sin(angle) * velocity;
        double r = firstRotation * 360 + secondRotation * 180;

        out.write("translate(%s %s) rotate(%s 50 50)"
                .formatted(
                        String.format(Locale.US, THREE_POINTS_DECIMAL_FORMAT, x),
                        String.format(Locale.US, THREE_POINTS_DECIMAL_FORMAT, y),
                        String.format(Locale.US, ONE_POINT_DECIMAL_FORMAT, r)));
    }

    private void nextColor(@NonNull List<String> rotatedColors, Writer out) throws IOException {
        randomGenerator.nextInt();
        var position = randomGenerator.nextDouble();
        int index = (int) Math.floor((rotatedColors.size() - 1) * position);
        while (index >= rotatedColors.size() || index < 0) {
            position = randomGenerator.nextInt();
            index = (int) Math.floor((rotatedColors.size() - 1) * position);
        }

        var color = rotatedColors.remove(index);
        if (StringUtils.isBlank(color) || !ColorUtils.isValidHexColor(color)) {
            color = "#FFFFFF";
        }
        out.write(color);
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

    private void createShapes(@NonNull List<String> rotatedColors, Writer out) throws IOException {
        List<String> shapes = new ArrayList<>();

        // first line
        out.write("rect x=\"0\" y=\"0\" width=\"100%\" height=\"100%\" fill=\"");
        nextColor(rotatedColors, out);
        out.write("\" />");

        for (int i = 0; i < shapeCount; i++) {
            out.write("<rect x=\"0\" y=\"0\" width=\"100%\" height=\"100%\" transform=\"");
            nextTransform(i, out);
            out.write("\" fill=\"");
            nextColor(rotatedColors, out);
            out.write("\" />");
        }
    }

    private String randomStringFromAllowedChars(int length) {
        StringBuilder sb = new StringBuilder(length);
        int n = allowedCharacters.length();
        for (int i = 0; i < length; i++) {
            int idx = SECURE_RANDOM.nextInt(n);
            sb.append(allowedCharacters.charAt(idx));
        }
        return sb.toString();
    }

    public void generateIconToStream(@NonNull String text, @NonNull OutputStream outputStream) throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            generateIconToWriter(text, osw, null);
            osw.flush();
        }
    }

    private void generateIconToWriter(
            @NonNull String text, @NonNull Writer out, @Nullable Consumer<Writer> writerConsumer) {
        String safeText = StringUtils.trimToEmpty(text);
        if (safeText.length() <= 3) {
            safeText = randomStringFromAllowedChars(6) + safeText + randomStringFromAllowedChars(6);
        }

        Set<String> baseActualColors = baseColors.getColors();

        if (shapeCount + 1 > baseColors.getColors().size()) {
            throw new IllegalArgumentException(
                    "Insufficient base colors, shape count list size must be higher than shape count + 1");
        }

        long seed;
        try {
            seed = Long.parseUnsignedLong(safeText.substring(2, Math.min(10, safeText.length())), 16);
        } catch (NumberFormatException e) {
            // fallback to hash if not hex
            seed = safeText.hashCode();
        }

        try {
            // lock the randomGenerator
            lock.lock();
            randomGenerator.setSeed(seed);

            double position = randomGenerator.nextDouble();
            double hueShift = (30 * position) - (wobble / 2.0F);

            List<String> rotatedColors = baseActualColors.stream()
                    .map(base -> rotateColor(base, hueShift))
                    .collect(Collectors.toList());

            // append head
            out.write("<svg ");
            if (!svgClasses.isEmpty()) {
                out.write("class=\"" + String.join(" ", svgClasses) + "\" ");
            }
            if (!svgStyles.isEmpty()) {
                out.write("style=\"" + String.join(" ", svgStyles) + "\" ");
            }
            out.write("xmlns=\"http://www.w3.org/2000/svg\" x=\"0\" y=\"0\" viewBox=\"0 0 100 100\">");

            createShapes(rotatedColors, out);

            if (Objects.nonNull(writerConsumer)) {
                writerConsumer.accept(out);
            }

            // append tail
            out.write("</svg>");
        } catch (IOException e) {
            throw new JazzIconGenerationException(
                    "An error with the Writer has been encountered while trying to generate JazzIcon", e);
        } finally {
            lock.unlock();
        }
    }

    public String generateIcon(@NonNull String text) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            generateIconToStream(text, baos);
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JazzIconGenerationException("error while generating icon to writer", e);
        }
    }

    public String generateIconWithInitials(@NonNull String name) {
        var jazzIcon = generateIcon(name);
        var initials = NameUtils.getInitials(name);

        return RegExUtils.replaceFirst(
                jazzIcon,
                "</svg>",
                MessageFormat.format(
                        "<text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dominant-baseline=\"middle\" class=\"fill-white font-bold text-[30px] font-sans\">{0}</text></svg>",
                        initials));
    }

    public int getShapeCount() {
        return shapeCount;
    }

    public void setShapeCount(int shapeCount) {
        if (shapeCount <= 0) {
            throw new IllegalArgumentException("shapeCount must be > 0");
        }
        if (shapeCount + 1 > baseColors.getColors().size()) {
            throw new IllegalArgumentException(
                    "Insufficient base colors, shape count list size must be higher than shape count + 1");
        }
        this.shapeCount = shapeCount;
    }

    public int getWobble() {
        return wobble;
    }

    public void setWobble(int wobble) {
        if (wobble <= 0) {
            throw new IllegalArgumentException("wobble must be > 0");
        }
        this.wobble = wobble;
    }

    @NonNull public ColorPalettes getBaseColors() {
        return baseColors;
    }

    public void setBaseColors(ColorPalettes baseColors) {
        if (Objects.isNull(baseColors)) {
            throw new IllegalArgumentException("baseColors must not be null");
        }
        if (shapeCount + 1 > baseColors.getColors().size()) {
            throw new IllegalArgumentException(
                    "Insufficient base colors, shape count list size must be higher than shape count + 1");
        }
        this.baseColors = baseColors;
    }

    @NonNull public String getAllowedCharacters() {
        return allowedCharacters;
    }

    public void setAllowedCharacters(String allowedCharacters) {
        if (StringUtils.isBlank(allowedCharacters)) {
            throw new IllegalArgumentException("allowedCharacters must not be just blanks, an empty string or null");
        }
        this.allowedCharacters = allowedCharacters;
    }

    @NonNull public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    public void setRandomGenerator(RandomGenerator randomGenerator) {
        if (Objects.isNull(randomGenerator)) {
            throw new IllegalArgumentException("randomGenerator must not be null");
        }
        this.randomGenerator = randomGenerator;
    }
}
