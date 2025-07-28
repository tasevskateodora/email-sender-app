package com.example.iwemailsender.email.dto;

import lombok.Data;

@Data
public class CreateEmailTemplateRequestDto {

    private String name;
    private String subject;
    private String body;

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
}
