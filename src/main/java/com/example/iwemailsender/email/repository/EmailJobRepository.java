package com.example.iwemailsender.email.repository;

import com.example.iwemailsender.email.domain.EmailJob;
import com.example.iwemailsender.email.dto.EmailJobDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailJobRepository extends JpaRepository<EmailJob, UUID> {


   @Query("""
    SELECT ej FROM EmailJob ej
    WHERE ej.enabled = true
      AND ej.nextRunTime <= :currentTime
      AND (ej.startDate IS NULL OR ej.startDate <= :currentTime)
      AND (ej.endDate IS NULL OR ej.endDate >= :currentTime)
""")
    List<EmailJob> findJobsToExecute(@Param("currentTime") LocalDateTime currentTime);
    List<EmailJob> findByEnabledTrue();
    List<EmailJob> findByCreatedById(UUID userId);

}

