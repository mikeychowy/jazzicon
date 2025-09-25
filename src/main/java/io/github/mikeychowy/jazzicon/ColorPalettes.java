package io.github.mikeychowy.jazzicon;

import java.util.Set;
import org.jspecify.annotations.NonNull;

public class ColorPalettes {
    public static final ColorPalettes CHART_COLORS = new ColorPalettes(Set.of(
            "#AF8626", "#00759E", "#879420", "#4B2D58", "#9F8865", "#2E476B", "#469A6C", "#AD3E4A", "#8489BD",
            "#0C7EC6", "#654D16", "#804C95", "#45999C", "#4972AC", "#CC707A", "#295B40", "#545A9C", "#785E4A",
            "#07476F", "#620004"));
    public static final ColorPalettes SECTION_1 =
            new ColorPalettes(Set.of("#AF8626", "#9F8865", "#8489BD", "#45999C", "#545A9C"));
    public static final ColorPalettes SECTION_2 =
            new ColorPalettes(Set.of("#00759E", "#2E476B", "#0C7EC6", "#4972AC", "#785E4A"));
    public static final ColorPalettes SECTION_3 =
            new ColorPalettes(Set.of("#879420", "#469A6C", "#654D16", "#CC707A", "#07476F"));
    public static final ColorPalettes SECTION_4 =
            new ColorPalettes(Set.of("#4B2D58", "#AD3E4A", "#804C95", "#295B40", "#620004"));
    public static final ColorPalettes JAZZ_ICON = new ColorPalettes(Set.of(
            "#01888C", // teal
            "#FC7500", // bright orange
            "#034F5D", // dark teal
            "#F73F01", // orangered
            "#FC1960", // magenta
            "#C7144C", // raspberry
            "#F3C100", // goldenrod
            "#1598F2", // lightning blue
            "#2465E1", // sail blue
            "#F19E02" // gold
            ));
    public static final ColorPalettes PANTONE_COLORS = new ColorPalettes(Set.of(
            "#B7C5C4", "#DBBBB2", "#EAE4DC", "#5F7C7B", "#C5D4D2", "#ADB9B3", "#C69D78", "#E0CFC5", "#9E9E92",
            "#343833", "#02132D", "#183158", "#423B62", "#B87CB4", "#F5D6DB", "#F2916F", "#FFE3D7", "#DFB5B1",
            "#824C67", "#50295B", "#29114B"));
    public static final ColorPalettes MULBERRY_WHISPER = new ColorPalettes(Set.of(
            "#734652", // Ferra
            "#BF567D", // Mulberry
            "#D98BAF", // Can can
            "#D9B0C3", // Bloom
            "#D9C5CE" // Maverick
            ));
    public static final ColorPalettes CORAL_DREAM =
            new ColorPalettes(Set.of("#FF8B6B", "#FFB580", "#FFCDAD", "#FADFCC", "#F6C5AC"));
    public static final ColorPalettes WHIMSICAL_BLOSSOM =
            new ColorPalettes(Set.of("#6A4B6C", "#A45B8D", "#D88DB5", "#E1B7C5", "#E3C9D1"));
    private final Set<String> colors;

    public ColorPalettes(@NonNull Set<String> colors) {
        this.colors = colors;
    }

    public Set<String> getColors() {
        return colors;
    }
}
