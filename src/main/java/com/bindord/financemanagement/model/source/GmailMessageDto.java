package com.bindord.financemanagement.model.source;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GmailMessageDto {
  private String id;
  private String threadId;
  private String createdDateTime;
  private String subject;
  private String bodyPreview;
  private String bodyHTML;
  private String webLink;
  private String from;
}