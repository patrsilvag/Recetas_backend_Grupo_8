package com.duoc.backend;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collections;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



@Entity // This tells Hibernate to make a table out of this class
@Table(name = "USERS")
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Integer id;

  private String username;

  private String email;

  private String password;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    // Esto asigna el rol "ROLE_USER" a cualquier usuario que haga login exitoso
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
}

@Override
public boolean isAccountNonExpired() {
  return true; // Cambiado de Exception a true
}

@Override
public boolean isAccountNonLocked() {
  return true; // Permite que el usuario no esté bloqueado
}

@Override
public boolean isCredentialsNonExpired() {
  return true; // Las credenciales no expiran por ahora
}

@Override
public boolean isEnabled() {
  return true; // El usuario está habilitado para entrar
}

}