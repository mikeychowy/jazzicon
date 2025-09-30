package io.github.mikeychowy.jazzicon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.assertj.core.api.ThrowableAssert;
import org.hipparchus.random.RandomGenerator;
import org.hipparchus.random.Well1024a;
import org.hipparchus.random.Well512a;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JazzIconTest {

    private ThrowableAssert.ThrowingCallable setupJazzIconThrowingCondition(Consumer<JazzIcon> consumer) {
        return () -> {
            JazzIcon jazzIcon = new JazzIcon();
            assertThat(jazzIcon).isNotNull();
            consumer.accept(jazzIcon);
        };
    }

    @Test
    void test_constructorsValidAndGettersSetters_andCheckEmptyConstructorHasDefaultValues() {
        // default constructor
        JazzIcon jazzIcon = new JazzIcon();
        // also, check getters and default values
        assertThat(jazzIcon).isNotNull();
        assertThat(jazzIcon.getBaseColors()).isNotNull().isEqualTo(JazzIcon.DEFAULT_BASE_COLORS);
        assertThat(jazzIcon.getAllowedCharactersForPaddingText())
                .isNotBlank()
                .isEqualTo(JazzIcon.DEFAULT_ALLOWED_CHARACTERS);
        assertThat(jazzIcon.getRandomGenerator().getClass()).isNotNull().isEqualTo(Well512a.class);
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

        ex = catchThrowableOfType(IllegalArgumentException.class, setupJazzIconThrowingCondition(jazzIcon -> {
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
    void test_nextColor_whenRotatedColorsListIndexIsBlank_returningWhiteHex() {
        var rotatedColors = new ArrayList<String>();
        rotatedColors.add("");
        rotatedColors.add("");
        rotatedColors.add("");
        var writer = new StringWriter();
        var mockRandomGenerator = mock(RandomGenerator.class);
        var jazzIcon = new JazzIcon();
        jazzIcon.setRandomGenerator(mockRandomGenerator);
        when(mockRandomGenerator.nextDouble())
                .thenReturn(100.0)
                .thenReturn(100.0)
                .thenReturn(0.7);
        var ex = catchThrowable(() -> jazzIcon.nextColor(rotatedColors, writer));
        assertThat(ex).isNull();
        var result = writer.toString();
        assertThat(result).isNotBlank().isEqualTo("#FFFFFF");
    }

    @Test
    void test_nextColor_whenRotatedColorsListIndexIsNotValidHexColor_returningWhiteHex() {
        var rotatedColors = new ArrayList<String>();
        rotatedColors.add("XXXXX");
        rotatedColors.add("QQQQ");
        rotatedColors.add("TTTT");
        var writer = new StringWriter();
        var mockRandomGenerator = mock(RandomGenerator.class);
        var jazzIcon = new JazzIcon();
        jazzIcon.setRandomGenerator(mockRandomGenerator);
        when(mockRandomGenerator.nextDouble()).thenReturn(-1.0).thenReturn(-1.0).thenReturn(0.7);
        var ex = catchThrowable(() -> jazzIcon.nextColor(rotatedColors, writer));
        assertThat(ex).isNull();
        var result = writer.toString();
        assertThat(result).isNotBlank().isEqualTo("#FFFFFF");
    }

    @Test
    void test_generateDataUrl_success() {
        var result = JazzIcon.generateDataUrl("ABC");
        assertThat(result).isNotNull().isNotBlank().contains("data:image/svg+xml;base64,");
    }

    @Test
    void test_rotateColor_whenColorShiftLessThanZero_returningTheNegativeRemainderAgainst360() {
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

    @Test
    void test_generateIcon_baseFunctionality_success() {
        var jazzIcon = new JazzIcon();

        var result = jazzIcon.generateIcon("Harry");
        assertThat(result)
                .isNotNull()
                .isNotBlank()
                .containsOnlyOnce("<svg ")
                .containsOnlyOnce("</svg>")
                .containsAnyOf("<rect", "<circle", "<polygon");

        result = jazzIcon.generateIcon("ghh");
        assertThat(result)
                .isNotNull()
                .isNotBlank()
                .containsOnlyOnce("<svg ")
                .containsOnlyOnce("</svg>")
                .containsAnyOf("<rect", "<circle", "<polygon");
    }

    @Test
    void test_generateIcon_whenClassesAndStylesNotEmpty_classesAndStylesGeneratedInSVG() {
        var jazzIcon = new JazzIcon();
        jazzIcon.addSvgClass("show");
        jazzIcon.addSvgStyle("padding: 0;");

        var result = jazzIcon.generateIcon("Harry");
        assertThat(result)
                .isNotNull()
                .isNotBlank()
                .containsOnlyOnce("<svg ")
                .containsOnlyOnce("</svg>")
                .containsAnyOf("<rect", "<circle", "<polygon")
                .contains("class=\"show\"")
                .contains("style=\"padding: 0;\"");
    }

    @Test
    void test_generateIcon_whenBodyInterceptorIsNotEmpty_bodyOperationSuccess() {
        var jazzIcon = new JazzIcon();

        var result = jazzIcon.generateIcon("Harry", writer -> {
            try {
                writer.write(MessageFormat.format(
                        "<text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dominant-baseline=\"middle\" class=\"fill-white font-bold text-[30px] font-sans\">{0}</text>",
                        InitialUtils.getInitials("Harry")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        assertThat(result)
                .isNotNull()
                .isNotBlank()
                .containsOnlyOnce("<svg ")
                .containsOnlyOnce("</svg>")
                .containsAnyOf("<rect", "<circle", "<polygon")
                .contains(
                        "<text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dominant-baseline=\"middle\" class=\"fill-white font-bold text-[30px] font-sans\">H</text>");
    }

    @Test
    void test_generateIconToStream_success() {
        var jazzIcon = new JazzIcon();
        assertThatNoException().isThrownBy(() -> {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                jazzIcon.generateIconToStream("Harry", outputStream);
                var icon = outputStream.toString(StandardCharsets.UTF_8);
                assertThat(icon)
                        .isNotNull()
                        .isNotBlank()
                        .containsOnlyOnce("<svg ")
                        .containsOnlyOnce("</svg>")
                        .containsAnyOf("<rect", "<circle", "<polygon");
            }
        });
    }

    @Test
    void test_generateIconToWriter_whenIOExceptionOccurs_throwsJazzIconGenerationException() throws IOException {
        var jazzIcon = new JazzIcon();
        var mockWriter = mock(Writer.class);
        doThrow(new IOException("sumting wen wong")).when(mockWriter).append(anyString());

        var ex = catchThrowableOfType(
                JazzIconGenerationException.class, () -> jazzIcon.generateIconToWriter("", mockWriter, null));
        assertThat(ex)
                .isNotNull()
                .hasCauseExactlyInstanceOf(IOException.class)
                .hasMessageContaining("An error has been encountered while trying to generate icon to writer")
                .hasRootCauseMessage("sumting wen wong");
    }

    @Test
    void test_generateIcon_isValidSvg() {
        var svg = new JazzIcon().generateIcon("Harry");
        var valid = SvgUtil.isValidSvg(svg);
        assertThat(valid).isTrue();
    }

    @Test
    void test_generateIconWithClassesAndStyles_isValidSvg() {
        var jazzIcon = new JazzIcon();
        jazzIcon.addSvgClasses("show", "px-5");
        jazzIcon.addSvgStyles("padding: 0;", "margin: 0;");
        var svg = jazzIcon.generateIcon("Harry");
        var valid = SvgUtil.isValidSvg(svg);
        assertThat(valid).isTrue();
    }

    @Test
    void test_generateIcon_isSecureSvg() {
        Assertions.assertDoesNotThrow(() -> {
            var svg = new JazzIcon().generateIcon("Harry");
            SvgUtil.checkSvgSecureFromXSS(svg);
        });
    }

    @Test
    void test_generateIconWithClassesAndStyles_isSecureSvg() {
        Assertions.assertDoesNotThrow(() -> {
            var jazzIcon = new JazzIcon();
            jazzIcon.addSvgClass("show");
            jazzIcon.addSvgStyle("padding: 0;");
            var svg = jazzIcon.generateIcon("Harry");
            SvgUtil.checkSvgSecureFromXSS(svg);
        });
    }

    @Test
    void test_generateIcon_isThreadSafe() {
        var jazzIcon = new JazzIcon();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicReference<String> harrySvg = new AtomicReference<>();
        AtomicReference<String> lindaSvg = new AtomicReference<>();
        var t1 = CompletableFuture.runAsync(() -> harrySvg.set(jazzIcon.generateIcon("Harry")), executor);
        var t2 = CompletableFuture.runAsync(() -> lindaSvg.set(jazzIcon.generateIcon("Linda")), executor);
        CompletableFuture.allOf(t1, t2).join();

        assertThat(harrySvg.get()).isNotBlank();
        assertThat(lindaSvg.get()).isNotBlank();
        assertThat(harrySvg.get()).isNotEqualToIgnoringCase(lindaSvg.get());

        AtomicReference<String> harrySvg2 = new AtomicReference<>();
        t1 = CompletableFuture.runAsync(() -> harrySvg.set(jazzIcon.generateIcon("Harry")), executor);
        t2 = CompletableFuture.runAsync(() -> harrySvg2.set(jazzIcon.generateIcon("Harry")), executor);
        CompletableFuture.allOf(t1, t2).join();

        assertThat(harrySvg.get()).isNotBlank();
        assertThat(harrySvg2.get()).isNotBlank();
        assertThat(harrySvg.get()).isEqualTo(harrySvg2.get());
    }

    @Test
    void test_equals() {
        assertThat(new JazzIcon()).isEqualTo(new JazzIcon());
        assertThat(new JazzIcon()).isNotEqualTo(null);
        assertThat(new JazzIcon()).isNotEqualTo(new String[] {});

        var jazzIcon = new JazzIcon()
                .setWobble(50)
                .setShapeCount(5)
                .setBaseColors(ColorPalettes.TAILWIND)
                .setRandomGenerator(new Well1024a());
        var thatJazzIcon = new JazzIcon()
                .setWobble(50)
                .setShapeCount(5)
                .setBaseColors(ColorPalettes.TAILWIND)
                .setRandomGenerator(new Well1024a());
        assertThat(jazzIcon).isEqualTo(thatJazzIcon);
        thatJazzIcon.setRandomGenerator(new Well512a());
        assertThat(jazzIcon).isNotEqualTo(thatJazzIcon);
        thatJazzIcon.setBaseColors(ColorPalettes.CATPUCCIN_FRAPPE);
        assertThat(jazzIcon).isNotEqualTo(thatJazzIcon);
        thatJazzIcon.setWobble(10);
        assertThat(jazzIcon).isNotEqualTo(thatJazzIcon);
        thatJazzIcon.setShapeCount(1);
        assertThat(jazzIcon).isNotEqualTo(thatJazzIcon);

        thatJazzIcon
                .setWobble(50)
                .setShapeCount(5)
                .setBaseColors(ColorPalettes.TAILWIND)
                .setRandomGenerator(new Well1024a());
    }

    @Test
    void test_hashCode() {
        var jazzIcon = new JazzIcon();
        var hc1 = jazzIcon.hashCode();
        var hc2 = jazzIcon.hashCode();
        assertThat(hc1).isEqualTo(hc2);
    }
}
