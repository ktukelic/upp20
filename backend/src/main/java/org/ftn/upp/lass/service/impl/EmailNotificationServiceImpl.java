package org.ftn.upp.lass.service.impl;

import lombok.RequiredArgsConstructor;
import org.ftn.upp.lass.model.User;
import org.ftn.upp.lass.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

/**
 * E-mail notification delivery service implementation.
 */
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements NotificationService {

    @Value("${spring.mail.username}")
    private String email;

    @Value("${templates.html.verification}")
    private String verificationTemplateName;

    private final JavaMailSender mailSender;
    private final ITemplateEngine springTemplateEngine;

    /**
     * Sends a verification e-mail to the registered user.
     * In case an messaging error on the SMTP server occurs, a {@link MessagingException} is thrown.
     *
     * @param recipientUser {@link User} instance as recipient
     * @throws MessagingException Exception thrown in case an error on the SMTP server occurs
     */
    @Async
    public void sendVerificationEmail(User recipientUser, String processInstanceId) throws MessagingException {
        this.sendEmail(recipientUser.getEmail(), "LASS Verification - Please confirm your account", this.generateVerificationMail(recipientUser, processInstanceId));
    }

    /**
     * Sends an email with given content and attachments to the recipient.
     *
     * In case a messaging error on the SMTP server occurs, a {@link MessagingException} is thrown.
     * @param recipientEmailAddress Recipient's email address
     * @param subject               Email subject
     * @param text                  Email content
     * @param fileName              Name of the attachment file
     * @param pdf                   PDF file
     * @param html                  HTML file
     * @throws MessagingException Exception thrown in case an error on the SMTP server occurs
     */
    @Async
    void sendEmailWithAttachments(String recipientEmailAddress, String subject, String text,
                                  String fileName, byte[] pdf, String html) throws MessagingException {

        final String currentDate = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        final String newFileName = String.format("%s_%s", fileName, currentDate);

        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setContent(text, TEXT_HTML_VALUE);

        MimeBodyPart pdfAttachmentBodyPart = new MimeBodyPart();
        pdfAttachmentBodyPart.setContent(pdf, APPLICATION_PDF_VALUE);
        pdfAttachmentBodyPart.setFileName(newFileName + ".pdf");

        MimeBodyPart htmlAttachmentBodyPart = new MimeBodyPart();
        htmlAttachmentBodyPart.setContent(html, TEXT_HTML_VALUE);
        htmlAttachmentBodyPart.setFileName(newFileName + ".html");

        Multipart multipartContent = new MimeMultipart();
        multipartContent.addBodyPart(textBodyPart);
        multipartContent.addBodyPart(pdfAttachmentBodyPart);
        multipartContent.addBodyPart(htmlAttachmentBodyPart);

        this.sendEmail(recipientEmailAddress, subject, multipartContent);
    }

    /**
     * Sends an email.
     * In case an messaging error on the SMTP server occurs, a {@link MessagingException} is thrown.
     *
     * @param recipientEmailAddress Recipient's email address
     * @param subject               Email subject
     * @param content               Email content
     * @throws MessagingException   Exception thrown in case an error on the SMTP server occurs
     */
    @Async
    void sendEmail(String recipientEmailAddress, String subject, String content) throws MessagingException {
        final MimeMessage mailMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, StandardCharsets.UTF_8.name());

        messageHelper.setFrom(this.email);
        messageHelper.setTo(recipientEmailAddress);
        messageHelper.setSubject(subject);
        mailMessage.setContent(content, TEXT_HTML_VALUE);

        this.mailSender.send(mailMessage);
    }

    /**
     * Sends an email with multipart content.
     * In case an messaging error on the SMTP server occurs, a {@link MessagingException} is thrown.
     *
     * @param recipientEmailAddress Recipient's email address
     * @param subject               Email subject
     * @param content               Email content
     * @throws MessagingException   Exception thrown in case an error on the SMTP server occurs
     */
    @Async
    void sendEmail(String recipientEmailAddress, String subject, Multipart content) throws MessagingException {
        final MimeMessage mailMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, StandardCharsets.UTF_8.name());

        messageHelper.setFrom(this.email);
        messageHelper.setTo(recipientEmailAddress);
        messageHelper.setSubject(subject);
        mailMessage.setContent(content);

        this.mailSender.send(mailMessage);
    }

    /**
     * Generates verification e-mail content using a template, based on the user's info.
     *
     * @param recipientUser {@link User} instance as recipient, used to fill template data
     * @param processInstanceId unique identifier of the process instance
     * @return Verification email HTML content
     */
    private String generateVerificationMail(User recipientUser, String processInstanceId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("recipient", recipientUser.getFirstName());
        variables.put("code", recipientUser.getVerificationCode().getValue());
        variables.put("pid", processInstanceId);

        return this.springTemplateEngine
                .process(this.verificationTemplateName, new Context(Locale.getDefault(), variables));
    }
}
