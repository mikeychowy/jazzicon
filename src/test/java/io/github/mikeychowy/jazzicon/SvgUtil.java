package io.github.mikeychowy.jazzicon;

import com.github.bgalek.security.svg.SvgSecurityValidator;
import com.github.bgalek.security.svg.ValidationResult;
import com.github.javafaker.Faker;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;

public final class SvgUtil {

    private static final String EXAMPLE_HTML_TEMPLATE =
            """
            <!DOCTYPE html>
            <html class="base" lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta
                  content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=yes"
                  name="viewport"
              />
              <title>JazzIcon Examples</title>
              <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
            </head>
            <body>
            <div class="rounded-full size-24 hidden"></div>
            <div class="grid grid-cols-4 gap-x-0 gap-y-4 justify-items-center-safe justify-center-safe mt-5">
              %s
            </div>
            </body>
            </html>
            """;

    private static final Logger log = LoggerFactory.getLogger(SvgUtil.class);

    private SvgUtil() {}

    public static boolean isValidSvg(String svg) {
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
            SVGDocument document = factory.createSVGDocument(null, new StringReader(svg));
            return document != null;
        } catch (Exception e) {
            log.error("SVG error", e);
            return false;
        }
    }

    public static void checkSvgSecureFromXSS(String svg) {
        SvgSecurityValidator svgSecurityValidator = SvgSecurityValidator.builder()
                .withSyntaxValidation()
                .withAdditionalAttributes(List.of("dominant-baseline"))
                .build();
        ValidationResult validation = svgSecurityValidator.validate(svg);
        if (validation.hasViolations()) {
            throw new IllegalArgumentException("SVG violations: " + validation.getOffendingElements());
        }
    }

    public static void main(String[] args) throws IOException {
        JazzIcon jazzIcon = new JazzIcon();
        jazzIcon.addSvgClasses("rounded-full", "size-24");
        Faker faker = new Faker();

        Map<String, ColorPalettes> colorPalettesMap = new HashMap<>();
        colorPalettesMap.put("default", ColorPalettes.JAZZ_ICON);
        colorPalettesMap.put("chart", ColorPalettes.CHART_COLORS);
        colorPalettesMap.put("pantone", ColorPalettes.PANTONE_COLORS);
        colorPalettesMap.put("coral", ColorPalettes.CORAL_DREAM);
        colorPalettesMap.put("mulberry", ColorPalettes.MULBERRY_WHISPER);
        colorPalettesMap.put("blossom", ColorPalettes.WHIMSICAL_BLOSSOM);
        colorPalettesMap.put("latte", ColorPalettes.CATPUCCIN_LATTE);
        colorPalettesMap.put("frappe", ColorPalettes.CATPUCCIN_FRAPPE);
        colorPalettesMap.put("macchiato", ColorPalettes.CATPUCCIN_MACCHIATO);
        colorPalettesMap.put("mocha", ColorPalettes.CATPUCCIN_MOCHA);
        colorPalettesMap.put("tailwind", ColorPalettes.TAILWIND);

        for (Map.Entry<String, ColorPalettes> entry : colorPalettesMap.entrySet()) {
            jazzIcon.setBaseColors(entry.getValue());
            List<String> nameList = new ArrayList<>(16);
            for (int i = 0; i < 16; i++) {
                nameList.add(faker.name().fullName());
            }
            String svgHtml = nameList.parallelStream()
                    .map(jazzIcon::generateIcon)
                    .map(s -> "<div>" + s + "</div>")
                    .collect(Collectors.joining("\n"));

            String result = String.format(EXAMPLE_HTML_TEMPLATE, svgHtml);

            String fileName = "examples/" + entry.getKey() + ".html";

            // Write to /examples/${key}.html
            File output = new File(fileName);
            FileUtils.writeStringToFile(output, result, StandardCharsets.UTF_8);
            log.info("HTML file written to: {}", output.getAbsolutePath());
        }
    }
}
