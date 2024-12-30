package com.bindord.financemanagement.repository.external;

import com.bindord.financemanagement.model.source.MailMessagesResponse;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "microsoftGraph", url = "https://graph.microsoft.com/v1.0")
public interface MicrosoftGraphClient {

  @GetMapping(value = "/me/mailFolders/{folderId}/childFolders/{childFolderId}/messages")
  @Headers("Authorization: Bearer {token}")
  MailMessagesResponse getMessages(
      @PathVariable("folderId") String folderId,
      @PathVariable("childFolderId") String childFolderId,
      @RequestParam("top") int top,
      @RequestParam("skip") int skip,
      @RequestHeader("Authorization") String token
  );
}