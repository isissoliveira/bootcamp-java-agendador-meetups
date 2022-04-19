package com.bootcamp.microservicemeetup.service;

import com.bootcamp.microservicemeetup.controller.dto.MeetupFilterDTO;
import com.bootcamp.microservicemeetup.model.entity.Meetup;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MeetupService {

    Meetup save(Meetup meetup);

    Optional<Meetup> getMeetupById(Integer id);

    Meetup update(Meetup loan);

    void delete(Integer id);

    Page<Meetup> find(Meetup filter, Pageable pageable);

    Page<Meetup> getRegistrationsByMeetup(Registration registration, Pageable pageable);


}
