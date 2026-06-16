package com.backend_desigeo.desigeo_notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Async
    public void sendNotificationEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Notification email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send notification email to {}: {}", toEmail, e.getMessage());
        }
    }

    public String buildReportCreatedHtml(String category, String address, String reportId) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #001A33;">Nuevo Reporte Ciudadano - DESIGEO</h2>
                    <p>Se ha creado un nuevo reporte que requiere atención.</p>
                    <table style="width:100%%; border-collapse: collapse; margin: 20px 0;">
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Categoría</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Dirección</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">ID Reporte</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                    </table>
                    <p style="color: #666; font-size: 12px;">Ingresa al panel municipal para gestionar este reporte.</p>
                </div>
                """.formatted(category, address, reportId);
    }

    public String buildStatusChangedHtml(String reportId, String previousStatus, String newStatus) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #001A33;">Tu reporte cambió de estado - DESIGEO</h2>
                    <p>El estado de tu reporte ha sido actualizado.</p>
                    <table style="width:100%%; border-collapse: collapse; margin: 20px 0;">
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">ID Reporte</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Estado anterior</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Nuevo estado</td>
                            <td style="padding: 8px; border: 1px solid #ddd; color: #0050A5; font-weight: bold;">%s</td>
                        </tr>
                    </table>
                    <p style="color: #666; font-size: 12px;">Ingresa a DESIGEO para ver más detalles de tu reporte.</p>
                </div>
                """.formatted(reportId, previousStatus, newStatus);
    }

    public String buildReopenRequestedHtml(String reportId, String reason) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #001A33;">Solicitud de Reapertura de Reporte - DESIGEO</h2>
                    <p>Un ciudadano ha solicitado reabrir un reporte.</p>
                    <table style="width:100%%; border-collapse: collapse; margin: 20px 0;">
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">ID Reporte</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Motivo</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                    </table>
                    <p style="color: #666; font-size: 12px;">Ingresa al panel municipal para revisar y gestionar esta solicitud.</p>
                </div>
                """.formatted(reportId, reason);
    }
}
