package com.bootcamp.microservicemeetup.service;

import com.bootcamp.microservicemeetup.exception.BusinessException;
import com.bootcamp.microservicemeetup.model.entity.Meetup;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.repository.MeetupRepository;
import com.bootcamp.microservicemeetup.repository.RegistrationRepository;
import com.bootcamp.microservicemeetup.service.impl.MeetupServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class MeetupServiceTest {

    MeetupService meetupService;

    @Mock
    Page<Meetup> pageMeetup;
    @Mock
    Pageable pageRequest;

    @MockBean
    MeetupRepository meetupRepository;

    @MockBean
    RegistrationRepository registrationRepository;

    @BeforeEach
    public void setUp() {
        this.meetupService = new MeetupServiceImpl(meetupRepository);
    }

    @Test
    @DisplayName("Should save a meetup")
    public void save() {
        // cenario
        Meetup meetup = createMeetup();

        // excucao
        Mockito.when(meetupRepository.existsById(Mockito.anyInt())).thenReturn(false);
        Mockito.when(meetupRepository.save(meetup)).thenReturn(createMeetup());

        Meetup savedMeetup = meetupService.save(meetup);

        // assert
        assertThat(savedMeetup.getId()).isEqualTo(99);
        assertThat(savedMeetup.getEvent()).isEqualTo("Evento teste");
        assertThat(savedMeetup.getMeetupDate()).isEqualTo("01/01/2022");
    }

    @Test
    @DisplayName("Should findall meetups")
    public void find() {
        Example<Meetup> example = Example.of(createMeetup(),
                ExampleMatcher
                        .matching()
                        .withIgnoreCase()
                        .withIgnoreNullValues()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));

        Mockito.when(meetupRepository.findAll(example, pageRequest)).thenReturn(pageMeetup);

        Page<Meetup> pageReturn = meetupService.find(createMeetup(), pageRequest);

        assertThat(pageReturn).isInstanceOf(Page.class);
    }

    @Test
    @DisplayName("Should get Meetup by Id")
    public void getMeetupById() {
        Meetup meetup = createMeetup();
        Mockito.when(meetupRepository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(meetup));

        Optional<Meetup> resultado = meetupService.getMeetupById(meetup.getId());

        assertThat(resultado).isEqualTo(Optional.of(meetup));
    }

    @Test
    @DisplayName("Should get Meetup by Event")
    public void getMeetupByEvent() {
        Meetup meetup = createMeetup();
        Mockito.when(meetupRepository.findByEvent(Mockito.anyString())).thenReturn(Optional.ofNullable(meetup));

        Optional<Meetup> resultado = meetupService.getMeetupByEvent(meetup.getEvent());

        assertThat(resultado).isEqualTo(Optional.of(meetup));
    }

    @Test
    @DisplayName("Should update a Meetup")
    public void update() {
        Meetup meetup = createMeetupWithoutRegistrations();
        Meetup newMeetup = Meetup.builder()
                .id(meetup.getId())
                .event("Updated")
                .meetupDate("01/01/2025")
                .registrations(Collections.emptyList())
                .build();

        Mockito.when(meetupRepository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(meetup));
        Mockito.when(meetupRepository.save(meetup)).thenReturn(newMeetup);

        Meetup savedMeetup = meetupService.update(meetup);

        assertThat(savedMeetup.getId()).isEqualTo(meetup.getId());
        assertThat(savedMeetup.getEvent()).isEqualTo(newMeetup.getEvent());
        assertThat(savedMeetup.getMeetupDate()).isEqualTo(newMeetup.getMeetupDate());
        assertThat(savedMeetup.getRegistrations()).isEqualTo(newMeetup.getRegistrations());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when try to update Meetup without id")
    public void notUpdateNull() {
        Meetup nullIdMeetup = Meetup.builder().id(null).build();

        Throwable exception = Assertions.catchThrowable( () -> meetupService.update(nullIdMeetup)) ;
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Meetup id cannot be null!");

        Mockito.verify(meetupRepository, Mockito.never()).save(nullIdMeetup);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when try to update Meetup not found")
    public void notUpdateNotFound() {
        Meetup meetup = createMeetup();

        Mockito.when(meetupRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        Throwable exception = Assertions.catchThrowable( () -> meetupService.update(meetup)) ;
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Meetup not found!");

        Mockito.verify(meetupRepository, Mockito.never()).save(meetup);
    }

    @Test
    @DisplayName("Should delete a meetup without registrations")
    public void delete() {
        Meetup meetup = createMeetupWithoutRegistrations();
        Mockito.when(meetupRepository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(meetup));

        assertDoesNotThrow(() -> meetupService.delete(meetup.getId()));

        Mockito.verify(meetupRepository, Mockito.times(1)).delete(meetup);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when try to delete Meetup not found")
    public void notDeleteNotFound() {
        Meetup meetup = createMeetup();

        Mockito.when(meetupRepository.existsById(Mockito.anyInt())).thenReturn(false);

        Throwable exception = Assertions.catchThrowable( () -> meetupService.delete(meetup.getId())) ;
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Meetup not found!");

        Mockito.verify(meetupRepository, Mockito.times(0)).delete(meetup);
    }

    @Test
    @DisplayName("Should throw BusinessException when try to delete a Meetup with Registrations subscribed")
    public void notDeleteWithRegistrations() {
        Meetup meetup = createMeetup();

        Mockito.when(meetupRepository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(meetup));

        Throwable exception = Assertions.catchThrowable( () -> meetupService.delete(meetup.getId())) ;
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Meetup already has registrations! Please, unregister first.");

        Mockito.verify(meetupRepository, Mockito.times(0)).delete(meetup);
    }


    private Meetup createMeetup() {
        return Meetup.builder()
                .id(99)
                .event("Evento teste")
                .meetupDate("01/01/2022")
                .registrations(List.of(createValidRegistration()))
                .build();
    }

    private Meetup createMeetupWithoutRegistrations() {
        return Meetup.builder()
                .id(99)
                .event("Evento teste")
                .meetupDate("01/01/2022")
                .registrations(Collections.emptyList())
                .build();
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
