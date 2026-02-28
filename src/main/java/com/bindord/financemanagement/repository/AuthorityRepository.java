package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.auth.Authority;
import com.bindord.financemanagement.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
  List<Authority> findByUser(User user);
}