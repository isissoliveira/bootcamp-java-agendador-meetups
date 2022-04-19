package com.bootcamp.microservicemeetup.repository;

import com.bootcamp.microservicemeetup.model.entity.Meetup;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetupRepository extends JpaRepository<Meetup, Integer> {
    //@Query( value = " select l from Meetup as l join l.registration as b where b.registration = :registration or l.event =:event ")
 /*   @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name LIKE %?1%")
    Page<Meetup> findByRegistrationOnMeetup(
            @Param("registration") String registration,
            @Param("event") String event,
            Pageable pageable
    );*/

/*
    @Query( value = " select m from Meetup as m join m.registrations as regis where regs.registration = :registration or m.id =:id_meetup ")
    List<Registration> findByRegistrationOnMeetup(
            @Param("registration") String registration,
            @Param("id_meetup") Integer id_meetup
    );*/

/*
    Page<Meetup> findByRegistration(Registration registration, Pageable pageable );*/
}
