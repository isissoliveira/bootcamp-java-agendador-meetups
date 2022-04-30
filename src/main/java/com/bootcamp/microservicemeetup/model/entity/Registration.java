package com.bootcamp.microservicemeetup.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Set;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "registrations")
public class Registration implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", nullable = false)
    private String name;

    @Column(name = "data_cadastro", nullable = false)
    private String dateOfRegistration;

    @Column( nullable = false, unique = true)
    private String registration;

    @Column(name = "senha", nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(name = "registrations_roles",
            joinColumns = @JoinColumn(
                    name = "registration_id", referencedColumnName = "registration"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "nomeRole")
    )
    private List<Role> roles;

    @ManyToMany
    @JoinTable(name = "registrations_meetups",
            joinColumns = @JoinColumn(
                    name = "registration_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "meetup_id", referencedColumnName = "id")
    )
    private List<Meetup> meetups;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return (Collection<? extends GrantedAuthority>) this.roles;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.registration;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
