package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.source.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter, String> {
}
