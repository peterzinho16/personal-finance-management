package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

  boolean existsByUsername(String username);
}
