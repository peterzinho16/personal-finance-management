package com.bindord.financemanagement.model.source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_message")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class MailMessage {

  @Id
  @Column(nullable = false)
  private String id;

  @Column(nullable = false)
  private LocalDateTime createdDateTime;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String subject;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String bodyPreview;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String bodyHtml;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String bodyTextContent;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String webLink;

  @Column(nullable = false)
  private String fromEmail;

  @Column(nullable = false, unique = true)
  private String referenceId;
}
