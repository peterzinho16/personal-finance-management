package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.ParameterUserId;
import com.bindord.financemanagement.model.source.ParameterUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParameterUserRepository extends JpaRepository<ParameterUser, ParameterUserId> {

}
