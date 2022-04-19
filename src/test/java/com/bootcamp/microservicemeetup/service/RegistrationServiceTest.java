package com.bootcamp.microservicemeetup.service;

import com.bootcamp.microservicemeetup.exception.BusinessException;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.repository.RegistrationRepository;
import com.bootcamp.microservicemeetup.service.impl.RegistrationServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class RegistrationServiceTest {

    RegistrationService registrationService;

    PasswordEncoder passwordEncoder;

    @MockBean
    RegistrationRepository repository;

    @BeforeEach
    public void setUp() {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.registrationService = new RegistrationServiceImpl(repository, passwordEncoder);
    }

    @Test
    @DisplayName("Should save an registration")
    public void saveRegistration() {

        // cenario
        Registration registration = createValidRegistration();

        // excucao
        Mockito.when(repository.existsByRegistration(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(registration)).thenReturn(createValidRegistration());

         Registration savedRegistration = registrationService.save(registration);

        // assert
        assertThat(savedRegistration.getId()).isEqualTo(101);
        assertThat(savedRegistration.getName()).isEqualTo("Isis Oliveira");
        assertThat(savedRegistration.getDateOfRegistration()).isEqualTo("01/01/2022");
        assertThat(savedRegistration.getRegistration()).isEqualTo("001");
        assertThat(savedRegistration.getPassword()).isEqualTo("123");

    }

    @Test
    @DisplayName("Should throw business error when thy " +
            "to save a new registration with a registration duplicated")
    public void shouldNotSAveAsRegistrationDuplicated() {

        Registration registration = createValidRegistration();
        Mockito.when(repository.existsByRegistration(Mockito.any())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable( () -> registrationService.save(registration));
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Registration already created");

        Mockito.verify(repository, Mockito.never()).save(registration);
    }

    @Test
    @DisplayName("Should get a Registration by Id")
    public void getByRegistrationIdTest() {

        // cenario
        Integer id = 11;
        Registration registration = createValidRegistration();
        registration.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(registration));


        // execucao
        Optional<Registration> foundRegistration = registrationService.getRegistrationById(id);

        assertThat(foundRegistration.isPresent()).isTrue();
        assertThat(foundRegistration.get().getId()).isEqualTo(id);
        assertThat(foundRegistration.get().getName()).isEqualTo(registration.getName());
        assertThat(foundRegistration.get().getDateOfRegistration()).isEqualTo(registration.getDateOfRegistration());
        assertThat(foundRegistration.get().getRegistration()).isEqualTo(registration.getRegistration());

    }

    @Test
    @DisplayName("Should return empty when get a registration by id when doesn't exists")
    public void registrationNotFoundByIdTest() {

        Integer id = 11;
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Registration> registration  = registrationService.getRegistrationById(id);

        assertThat(registration.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should delete a student")
    public void deleteRegistrationTest() {

        Registration registration = Registration.builder().id(11).build();

        assertDoesNotThrow(() -> registrationService.delete(registration));

        Mockito.verify(repository, Mockito.times(1)).delete(registration);
    }

    @Test
    @DisplayName("Should update a registration")
    public void updateRegistration() {

        // cenario
        Integer id = 101;
        Registration updatingRegistration = Registration.builder()
                .id(101)
                .dateOfRegistration("02-02-2022")
                .registration("101")
                .password("111")
                .build();

        // execucao
        Registration updatedRegistration = createValidRegistration();
        updatedRegistration.setId(id);

        Mockito.when(repository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(updatingRegistration));
        Mockito.when(repository.save(updatingRegistration)).thenReturn(updatedRegistration);
        Registration registration = registrationService.update(updatingRegistration);

        // assert
        assertThat(registration.getId()).isEqualTo(updatedRegistration.getId());
        assertThat(registration.getName()).isEqualTo(updatedRegistration.getName());
        assertThat(registration.getDateOfRegistration()).isEqualTo(updatedRegistration.getDateOfRegistration());
        assertThat(registration.getRegistration()).isEqualTo(updatedRegistration.getRegistration());
        assertThat(registration.getPassword()).isEqualTo(updatedRegistration.getPassword());
    }

    @Test
    @DisplayName("Should filter registrations must by properties")
    public void findRegistrationTest() {

        // cenario
        Registration registration = createValidRegistration();
        PageRequest pageRequest = PageRequest.of(0,10);

        List<Registration> listRegistrations = Arrays.asList(registration);
        Page<Registration> page = new PageImpl<Registration>(Arrays.asList(registration),
                PageRequest.of(0,10), 1);

        // execucao
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Registration> result = registrationService.find(registration, pageRequest);

        // assercao
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(listRegistrations);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should get a Registration model by registration attribute")
    public void getRegistrationByRegistrationAtrb() {

        String registrationAttribute = "1234";

        Mockito.when(repository.findByRegistration(registrationAttribute))
                .thenReturn(Optional.of(Registration.builder().id(11).registration(registrationAttribute).build()));

        Optional<Registration> registration  = registrationService.getRegistrationByRegistrationAttribute(registrationAttribute);

        assertThat(registration.isPresent()).isTrue();
        assertThat(registration.get().getId()).isEqualTo(11);
        assertThat(registration.get().getRegistration()).isEqualTo(registrationAttribute);

        Mockito.verify(repository, Mockito.times(1)).findByRegistration(registrationAttribute);

    }

    private Registration createValidRegistration() {
        return Registration.builder()
                .id(101)
                .name("Isis Oliveira")
                .dateOfRegistration("01/01/2022")
                .registration("001")
                .password("123")
                .build();
    }
}
