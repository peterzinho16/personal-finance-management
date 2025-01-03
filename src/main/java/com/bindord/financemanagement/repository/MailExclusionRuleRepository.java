package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.source.MailExclusionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailExclusionRuleRepository extends JpaRepository<MailExclusionRule, Integer> {
}
