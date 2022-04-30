package com.bootcamp.microservicemeetup.service.impl;

import com.bootcamp.microservicemeetup.exception.BusinessException;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.repository.RegistrationRepository;
import com.bootcamp.microservicemeetup.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    RegistrationRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegistrationServiceImpl(RegistrationRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Registration save(Registration registration) {
        if (repository.existsByRegistration(registration.getRegistration())) {
            throw new BusinessException("Registration already created");
        }
        registration.setPassword(passwordEncoder.encode(registration.getPassword()));
        return repository.save(registration);
    }

    @Override
    public Optional<Registration> getRegistrationById(Integer id) {
        return this.repository.findById(id);
    }

    // inserir mais uma validacao no delete();
    @Override
    public void delete(Registration registration) {
        if (registration == null || registration.getId() == null) {
            throw new IllegalArgumentException("Registration id cannot be null");
        }
        this.repository.delete(registration);
    }

    // inserir mais uma validacao no save();
    @Override
    public Registration update(Registration registration) {
        if (registration == null || registration.getId() == null) {
            throw new IllegalArgumentException("Registration id cannot be null");
        }
        Optional<Registration> original_registration = repository.findById(registration.getId());
        if(!original_registration.isPresent()){
            throw new IllegalArgumentException("Registration not found");
        }
        registration.setRegistration( original_registration.get().getRegistration());
        registration.setPassword( passwordEncoder.encode(registration.getPassword()));
        return this.repository.save(registration);
    }

    @Override
    public Page<Registration> find(Registration filter, Pageable pageRequest) {
        Example<Registration> example = Example.of(filter,
                ExampleMatcher
                .matching()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));

        return repository.findAll(example, pageRequest);
    }

    @Override
    public Optional<Registration> getRegistrationByRegistrationAttribute(String registrationAttribute) {
        return repository.findByRegistration(registrationAttribute);
    }

}
