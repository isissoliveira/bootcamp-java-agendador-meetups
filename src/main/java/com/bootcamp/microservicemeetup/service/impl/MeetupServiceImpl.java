package com.bootcamp.microservicemeetup.service.impl;

import com.bootcamp.microservicemeetup.controller.dto.MeetupFilterDTO;
import com.bootcamp.microservicemeetup.exception.BusinessException;
import com.bootcamp.microservicemeetup.model.entity.Meetup;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.repository.MeetupRepository;
import com.bootcamp.microservicemeetup.service.MeetupService;
import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.management.AttributeNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class MeetupServiceImpl implements MeetupService {


    private MeetupRepository repository;

    public MeetupServiceImpl(MeetupRepository repository) {
        this.repository = repository;
    }

    @Override
    public Meetup save(Meetup meetup) {return repository.save(meetup);}

    @Override
    public Page<Meetup> find(Meetup filter, Pageable pageRequest) {

        Example<Meetup> example = Example.of(filter,
                ExampleMatcher
                        .matching()
                        .withIgnoreCase()
                        .withIgnoreNullValues()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));

        return repository.findAll(example, pageRequest);
    }

    @Override
    public Optional<Meetup> getMeetupById(Integer id) {
        return this.repository.findById(id);
    }

    @Override
    public Optional<Meetup> getMeetupByEvent(String event) {
        return this.repository.findByEvent(event);
    }

    @Override
    public Meetup update(Meetup meetup) {
        if (meetup == null || meetup.getId() == null) {
            throw new IllegalArgumentException("Meetup id cannot be null!");
        }
        Optional<Meetup> original_meetup = repository.findById(meetup.getId());
        if(!original_meetup.isPresent()){
            throw new IllegalArgumentException("Meetup not found!");
        }
        return this.repository.save(meetup);
    }

    @Override
    public void delete(Integer id) {

        Optional<Meetup> meetup = repository.findById(id);
        if(!meetup.isPresent()){
            throw new IllegalArgumentException("Meetup not found!");
        }
        if(!meetup.get().getRegistrations().isEmpty()){
            throw new BusinessException("Meetup already has registrations! Please, unregister first.");
        }
        this.repository.delete(meetup.get());
    }


    @Override
    public Page<Meetup> getRegistrationsByMeetup(Registration registration, Pageable pageable) {
        return null;
    }

 /*   @Override
    public Page<Meetup> find(MeetupFilterDTO filterDTO, Pageable pageable) {
        return repository.findByRegistrationOnMeetup( filterDTO.getRegistration(), filterDTO.getEvent(), pageable );
    }


    @Override
    public Page<Meetup> getRegistrationsByMeetup(Registration registration, Pageable pageable) {
        return repository.findByRegistration(registration, pageable);
    }
*/

}