package io.github.mikeychowy.jazzicon;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class ColorUtilsTest {

    static List<String> validInputs = List.of("#FFF", "#fff", "734652", "BF567D", "c69d78", "#C63");
    static List<String> invalidInputs = List.of("#thunder", "203XTY847", "xzaw344w", "(*&^%^%&^)");

    @ParameterizedTest
    @FieldSource("invalidInputs")
    void test_isValidHexColor_returningFalse_whenInputsAreInvalid(String input) {
        var result = ColorUtils.isValidHexColor(input);
        Assertions.assertThat(result).isFalse();
    }

    @ParameterizedTest
    @FieldSource("validInputs")
    void test_isValidHexColor_returningTrue_whenInputsAreValid(String input) {
        var result = ColorUtils.isValidHexColor(input);
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void test_isValidHexColor_returningFalse_whenInputIsNull() {
        var result = ColorUtils.isValidHexColor(null);
        Assertions.assertThat(result).isFalse();
    }
}
