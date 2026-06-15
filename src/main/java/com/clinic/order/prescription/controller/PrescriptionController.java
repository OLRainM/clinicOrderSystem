package com.clinic.order.prescription.controller;

import com.clinic.order.common.security.SecurityUtils;
import com.clinic.order.prescription.service.PdfRenderService;
import com.clinic.order.prescription.service.PrescriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final PdfRenderService pdfRenderService;

    public PrescriptionController(PrescriptionService prescriptionService, PdfRenderService pdfRenderService) {
        this.prescriptionService = prescriptionService;
        this.pdfRenderService = pdfRenderService;
    }

    @GetMapping("/{prescriptionId}/download")
    public void download(@PathVariable Long prescriptionId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long userId = SecurityUtils.currentUserId(request);
        Map<String, Object> data = prescriptionService.getPrescriptionData(prescriptionId, userId).orElse(null);
        if (data == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权访问");
            return;
        }
        String fileName = URLEncoder.encode("prescription_" + prescriptionId + ".pdf", StandardCharsets.UTF_8);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        pdfRenderService.renderPrescriptionPdf(data, response.getOutputStream());
    }
}
