package com.bootcamp.microservicemeetup.controller;


import com.bootcamp.microservicemeetup.controller.dto.RegistrationDTO;
import com.bootcamp.microservicemeetup.controller.dto.RegistrationFilterDTO;
import com.bootcamp.microservicemeetup.controller.resource.MeetupController;
import com.bootcamp.microservicemeetup.exception.BusinessException;
import com.bootcamp.microservicemeetup.controller.dto.MeetupDTO;
import com.bootcamp.microservicemeetup.model.entity.Meetup;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.security.UserDetailServiceImpl;
import com.bootcamp.microservicemeetup.service.MeetupService;
import com.bootcamp.microservicemeetup.service.RegistrationService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = {MeetupController.class})
@AutoConfigureMockMvc(addFilters = false) // addFilters = false para não ser bloqueado pelos filtros do spring security
public class MeetupControllerTest {

    static final String MEETUP_API = "/api/meetups";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private MeetupService meetupService;

    @MockBean
    private UserDetailServiceImpl userDetailService;

    @Test
    @DisplayName("Should register on a meetup")
    public void create() throws Exception {

        // cenario
        MeetupDTO meetupDTO = MeetupDTO.builder()
                .id(99)
                .event("Evento teste")
                .meetupDate("01/01/2022")
                .build();
        Meetup savedMeetup = Meetup.builder()
                .id(99)
                .event("Evento teste")
                .meetupDate("01/01/2022")
                .build();

        BDDMockito.given(meetupService.save(any(Meetup.class))).willReturn(savedMeetup);

        String json = new ObjectMapper().writeValueAsString(meetupDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(MEETUP_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(meetupDTO.getId()))
                .andExpect(jsonPath("event").value(meetupDTO.getEvent()))
                .andExpect(jsonPath("meetupDate").value(meetupDTO.getMeetupDate()));
    }

    @Test
    @DisplayName("Should find all meetups")
    public void find() throws Exception {
        Meetup meetup = createMeetup();

        BDDMockito.given(meetupService.find(Mockito.any(Meetup.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Meetup>(Arrays.asList(meetup), PageRequest.of(0, 100), 1));

        String queryString = String.format("?event=%s&meetupDate=%s&page=0&size=100",
                meetup.getEvent(), meetup.getMeetupDate());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(MEETUP_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

    @Test
    @DisplayName("Should find a meetups by its id")
    public void findById() throws Exception {
        Meetup meetup = createMeetup();
        MeetupDTO meetupDTO = createMeetupDTO();

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt()))
                .willReturn(Optional.ofNullable(meetup));

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(MEETUP_API.concat("/"+ meetup.getId()))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(meetupDTO.getId()))
                .andExpect(jsonPath("event").value(meetupDTO.getEvent()))
                .andExpect(jsonPath("meetupDate").value(meetupDTO.getMeetupDate()))
                .andExpect(jsonPath("registrations[0]").value(meetupDTO.getRegistrations().get(0)));
    }

    @Test
    @DisplayName("Should update a meetup")
    public void update() throws Exception {
        Meetup meetup = createMeetup();
        meetup.setRegistrations(List.of());
        MeetupDTO meetupDTO = createMeetupDTO();

        Meetup updated_meetup = createMeetup();
        updated_meetup.setEvent("Updated event");
        updated_meetup.setMeetupDate("12-12-2022");

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.ofNullable(meetup));
        BDDMockito.given(meetupService.update(meetup)).willReturn(updated_meetup);

        String json = new ObjectMapper().writeValueAsString(meetupDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + meetup.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(meetupDTO.getId()))
                .andExpect(jsonPath("event").value(updated_meetup.getEvent()))
                .andExpect(jsonPath("meetupDate").value(updated_meetup.getMeetupDate()));
    }

    @Test
    @DisplayName("Should return not found when try to update Meetup without id")
    public void notUpdateNull() throws Exception {
        Meetup meetup = createMeetup();
        meetup.setId(null);
        MeetupDTO meetupDTO = createMeetupDTO();
        meetupDTO.setId(null);

        BDDMockito.given(meetupService.update(meetup)).willThrow(IllegalArgumentException.class);

        String json = new ObjectMapper().writeValueAsString(meetupDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + meetup.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when try to update a non existing meetup")
    public void notUpdateNotFound() throws Exception {
        Meetup meetup = createMeetup();
        MeetupDTO meetupDTO = createMeetupDTO();

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.empty());

        String json = new ObjectMapper().writeValueAsString(meetupDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + meetup.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return bad request when try to update a meetup with registrations")
    public void notUpdateWithRegistrations() throws Exception {
        Meetup meetup = createMeetup();
        MeetupDTO meetupDTO = createMeetupDTO();

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.ofNullable(meetup));

        String json = new ObjectMapper().writeValueAsString(meetupDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + meetup.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return not found when try to delete a non existing meetup")
    public void notDeleteNotFound() throws Exception {
        Meetup meetup = createMeetup();

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(MEETUP_API.concat("/" + meetup.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return bad request when try to delete a meetup with registrations")
    public void notDeleteWithRegistrations() throws Exception {
        Meetup meetup = createMeetup();

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.of(meetup));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(MEETUP_API.concat("/" + meetup.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should subscribe on a meetup")
    public void subscribe() throws Exception {
        Meetup meetup = createMeetup();
        meetup.setRegistrations(new ArrayList<Registration>());
        RegistrationFilterDTO registrationFilterDTO = createValidRegistrationFilterDTO();

        Meetup updated_meetup = createMeetup();

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.ofNullable(meetup));
        BDDMockito.given(registrationService.getRegistrationById(Mockito.anyInt())).willReturn(Optional.ofNullable(createValidRegistration()));

        BDDMockito.given(meetupService.update(meetup)).willReturn(updated_meetup);

        String json = new ObjectMapper().writeValueAsString(registrationFilterDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + meetup.getId() + "/subscribe"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(updated_meetup.getId()))
                .andExpect(jsonPath("registrations[0]").value(registrationFilterDTO));
    }

    @Test
    @DisplayName("Should not subscribe on a non existing meetup")
    public void notSubscribeNotFound() throws Exception {
        Meetup meetup = createMeetup();
        meetup.setRegistrations(new ArrayList<Registration>());
        RegistrationFilterDTO registrationFilterDTO = createValidRegistrationFilterDTO();

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.ofNullable(null));

        String json = new ObjectMapper().writeValueAsString(registrationFilterDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + meetup.getId() + "/subscribe"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should not subscribe on a meetup when registration not found")
    public void notSubscribeNotFoundRegistration() throws Exception {
        Meetup meetup = createMeetup();
        meetup.setRegistrations(new ArrayList<Registration>());
        RegistrationFilterDTO registrationFilterDTO = createValidRegistrationFilterDTO();

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.ofNullable(meetup));
        BDDMockito.given(registrationService.getRegistrationById(Mockito.anyInt())).willReturn(Optional.ofNullable(null));

        String json = new ObjectMapper().writeValueAsString(registrationFilterDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + meetup.getId() + "/subscribe"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should not subscribe on a meetup twice")
    public void notSubscribeAlreadySubscribed() throws Exception {
        Meetup meetup = createMeetup();
        RegistrationFilterDTO registrationFilterDTO = createValidRegistrationFilterDTO();

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.ofNullable(meetup));
        BDDMockito.given(registrationService.getRegistrationById(Mockito.anyInt())).willReturn(Optional.ofNullable(createValidRegistration()));

        String json = new ObjectMapper().writeValueAsString(registrationFilterDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + meetup.getId() + "/subscribe"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not subscribe on another meetup at the same date")
    public void notSubscribeAlreadySubscribedOnAnother() throws Exception {
        Meetup meetup = createMeetup();
        meetup.setRegistrations(new ArrayList<Registration>());
        RegistrationFilterDTO registrationFilterDTO = createValidRegistrationFilterDTO();

        Registration registration = createValidRegistration();
        Meetup anotherMeetup = Meetup.builder()
                .id(100)
                .event("Evento teste")
                .meetupDate("01/01/2022")
                .registrations(List.of(registration))
                .build();
        registration.getMeetups().add(anotherMeetup);

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.ofNullable(meetup));
        BDDMockito.given(registrationService.getRegistrationById(Mockito.anyInt())).willReturn(Optional.ofNullable(registration));

        String json = new ObjectMapper().writeValueAsString(registrationFilterDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + meetup.getId() + "/subscribe"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Should unsubscribe on a meetup")
    public void unsubscribe() throws Exception {
        Meetup meetup = createMeetup();
        RegistrationFilterDTO registrationFilterDTO = createValidRegistrationFilterDTO();

        Meetup updated_meetup = createMeetup();
        updated_meetup.setRegistrations(new ArrayList<Registration>());

        BDDMockito.given(meetupService.getMeetupById(Mockito.anyInt())).willReturn(Optional.ofNullable(meetup));
        BDDMockito.given(registrationService.getRegistrationById(Mockito.anyInt())).willReturn(Optional.ofNullable(createValidRegistration()));

        BDDMockito.given(meetupService.update(meetup)).willReturn(updated_meetup);

        String json = new ObjectMapper().writeValueAsString(registrationFilterDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + meetup.getId() + "/unsubscribe"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(updated_meetup.getId()))
                .andExpect(jsonPath("registrations[0]").value(null));
    }



    private Meetup createMeetup() {
        return Meetup.builder()
                .id(99)
                .event("Evento teste")
                .meetupDate("01/01/2022")
                .registrations(List.of(createValidRegistration()))
                .build();
    }

    private Registration createValidRegistration() {
        return Registration.builder()
                .id(101)
                .name("Isis Oliveira")
                .dateOfRegistration("01/01/2022")
                .registration("001")
                .password("123")
                .meetups(new ArrayList<Meetup>())
                .build();
    }

    private MeetupDTO createMeetupDTO() {
        return MeetupDTO.builder()
                .id(99)
                .event("Evento teste")
                .meetupDate("01/01/2022")
                .registrations(List.of(createValidRegistrationFilterDTO()))
                .build();
    }

    private RegistrationFilterDTO createValidRegistrationFilterDTO() {
        return RegistrationFilterDTO.builder()
                .id(101)
                .name("Isis Oliveira")
                .dateOfRegistration("01/01/2022")
                .registration("001")
                .build();
    }
}
