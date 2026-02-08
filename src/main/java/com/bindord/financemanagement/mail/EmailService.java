package com.bindord.financemanagement.mail;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final EmailClient emailClient;

  @Value("${azure.communication.email.sender}")
  private String sender;
  public static final String LOCAL_DOMAIN_ACTIVATION = "http://localhost:8080/activate?token=";

  public void sendActivationEmail(String to, String activationLink) {

    String subject = "Activate your account";

    String htmlBody = """
            <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Welcome!</h2>
                    <p>Please activate your account by clicking the link below:</p>
                    <p>
                        <a href="%s"
                           style="padding:10px 15px;
                                  background:#0d6efd;
                                  color:#fff;
                                  text-decoration:none;
                                  border-radius:4px;">
                           Activate account
                        </a>
                    </p>
                    <p>This link expires in 24 hours.</p>
                </body>
            </html>
        """.formatted(LOCAL_DOMAIN_ACTIVATION + activationLink);


    EmailMessage message = new EmailMessage()
        .setSenderAddress(sender)
        .setSubject(subject)
        .setBodyHtml(htmlBody)
        .setToRecipients(to);

    try {
      EmailSendResult result =
          emailClient.beginSend(message)
              .getFinalResult();

      log.info("Activation email sent to {} (messageId={})",
          to, result.getId());

    } catch (Exception ex) {
      log.error("Failed to send activation email to {}", to, ex);
      throw ex;
    }
  }
}