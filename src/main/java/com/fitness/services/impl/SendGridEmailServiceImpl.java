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

@Service
@RequiredArgsConstructor
public class SendGridEmailServiceImpl implements EmailService {
    @Value("${app.sendgrid.api-key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.url.base}")
    private String baseUrl;

    private void send(String to, String subject, String body) {
        try {
            Email fromEmail = new Email(from);
            Email toEmail   = new Email(to);
            Content content = new Content("text/plain", body);
            Mail mail       = new Mail(fromEmail, subject, toEmail, content);

            SendGrid sg     = new SendGrid(apiKey);
            Request req     = new Request();
            req.setMethod(Method.POST);
            req.setEndpoint("mail/send");
            req.setBody(mail.build());
            Response resp   = sg.api(req);

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
        String link    = baseUrl + "/auth/confirm?token=" + token;
        String body    = "Hello!\nPlease confirm: " + link;
        send(to, subject, body);
    }

    @Override
    public void sendProfileUpdateEmail(String to) {
        send(to, "Profile Updated", "Your profile has been updated.");
    }
}
