package io.github.mikeychowy.jazzicon;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InitialUtilsTest {

    @Test
    void test_getInitials_returningCorrectInitials() {
        var result = InitialUtils.getInitials("Orang Bandung");
        assertThat(result).isEqualTo("OB");
        result = InitialUtils.getInitials("Henry Sibutar-butar");
        assertThat(result).isEqualTo("HS");
        result = InitialUtils.getInitials("Ini_nama_pakai_underscore");
        assertThat(result).isEqualTo("I");
        result = InitialUtils.getInitials("");
        assertThat(result).isEqualTo("?");
    }

    @Test
    void test_generateIconWithInitials_isValidSvg() {
        var jazzIcon = new JazzIcon();
        var svg = InitialUtils.generateIconWithInitials(jazzIcon, "Harry");
        var valid = SvgUtil.isValidSvg(svg);
        assertThat(valid).isTrue();
    }

    @Test
    void test_generateIconWithInitials_isSecureSvg() {
        Assertions.assertDoesNotThrow(() -> {
            var jazzIcon = new JazzIcon();
            var svg = InitialUtils.generateIconWithInitials(jazzIcon, "Harry");
            SvgUtil.checkSvgSecureFromXSS(svg);
        });
    }

    @Test
    void test_generateIconWithInitials_success() {
        var jazzIcon = new JazzIcon();

        var result = InitialUtils.generateIconWithInitials(jazzIcon, "Harry");
        assertThat(result)
                .isNotNull()
                .isNotBlank()
                .containsOnlyOnce("<svg ")
                .containsOnlyOnce("</svg>")
                .contains("<rect")
                .contains(
                        "<text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dominant-baseline=\"middle\" class=\"fill-white font-bold text-[30px] font-sans\">H</text>");
    }

    @Test
    void test_generateIconWithInitials_convenienceMethods_success() throws IOException {
        var jazzIcon = new JazzIcon();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            InitialUtils.generateIconWithInitialsToStream(jazzIcon, "Harry", outputStream);
            var result = outputStream.toString(StandardCharsets.UTF_8);
            assertThat(result)
                    .isNotNull()
                    .isNotBlank()
                    .containsOnlyOnce("<svg ")
                    .containsOnlyOnce("</svg>")
                    .contains("<rect")
                    .contains(
                            "<text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dominant-baseline=\"middle\" class=\"fill-white font-bold text-[30px] font-sans\">H</text>");
        }

        try (StringWriter writer = new StringWriter()) {
            InitialUtils.generateIconWithInitialsToWriter(jazzIcon, "Harry", writer);
            var result = writer.toString();
            assertThat(result)
                    .isNotNull()
                    .isNotBlank()
                    .containsOnlyOnce("<svg ")
                    .containsOnlyOnce("</svg>")
                    .contains("<rect")
                    .contains(
                            "<text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dominant-baseline=\"middle\" class=\"fill-white font-bold text-[30px] font-sans\">H</text>");
        }
    }

    @Test
    void test_generateIconWithInitials_whenClassesAndStylesNotEmpty_success() {
        var jazzIcon = new JazzIcon();

        var result = InitialUtils.generateIconWithInitials(jazzIcon, "Harry", List.of("show"), List.of("padding:0;"));
        assertThat(result)
                .isNotNull()
                .isNotBlank()
                .containsOnlyOnce("<svg ")
                .containsOnlyOnce("</svg>")
                .contains("<rect")
                .contains("<text class=\"show\" style=\"padding:0;\" x=\"50%\" y=\"50%\" text-anchor=\"middle\" "
                        + "dominant-baseline=\"middle\" "
                        + "class=\"fill-white font-bold text-[30px] font-sans\">H</text>");
    }
}
