package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.source.MailMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailMessageRepository extends JpaRepository<MailMessage, String> {
  MailMessage findByReferenceId(String referenceId);
}
