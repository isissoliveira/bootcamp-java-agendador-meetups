package com.bootcamp.microservicemeetup.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table( name = "roles")
public class Role implements GrantedAuthority {
    @Id
    @Column(nullable = false)
    private String nomeRole;

    @ManyToMany
    private List<Registration> registrations;

    @Override
    public String getAuthority() {
        return this.nomeRole;
    }
}
