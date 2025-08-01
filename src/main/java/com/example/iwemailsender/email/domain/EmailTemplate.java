package com.example.iwemailsender.email.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "email_templates")
public class EmailTemplate extends BaseEntity{


    private String name;
    private String subject;
    private String body;


   /* @OneToMany(mappedBy = "emailTemplate", fetch = FetchType.LAZY)
    //@JsonIgnore
    private List<EmailJob> emailJobs = new ArrayList<>();*/

    public EmailTemplate(String name, String subject, String body ){
        this.name = name;
        this.subject = subject;
        this.body = body;

    }

    public EmailTemplate() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

 /*   public List<EmailJob> getEmailJobs() {
        return emailJobs;
    }

    public void setEmailJobs(List<EmailJob> emailJobs) {
        this.emailJobs = emailJobs;
    }*/
}