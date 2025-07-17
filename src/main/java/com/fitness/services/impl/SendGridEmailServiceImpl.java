package com.fitness.services.impl;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.fitness.services.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SendGridEmailServiceImpl implements EmailService {
    private final Environment env;
    @Value("${app.sendgrid.api-key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.url.base}")
    private String baseUrl;

    private void send(String to, String subject, String body) {
        try {
            Email fromEmail = new Email(from);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(fromEmail, subject, toEmail, content);

            SendGrid sg = new SendGrid(apiKey);
            Request req = new Request();
            req.setMethod(Method.POST);
            req.setEndpoint("mail/send");
            req.setBody(mail.build());
            Response resp = sg.api(req);

            if (resp.getStatusCode() >= 400) {
                System.err.println("SendGrid error: " + resp.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendRegistrationEmail(String to, String token) {
        String subject = "Confirm your email";
        String link = baseUrl + "/auth/confirm?token=" + token;
        String body = "Hello!\nPlease confirm: " + link;
        devPrint("CONFIRM", to, link);
        send(to, subject, body);
    }

    @Override
    public void sendProfileUpdateEmail(String to) {
        send(to, "Profile Updated", "Your profile has been updated.");
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Password Reset Request";
        String link = baseUrl + "/auth/reset?token=" + token;
        String body = "To reset your password, click: " + link;
        devPrint("RESET", to, link);
        send(to, subject, body);
    }


    //печатает ТОЛЬКО если профиль не prod
    private void devPrint(String type, String email, String link) {
        if (!List.of(env.getActiveProfiles()).contains("prod")) {
            System.out.printf("⇢ DEV %s link for %s: %s%n", type, email, link);
        }
    }
}
