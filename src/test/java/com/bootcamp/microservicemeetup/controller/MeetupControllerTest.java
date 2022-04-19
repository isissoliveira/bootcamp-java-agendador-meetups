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

        BDDMockito.given(meetupService.update(meetup)).willThrow(BusinessException.class);

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



/*
    @Test
    @DisplayName("Should register on a meetup")
    public void createMeetupTest() throws Exception {

        // quando enviar uma requisicao pra esse registration precisa ser encontrado um valor que tem esse usuario
        MeetupDTO dto = MeetupDTO.builder().event("Womakerscode Dados").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Registration registration = Registration.builder().id(11).registration("123").build();

        BDDMockito.given(registrationService.getRegistrationByRegistrationAttribute("123")).
                willReturn(Optional.of(registration));
        //desfazer comentarios abaixo
      //  Meetup meetup = Meetup.builder().id(11).event("Womakerscode Dados").registration(registration).meetupDate("10/10/2021").build();

     //   BDDMockito.given(meetupService.save(Mockito.any(Meetup.class))).willReturn(meetup);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(MEETUP_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // Aqui o que retorna é o id do registro no meetup
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("11"));

    }

    @Test
    @DisplayName("Should return error when try to register an a meetup nonexistent")
    public void invalidRegistrationCreateMeetupTest() throws Exception {

        MeetupDTO dto = MeetupDTO.builder().event("Womakerscode Dados").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(registrationService.getRegistrationByRegistrationAttribute("123")).
                willReturn(Optional.empty());


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(MEETUP_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Should return error when try to register a registration already register on a meetup")
    public void  meetupRegistrationErrorOnCreateMeetupTest() throws Exception {

        MeetupDTO dto = MeetupDTO.builder().event("Womakerscode Dados").build();
        String json = new ObjectMapper().writeValueAsString(dto);


        Registration registration = Registration.builder().id(11).name("Ana Neri").registration("123").build();
        BDDMockito.given(registrationService.getRegistrationByRegistrationAttribute("123"))
                .willReturn(Optional.of(registration));

        // procura na base se ja tem algum registration pra esse meetup
        BDDMockito.given(meetupService.save(Mockito.any(Meetup.class))).willThrow(new BusinessException("Meetup already enrolled"));


        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(MEETUP_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }
*/

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
