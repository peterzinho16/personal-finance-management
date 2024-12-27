package com.bindord.financemanagement.controller;

import com.bindord.financemanagement.model.MailMessagesResponse;
import com.bindord.financemanagement.model.MessageDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Controller
@RestController
@RequestMapping("/eureka/finance-app/automatic-ingest")
@AllArgsConstructor
public class IngestToDatabaseController {

    @GetMapping
    public MailMessagesResponse ingestToDatabaseWithMailMessages() throws IOException {
        Resource resource = new ClassPathResource("mail-responses/response_last_10_mails.json");
        InputStream inputStream = resource.getInputStream();
        var objMapper = new ObjectMapper();
        objMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        MailMessagesResponse mailMessagesResponse = objMapper.readValue(inputStream, MailMessagesResponse.class);
        List<MessageDto> messages = mailMessagesResponse.getValue();
        for(MessageDto msg : messages) {
            System.out.println(msg.getBodyPreview());
            System.out.println("***************************************************************************************");
            System.out.println("***************************************************************************************");
        }
        return mailMessagesResponse;
    }
}
