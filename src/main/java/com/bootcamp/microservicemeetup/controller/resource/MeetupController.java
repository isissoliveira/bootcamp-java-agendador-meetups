package com.bootcamp.microservicemeetup.controller.resource;

import com.bootcamp.microservicemeetup.controller.dto.MeetupDTO;
import com.bootcamp.microservicemeetup.controller.dto.MeetupFilterDTO;
import com.bootcamp.microservicemeetup.controller.dto.RegistrationFilterDTO;
import com.bootcamp.microservicemeetup.exception.BusinessException;
import com.bootcamp.microservicemeetup.model.entity.Meetup;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.service.MeetupService;
import com.bootcamp.microservicemeetup.service.RegistrationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meetups")
@RequiredArgsConstructor
@Api(value = "API Rest Meetup")
public class MeetupController {
    @Autowired
    private RegistrationService registrationService;

    private final MeetupService meetupService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a meetup")
    private MeetupDTO create(@RequestBody MeetupDTO meetupDTO) {
        if (meetupService.getMeetupByEvent(meetupDTO.getEvent()).isPresent()){
            throw new BusinessException("A Meetup já existe!");
        }
        Meetup entity = Meetup.builder()
                .event(meetupDTO.getEvent())
                .meetupDate(meetupDTO.getMeetupDate())
                .build();

        entity = meetupService.save(entity);
        return modelMapper.map(entity, MeetupDTO.class);
    }

    @GetMapping
    @ApiOperation(value = "Find all meetups")
    public Page<MeetupDTO> find(MeetupFilterDTO dto, Pageable pageRequest) {
        Meetup filter = modelMapper.map(dto, Meetup.class);
        Page<Meetup> result = meetupService.find(filter, pageRequest);
        List<MeetupDTO> meetups = result
                .getContent()
                .stream()
                .map(entity -> {

                    MeetupDTO meetupDTO = modelMapper.map(entity, MeetupDTO.class);

                    return meetupDTO;
                }).collect(Collectors.toList());
        return new PageImpl<MeetupDTO>(meetups, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}")
    @ApiOperation(value = "Find a especific Meetup")
    public MeetupDTO findById(@PathVariable Integer id) {
        return meetupService.getMeetupById(id).map(
                meetup -> modelMapper.map(meetup, MeetupDTO.class)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("{id}")
    @ApiOperation(value = "Update a specific meetup")
    public MeetupDTO update(@PathVariable Integer id, @RequestBody @Valid MeetupDTO meetupDTO) {

        return meetupService.getMeetupById(id).map(meetup -> {
            if(!meetup.getRegistrations().isEmpty()){
                throw new BusinessException("Meetup already has registrations!");
            }
            meetup.setEvent(meetupDTO.getEvent());
            meetup.setMeetupDate(meetupDTO.getMeetupDate());

            meetup = meetupService.update(meetup);

            return modelMapper.map(meetup, MeetupDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Delete a specific meetup")
    public void deleteById(@PathVariable Integer id) {
        Meetup meetup = meetupService.getMeetupById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!meetup.getRegistrations().isEmpty()) {
            throw new BusinessException("A Meetup não pode ser removida. Há Registrations adicionadas!");
        }
        meetupService.delete(id);
    }

    @PutMapping("{id}/subscribe")
    @ApiOperation(value = "Add a Registration to a specific Meetup")
    public MeetupDTO subscribe(@PathVariable Integer id, @RequestBody @Valid RegistrationFilterDTO registrationFilterDTO) {

        return meetupService.getMeetupById(id).map(meetup -> {
            Optional<Registration> registration = registrationService.getRegistrationById(registrationFilterDTO.getId());

            if (!registration.isPresent()) {
                throw new IllegalArgumentException("Registration não encontrada!");
            }

            if (meetup.getRegistrations().contains(registration.get())) {
                throw new BusinessException("A Meetup já contém a registration informada!");
            }
            for ( Meetup meet:registration.get().getMeetups()) {
                if(meet.getMeetupDate().equals(meetup.getMeetupDate())){
                    throw new BusinessException("A Registration já está inscrita em outra meetup no dia "+ meetup.getMeetupDate());
                }
            }
            meetup.getRegistrations().add(registration.get());
            registration.get().getMeetups().add(meetup);

            meetup = meetupService.update(meetup);
            registrationService.update(registration.get());

            return modelMapper.map(meetup, MeetupDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("{id}/unsubscribe")
    @ApiOperation(value = "Remove a Registration from a specific Meetup")
    public MeetupDTO unsubscribe(@PathVariable Integer id, @RequestBody @Valid RegistrationFilterDTO registrationFilterDTO) {

        return meetupService.getMeetupById(id).map(meetup -> {
            Optional<Registration> registration = registrationService.getRegistrationById(registrationFilterDTO.getId());

            if (!registration.isPresent()) {
                throw new IllegalArgumentException("Registration não encontrada!");
            }
            meetup.getRegistrations().remove(registration.get());
            registration.get().getMeetups().remove(meetup);

            meetup = meetupService.update(meetup);
            registrationService.update(registration.get());

            return modelMapper.map(meetup, MeetupDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }


}
