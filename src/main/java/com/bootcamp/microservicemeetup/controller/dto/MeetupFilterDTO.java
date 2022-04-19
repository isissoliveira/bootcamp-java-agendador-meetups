package com.bootcamp.microservicemeetup.controller.dto;


import com.bootcamp.microservicemeetup.model.entity.Registration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetupFilterDTO {

    private Integer id;
    @NotEmpty
    private String meetupDate;
    @NotEmpty
    private  String event;

}
