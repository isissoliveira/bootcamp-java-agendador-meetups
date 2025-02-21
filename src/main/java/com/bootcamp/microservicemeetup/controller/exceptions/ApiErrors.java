package com.bootcamp.microservicemeetup.controller.exceptions;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.bootcamp.microservicemeetup.exception.BusinessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {

    private final List<String> errors;

    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors()
                .forEach(error -> this.errors.add(error.getDefaultMessage()));
    }

    public ApiErrors(BusinessException e) {
        this.errors = List.of(e.getMessage());
    }

    public ApiErrors(ResponseStatusException e) {
        this.errors = List.of(e.getReason());
    }

    public ApiErrors(IllegalArgumentException e) {
        this.errors = List.of(e.getMessage());
    }

    public List<String> getErrors() {
        return errors;
    }

}
