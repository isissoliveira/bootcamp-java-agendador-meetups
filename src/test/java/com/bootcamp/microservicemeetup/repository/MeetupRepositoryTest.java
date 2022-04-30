package com.bootcamp.microservicemeetup.repository;

import com.bootcamp.microservicemeetup.model.entity.Meetup;
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
public class MeetupRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    MeetupRepository repository;

    @Test
    @DisplayName("Should return true when exists a meetup already created.")
    public void returnTrueWhenMeetupExists() {
        Meetup new_meetup = createNewMeetup();
        Meetup created_meetup = entityManager.persist(new_meetup);

        boolean exists = repository.existsById(created_meetup.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when meetup doesn't exists")
    public void returnFalseWhenMeetupDoesntExists() {
        boolean exists = repository.existsById(99);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should get a meetup by id")
    public void findByIdTest() {
        Meetup new_meetup = createNewMeetup();
        Meetup created_meetup = entityManager.persist(new_meetup);

        Optional<Meetup> foundMeetup = repository
                .findById(created_meetup.getId());

        assertThat(foundMeetup.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should get a meetup by event")
    public void findByEventTest() {
        Meetup new_meetup = createNewMeetup();
        Meetup created_meetup = entityManager.persist(new_meetup);

        Optional<Meetup> foundMeetup = repository
                .findByEvent(created_meetup.getEvent());

        assertThat(foundMeetup.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should save a meetup")
    public void saveRegistrationTest() {
        Meetup new_meetup = createNewMeetup();
        Meetup created_meetup = repository.save(new_meetup);

        assertThat(created_meetup.getId()).isNotNull();
    }


    @Test
    @DisplayName("Should delete and registration from the base")
    public void deleteRegistation() {
        Meetup new_meetup = createNewMeetup();
        Meetup created_meetup = entityManager.persist(new_meetup);

        Meetup foundMeetup = entityManager
                .find(Meetup.class, created_meetup.getId());
        repository.delete(foundMeetup);

        Meetup deletedMeetup = entityManager
                .find(Meetup.class, created_meetup.getId());

        assertThat(deletedMeetup).isNull();
    }

    private Meetup createNewMeetup() {
        return Meetup.builder()
                .event("Evento teste")
                .meetupDate("01/01/2022")
                .build();
    }
}
