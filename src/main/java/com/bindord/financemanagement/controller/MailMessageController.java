package com.bindord.financemanagement.controller;

import com.bindord.financemanagement.model.source.MailMessage;
import com.bindord.financemanagement.repository.MailMessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RestController
@RequestMapping("/eureka/finance-app/mail-message")
@AllArgsConstructor
public class MailMessageController {


  private final MailMessageRepository mailMessageRepository;

  @GetMapping("")
  public List<MailMessage> findAll() {
    return mailMessageRepository.findAll();
  }
}
