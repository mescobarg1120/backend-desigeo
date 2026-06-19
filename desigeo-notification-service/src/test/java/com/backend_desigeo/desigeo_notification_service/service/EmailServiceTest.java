package com.backend_desigeo.desigeo_notification_service.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CP-NOTIF-001: Enviar email de confirmacion exitosamente
 * CP-NOTIF-002: Simular fallo SMTP → 3 reintentos + logs
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage())
                .thenReturn(new JavaMailSenderImpl().createMimeMessage());
        emailService = new EmailService(mailSender, "noreply@desigeo.cl");
    }

    // CP-NOTIF-001
    @Test
    void sendNotificationEmail_success_emailIsSentOnce() {
        emailService.sendNotificationEmail(
                "usuario@test.com",
                "Confirmacion DESIGEO",
                "<p>Tu reporte fue recibido correctamente.</p>"
        );

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // CP-NOTIF-002
    @Test
    void sendNotificationEmail_onSmtpFailure_retriesThreeTimes() {
        doThrow(new MailSendException("SMTP connection refused"))
                .when(mailSender).send(any(MimeMessage.class));

        emailService.sendNotificationEmail(
                "usuario@test.com",
                "Confirmacion DESIGEO",
                "<p>Tu reporte fue recibido correctamente.</p>"
        );

        verify(mailSender, times(3)).send(any(MimeMessage.class));
    }
}
