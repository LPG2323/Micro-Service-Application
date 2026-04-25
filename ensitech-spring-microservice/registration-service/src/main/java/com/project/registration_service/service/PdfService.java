package com.project.registration_service.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.project.registration_service.domain.Registration;
import com.project.registration_service.dto.RegDTO;
import com.project.registration_service.feign.StudentRestClient;
import com.project.registration_service.model.Student;
import jakarta.ws.rs.NotFoundException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PdfService {
    private final TemplateEngine templateEngine;
    private final StudentRestClient studentRestClient;

    public PdfService(TemplateEngine templateEngine, StudentRestClient studentRestClient) {
        this.templateEngine = templateEngine;
        this.studentRestClient = studentRestClient;
    }

    private String generateQRCodeBase64(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1); // marges minimales
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = (bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                    image.setRGB(x, y, color);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }

    public byte[] generateRegistrationPdf(Registration registration){
        Student student = studentRestClient.getStudentByMatricule(registration.getMatricule());
        if(student == null)
            throw new NotFoundException("Aucun n'etudiant à ce matricule");
//        String qrData = "Matricule: " + student.getMatricule();
        String qrData = "http://localhost:8888/api/registrations/"+registration.getId()+"/original-pdf";
//        String qrData = "http://${APP_HOST}:${APP_PORT}/api/registrations/"+registration.getId()+"/original-pdf";

        String qrBase64 = generateQRCodeBase64(qrData, 150, 150);

        String logoBase64 = getLogoBase64();

        Context context = new Context();
        context.setVariable("registration", registration);
        context.setVariable("student", student); // si tu veux afficher des infos student
        context.setVariable("today", LocalDate.now());
        context.setVariable("qrCodeBase64", qrBase64);

        // Injection of the image
        context.setVariable("logoPath", logoBase64);

        String htmlContent = templateEngine.process("registration-pdf", context);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    public byte[] generateRegistrationPdfWithoutQr(Registration registration) {
        Student student = studentRestClient.getStudentByMatricule(registration.getMatricule());
        if (student == null) throw new NotFoundException("Aucun étudiant avec ce matricule");
        String logoBase64 = getLogoBase64();

        Context context = new Context();
        context.setVariable("registration", registration);
        context.setVariable("student", student);
        context.setVariable("today", LocalDate.now());

        // Injection of the image
        context.setVariable("logoPath", logoBase64);

        String htmlContent = templateEngine.process("registration-pdf-original", context);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private String getLogoBase64() {
        try {
            ClassPathResource resource = new ClassPathResource("static/logo_ensitech_remove_bg.png");
            byte[] imageBytes = resource.getInputStream().readAllBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            // Le préfixe "data:image/png;base64," est crucial pour le Data URI
            return "data:image/png;base64," + base64Image;
        } catch (IOException e) {
            System.err.println("Erreur lors de la conversion du logo en Base64: " + e.getMessage());
            return "";
        }
    }

}
