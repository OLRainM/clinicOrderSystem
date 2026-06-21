package com.clinic.order.prescription.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class PdfRenderService {
    private final Configuration configuration;

    public PdfRenderService(Configuration configuration) {
        this.configuration = configuration;
    }

    public void renderPrescriptionPdf(Map<String, Object> data, OutputStream outputStream) throws Exception {
        Template template = configuration.getTemplate("prescription_template.ftl");
        StringWriter writer = new StringWriter();
        template.process(data, writer);
        ITextRenderer renderer = new ITextRenderer();
        for (String fontPath : candidateFonts()) {
            if (!fontPath.isBlank() && Files.isRegularFile(Path.of(fontPath))) {
                renderer.getFontResolver().addFont(fontPath, "Identity-H", true);
                break;
            }
        }
        renderer.setDocumentFromString(writer.toString());
        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.flush();
    }

    private List<String> candidateFonts() throws Exception {
        ClassPathResource bundled = new ClassPathResource("fonts/simsun.ttf");
        return List.of(
                bundled.exists() ? bundled.getFile().getAbsolutePath() : "",
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/msyh.ttc",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
        );
    }

}
