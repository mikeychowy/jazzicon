package io.github.mikeychowy.jazzicon;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NameUtilsTest {

    @Test
    void test_getInitials_returningCorrectInitials() {
        var result = NameUtils.getInitials("Orang Bandung");
        assertThat(result).isEqualTo("OB");
        result = NameUtils.getInitials("Henry Sibutar-butar");
        assertThat(result).isEqualTo("HS");
        result = NameUtils.getInitials("Ini_nama_pakai_underscore");
        assertThat(result).isEqualTo("I");
        result = NameUtils.getInitials("");
        assertThat(result).isEqualTo("?");
    }
}
