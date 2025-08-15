package com.example.iwemailsender.email.repository;

import com.example.iwemailsender.email.domain.EmailExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailExecutionRepository extends JpaRepository<EmailExecution, UUID> {

    List<EmailExecution> findByEmailJobIdOrderByExecutedAtDesc(UUID emailJobId);


}
