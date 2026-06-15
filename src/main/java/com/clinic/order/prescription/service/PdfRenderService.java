package com.clinic.order.prescription.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.OutputStream;
import java.io.StringWriter;
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
        ClassPathResource font = new ClassPathResource("fonts/simsun.ttf");
        if (font.exists()) {
            renderer.getFontResolver().addFont(font.getFile().getAbsolutePath(), "Identity-H", true);
        }
        renderer.setDocumentFromString(writer.toString());
        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.flush();
    }
}
