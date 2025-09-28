package io.github.mikeychowy.jazzicon;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ColorUtilsTest {

    @Test
    void test_isValidHexColor_returningFalse_whenInputsAreInvalid() {
        List.of("#FFF", "#fff", "734652", "BF567D", "c69d78", "#C63").forEach(color -> {
            var result = ColorUtils.isValidHexColor(color);
            assertThat(result).isTrue();
        });
    }

    @Test
    void test_isValidHexColor_returningTrue_whenInputsAreValid() {
        List.of("#thunder", "203XTY847", "xzaw344w", "(*&^%^%&^)").forEach(color -> {
            var result = ColorUtils.isValidHexColor(color);
            assertThat(result).isFalse();
        });
    }

    @Test
    void test_isValidHexColor_returningFalse_whenInputIsInvalid() {
        var result = ColorUtils.isValidHexColor("XXX");
        assertThat(result).isFalse();
    }

    @Test
    @SuppressWarnings("ConstantValue")
    void test_isValidHexColor_returningFalse_whenInputIsNull() {
        var result = ColorUtils.isValidHexColor(null);
        assertThat(result).isFalse();
    }
}
