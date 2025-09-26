package io.github.mikeychowy.jazzicon;

import com.github.bgalek.security.svg.SvgSecurityValidator;
import com.github.bgalek.security.svg.ValidationResult;
import java.io.StringReader;
import java.util.List;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;

public final class SvgUtil {

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
}
