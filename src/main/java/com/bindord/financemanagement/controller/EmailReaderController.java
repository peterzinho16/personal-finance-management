package com.bindord.financemanagement.controller;

import com.bindord.financemanagement.config.OAuth2Authenticator;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Slf4j
@RequestMapping("/email-management")
@RestController
@AllArgsConstructor
public class EmailReaderController {

    public static final String PERSONAL_EMAIL = "peterpaul.182@live.com";
    public static final String OUTLOOK_HOST = "outlook.office365.com";
    public static final String MAIL_STORE_PROTOCOL = "imap";


    @GetMapping
    public void readMail() throws Exception {

        final String oAuthToken = OAuth2Authenticator.getAccessToken(); // Replace with your actual token
        System.out.println(oAuthToken);
        String subFolderName = "Notificationes Compras"; // Name of your sub-folder under INBOX

        try {
            Properties props = getProperties();

            Session session = Session.getInstance(props);
            // Create the IMAP store object and connect to the server
            Store store = session.getStore(MAIL_STORE_PROTOCOL);
            store.connect(OUTLOOK_HOST, PERSONAL_EMAIL, oAuthToken);

            // Get the inbox folder
            Folder inboxFolder = store.getFolder("INBOX");
            inboxFolder.open(Folder.READ_ONLY);

            // Get the sub-folder under INBOX
            Folder subFolder = inboxFolder.getFolder(subFolderName);

            // Check if the sub-folder exists and open it
            if (subFolder.exists()) {
                subFolder.open(Folder.READ_ONLY);
                // Define the date after which emails should be fetched
                String dateString = "2024-12-19"; // Example: Get emails after December 1, 2024
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date targetDate = sdf.parse(dateString); // Convert the string to Date object
                // Create a search term to filter emails after the specified date
                SearchTerm searchTerm = new ReceivedDateTerm(ComparisonTerm.GT, targetDate);

                // Fetch messages that match the search term (emails after the target date)
                Message[] messages = subFolder.search(searchTerm);
                System.out.println("Total messages after " + targetDate + ": " + messages.length);

                for (Message message : messages) {
                    System.out.println("---------------------------------");
                    System.out.println("Subject: " + message.getSubject());
                    System.out.println("From: " + message.getFrom()[0]);
                    System.out.println("Sent Date: " + message.getSentDate());
                    System.out.println("Content: " + message.getContent());
                }

                // Close the sub-folder
                subFolder.close(false);
            } else {
                System.out.println("Sub-folder not found!");
            }

            // Close the inbox folder and store
            inboxFolder.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getCause());
        }
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("mail.store.protocol", MAIL_STORE_PROTOCOL);
        props.put("mail.imap.host", OUTLOOK_HOST);
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl.enable", "true"); // Recommended for security
        props.put("mail.imap.starttls.enable", "true");
        props.put("mail.imap.auth", "true");
        props.put("mail.imap.auth.mechanisms", "XOAUTH2");
        props.put("mail.imap.user", PERSONAL_EMAIL);
        props.put("mail.debug", "true");
        props.put("mail.debug.auth", "true");
        return props;
    }

}