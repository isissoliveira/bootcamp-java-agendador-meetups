package com.bootcamp.microservicemeetup.controller.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String registration;
    private String password;
}