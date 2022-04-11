package com.bootcamp.microservicemeetup.model;

import lombok.Data;

@Data
public class LoginDto {
    private String registration;
    private String password;
}