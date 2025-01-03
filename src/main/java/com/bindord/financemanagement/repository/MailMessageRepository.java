package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.source.MailMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailMessageRepository extends JpaRepository<MailMessage, String> {
}
