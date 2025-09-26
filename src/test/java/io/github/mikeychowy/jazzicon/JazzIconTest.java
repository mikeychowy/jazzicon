package io.github.mikeychowy.jazzicon;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.assertj.core.api.ThrowableAssert;
import org.hipparchus.random.RandomGenerator;
import org.hipparchus.random.Well1024a;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class JazzIconTest {
    private static final Logger log = LoggerFactory.getLogger(JazzIconTest.class);

    private ThrowableAssert.ThrowingCallable setupJazzIconThrowingCondition(Consumer<JazzIcon> consumer) {
        return () -> {
            JazzIcon jazzIcon = new JazzIcon();
            assertThat(jazzIcon).isNotNull();
            consumer.accept(jazzIcon);
        };
    }

    @Test
    void test_constructorsAndGettersSetters_andCheckEmptyConstructorHasDefaultValues() {
        // default constructor
        JazzIcon jazzIcon = new JazzIcon();
        // also, check getters and default values
        assertThat(jazzIcon).isNotNull();
        assertThat(jazzIcon.getBaseColors()).isNotNull().isEqualTo(JazzIcon.DEFAULT_BASE_COLORS);
        assertThat(jazzIcon.getAllowedCharactersForPaddingText())
                .isNotBlank()
                .isEqualTo(JazzIcon.DEFAULT_ALLOWED_CHARACTERS);
        assertThat(jazzIcon.getRandomGenerator()).isNotNull().isEqualTo(JazzIcon.DEFAULT_RANDOM_GENERATOR);
        assertThat(jazzIcon.getShapeCount()).isEqualTo(JazzIcon.DEFAULT_SHAPE_COUNT);
        assertThat(jazzIcon.getWobble()).isEqualTo(JazzIcon.DEFAULT_WOBBLE);

        // specific constructor
        jazzIcon = new JazzIcon(5, 60, ColorPalettes.CHART_COLORS, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", new Well1024a());
        // also, check getters, setters and specified values
        // setters are checking for happy path first
        assertThat(jazzIcon).isNotNull();
        jazzIcon.setBaseColors(ColorPalettes.CHART_COLORS);
        assertThat(jazzIcon.getBaseColors()).isNotNull().isEqualTo(ColorPalettes.CHART_COLORS);
        jazzIcon.setAllowedCharactersForPaddingText("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        assertThat(jazzIcon.getAllowedCharactersForPaddingText()).isNotBlank().isEqualTo("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        jazzIcon.setRandomGenerator(new Well1024a());
        assertThat(jazzIcon.getRandomGenerator()).isNotNull().isOfAnyClassIn(Well1024a.class);
        jazzIcon.setShapeCount(5);
        assertThat(jazzIcon.getShapeCount()).isEqualTo(5);
        jazzIcon.setWobble(60);
        assertThat(jazzIcon.getWobble()).isEqualTo(60);

        // svg classes and styles
        jazzIcon = new JazzIcon();
        assertThat(jazzIcon).isNotNull();
        jazzIcon.addSvgClass("test");
        assertThat(jazzIcon.getSvgClasses()).isNotEmpty().contains("test");
        jazzIcon.addSvgClasses("abc", "qwe");
        assertThat(jazzIcon.getSvgClasses()).isNotEmpty().contains("abc", "qwe");
        jazzIcon.addSvgClasses(List.of("WRT", "ZXC"));
        assertThat(jazzIcon.getSvgClasses()).isNotEmpty().contains("WRT", "ZXC");
        jazzIcon.removeSvgClass("test");
        assertThat(jazzIcon.getSvgClasses()).isNotEmpty().doesNotContain("test");
        jazzIcon.removeSvgClasses("abc", "qwe");
        assertThat(jazzIcon.getSvgClasses()).isNotEmpty().doesNotContain("abc", "qwe");
        jazzIcon.removeSvgClasses(List.of("WRT", "ZXC"));
        assertThat(jazzIcon.getSvgClasses()).isEmpty();

        jazzIcon.addSvgStyle("test");
        assertThat(jazzIcon.getSvgStyles()).isNotEmpty().contains("test");
        jazzIcon.addSvgStyles("abc", "qwe");
        assertThat(jazzIcon.getSvgStyles()).isNotEmpty().contains("abc", "qwe");
        jazzIcon.addSvgStyles(List.of("WRT", "ZXC"));
        assertThat(jazzIcon.getSvgStyles()).isNotEmpty().contains("WRT", "ZXC");
        jazzIcon.removeSvgStyle("test");
        assertThat(jazzIcon.getSvgStyles()).isNotEmpty().doesNotContain("test");
        jazzIcon.removeSvgStyles("abc", "qwe");
        assertThat(jazzIcon.getSvgStyles()).isNotEmpty().doesNotContain("abc", "qwe");
        jazzIcon.removeSvgStyles(List.of("WRT", "ZXC"));
        assertThat(jazzIcon.getSvgStyles()).isEmpty();
    }

    @Test
    void test_settersAndAllArgsConstructorValidations_areWorking() {
        // check setters
        var ex = catchThrowableOfType(
                IllegalArgumentException.class,
                setupJazzIconThrowingCondition(jazzIcon -> jazzIcon.setAllowedCharactersForPaddingText(null)));
        assertThat(ex).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("null");

        ex = catchThrowableOfType(
                IllegalArgumentException.class,
                setupJazzIconThrowingCondition(jazzIcon -> jazzIcon.setRandomGenerator(null)));
        assertThat(ex).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("null");

        ex = catchThrowableOfType(
                IllegalArgumentException.class,
                setupJazzIconThrowingCondition(jazzIcon -> jazzIcon.setBaseColors(null)));
        assertThat(ex).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("null");

        ex = catchThrowableOfType(
                IllegalArgumentException.class, setupJazzIconThrowingCondition(jazzIcon -> jazzIcon.setWobble(-1)));
        assertThat(ex).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("must be > 0");

        ex = catchThrowableOfType(
                IllegalArgumentException.class, setupJazzIconThrowingCondition(jazzIcon -> jazzIcon.setShapeCount(-1)));
        assertThat(ex).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("must be > 0");

        ex = catchThrowableOfType(
                IllegalArgumentException.class,
                setupJazzIconThrowingCondition(jazzIcon -> jazzIcon.setShapeCount(1000)));
        assertThat(ex)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be higher than shapeCount");

        ex = catchThrowableOfType(
                IllegalArgumentException.class, setupJazzIconThrowingCondition(jazzIcon -> {
                    jazzIcon.setShapeCount(9);
                    jazzIcon.setBaseColors(ColorPalettes.SECTION_1);
                }));
        assertThat(ex)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be higher than shapeCount");

        // check all-args constructor validation
        ex = catchThrowableOfType(
                IllegalArgumentException.class,
                () -> new JazzIcon(-1, 60, ColorPalettes.CHART_COLORS, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", new Well1024a()));
        assertThat(ex).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("must be > 0");

        ex = catchThrowableOfType(
                IllegalArgumentException.class,
                () -> new JazzIcon(5, -1, ColorPalettes.CHART_COLORS, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", new Well1024a()));
        assertThat(ex).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("must be > 0");

        ex = catchThrowableOfType(
                IllegalArgumentException.class,
                () -> new JazzIcon(5, 60, null, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", new Well1024a()));
        assertThat(ex).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("null");

        ex = catchThrowableOfType(
                IllegalArgumentException.class,
                () -> new JazzIcon(5, 60, ColorPalettes.CHART_COLORS, null, new Well1024a()));
        assertThat(ex).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("null");

        ex = catchThrowableOfType(
                IllegalArgumentException.class, () -> new JazzIcon(5, 60, ColorPalettes.CHART_COLORS, "QWERTY", null));
        assertThat(ex).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("null");

        ex = catchThrowableOfType(
                IllegalArgumentException.class,
                () -> new JazzIcon(1000, 60, ColorPalettes.CHART_COLORS, "QWERTY", new Well1024a()));
        assertThat(ex)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be higher than shapeCount");
    }

    @Test
    void test_generateIcon_baseFunctionality_success() {
        var jazzIcon = new JazzIcon();

        var result = jazzIcon.generateIcon("Harry");
        assertThat(result)
                .isNotNull()
                .isNotBlank()
                .containsOnlyOnce("<svg ")
                .containsOnlyOnce("</svg>")
                .contains("<rect");

        result = jazzIcon.generateIcon("ghh");
        assertThat(result)
                .isNotNull()
                .isNotBlank()
                .containsOnlyOnce("<svg ")
                .containsOnlyOnce("</svg>")
                .contains("<rect");
    }

    @Test
    void test_nextColor_returningWhiteHex_whenRotatedColorsListIndexIsBlank() {
        var rotatedColors = new ArrayList<String>();
        rotatedColors.add("");
        rotatedColors.add("");
        rotatedColors.add("");
        var writer = new StringWriter();
        var mockRandomGenerator = Mockito.mock(RandomGenerator.class);
        var jazzIcon = new JazzIcon();
        jazzIcon.setRandomGenerator(mockRandomGenerator);
        Mockito.when(mockRandomGenerator.nextDouble())
                .thenReturn(100.0)
                .thenReturn(100.0)
                .thenReturn(0.7);
        var ex = catchThrowable(() -> jazzIcon.nextColor(rotatedColors, writer));
        assertThat(ex).isNull();
        var result = writer.toString();
        assertThat(result).isNotBlank().isEqualTo("#FFFFFF");
    }

    @Test
    void test_nextColor_returningWhiteHex_whenRotatedColorsListIndexIsNotValidHexColor() {
        var rotatedColors = new ArrayList<String>();
        rotatedColors.add("XXXXX");
        rotatedColors.add("QQQQ");
        rotatedColors.add("TTTT");
        var writer = new StringWriter();
        var mockRandomGenerator = Mockito.mock(RandomGenerator.class);
        var jazzIcon = new JazzIcon();
        jazzIcon.setRandomGenerator(mockRandomGenerator);
        Mockito.when(mockRandomGenerator.nextDouble())
                .thenReturn(-1.0)
                .thenReturn(-1.0)
                .thenReturn(0.7);
        var ex = catchThrowable(() -> jazzIcon.nextColor(rotatedColors, writer));
        assertThat(ex).isNull();
        var result = writer.toString();
        assertThat(result).isNotBlank().isEqualTo("#FFFFFF");
    }

    @Test
    void test_generateDataUrl() {
        var jazzIcon = new JazzIcon();
        var result = jazzIcon.generateDataUrl("ABC");
        assertThat(result).isNotNull().isNotBlank().contains("data:image/svg+xml;base64,");
    }

    @Test
    void test_rotateColor_returningWheel_whenColorShiftLessThanZero() {
        var result = JazzIcon.rotateColor("#ff0000", -1.0);
        assertThat(result).isNotBlank().containsOnlyOnce("#").hasSizeGreaterThan(6);
    }

    @Test
    void test_builder() {
        var jazzIcon = JazzIcon.builder()
                .withAllowedCharactersForPaddingText("ASD")
                .withBaseColors(ColorPalettes.CHART_COLORS)
                .withShapeCount(10)
                .withWobble(15)
                .withRandomGenerator(new Well1024a())
                .build();
        assertThat(jazzIcon).isNotNull();
        assertThat(jazzIcon.getAllowedCharactersForPaddingText()).isEqualTo("ASD");
        assertThat(jazzIcon.getBaseColors()).isNotNull().isEqualTo(ColorPalettes.CHART_COLORS);
        assertThat(jazzIcon.getShapeCount()).isEqualTo(10);
        assertThat(jazzIcon.getWobble()).isEqualTo(15);
        assertThat(jazzIcon.getRandomGenerator())
                .isInstanceOf(RandomGenerator.class)
                .isOfAnyClassIn(Well1024a.class);
    }
}
