package com.bootcamp.microservicemeetup.repository;

import com.bootcamp.microservicemeetup.model.entity.Registration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RegistrationRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    RegistrationRepository repository;

    @Test
    @DisplayName("Should return true when exists an registration already created.")
    public void returnTrueWhenRegistrationExists() {

        String registration = "teste";

        Registration new_Registration = createNewRegistration(registration);
        entityManager.persist(new_Registration);

        boolean exists = repository.existsByRegistration(registration);

        assertThat(exists).isTrue();
    }


    @Test
    @DisplayName("Should return false when doesn't exists an registration_attribute with a registration already created.")
    public void returnFalseWhenRegistrationAttributeDoesntExists() {

        String registration = "teste";

        boolean exists = repository.existsByRegistration(registration);

        assertThat(exists).isFalse();

    }

    @Test
    @DisplayName("Should get an registration by id")
    public void findByIdTest() {

        Registration registration_attribute = createNewRegistration("teste");
        entityManager.persist(registration_attribute);

        Optional<Registration> foundRegistration = repository
                .findById(registration_attribute.getId());

        assertThat(foundRegistration.isPresent()).isTrue();

    }

    @Test
    @DisplayName("Should save an registration")
    public void saveRegistrationTest() {

        Registration registration_attribute = createNewRegistration("teste");

        Registration savedRegistration = repository.save(registration_attribute);

        assertThat(savedRegistration.getId()).isNotNull();

    }

    @Test
    @DisplayName("Should delete and registration from the base")
    public void deleteRegistation() {

        Registration registration_attribute = createNewRegistration("teste");
        entityManager.persist(registration_attribute);

        Registration foundRegistration = entityManager
                .find(Registration.class, registration_attribute.getId());
        repository.delete(foundRegistration);

        Registration deleteRegistration = entityManager
                .find(Registration.class, registration_attribute.getId());

        assertThat(deleteRegistration).isNull();
    }

    private Registration createNewRegistration(String registration) {
        return Registration.builder()
                .name("Isis Oliveira")
                .dateOfRegistration("01/01/2022")
                .registration(registration)
                .password("123")
                .build();
    }
}
