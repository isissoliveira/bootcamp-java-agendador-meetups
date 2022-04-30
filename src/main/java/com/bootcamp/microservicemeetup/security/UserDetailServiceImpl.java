package com.bootcamp.microservicemeetup.security;

import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<Registration> registration = registrationRepository.findByRegistration(login);
        if(!registration.isPresent()){
            throw new UsernameNotFoundException("Usuario não encontrado");
        }
        return new User( registration.get().getRegistration(), registration.get().getPassword(), true, true, true, true, registration.get().getAuthorities());
    }
}
