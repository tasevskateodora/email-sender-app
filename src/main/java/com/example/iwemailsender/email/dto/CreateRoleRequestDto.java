package com.example.iwemailsender.email.dto;

import lombok.Data;

@Data
public class CreateRoleRequestDto {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
