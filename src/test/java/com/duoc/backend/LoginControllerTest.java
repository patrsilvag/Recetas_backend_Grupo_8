package com.duoc.backend;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoginController.class)
@Import(WebSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JWTAuthenticationConfig jwtAuthenticationConfig;

    @MockBean
    private MyUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JWTAuthorizationFilter jwtAuthorizationFilter;

    @Test
    void register_NullBody_ShouldReturn400() throws Exception {

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_UserAlreadyExists_ShouldReturn400() throws Exception {

        when(userRepository.existsByUsername("patricio")).thenReturn(true);

        String body = """
                {
                  "username":"patricio",
                  "password":"1234"
                }
                """;

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_EmailAlreadyExists_ShouldReturn400() throws Exception {

        when(userRepository.existsByUsername("patricio")).thenReturn(false);

        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        String body = """
                {
                  "username":"patricio",
                  "password":"1234",
                  "email":"test@test.com"
                }
                """;

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_Success_ShouldReturn201() throws Exception {

        when(userRepository.existsByUsername("patricio")).thenReturn(false);

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        String body = """
                {
                  "username":"patricio",
                  "password":"1234",
                  "email":"test@test.com"
                }
                """;

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }
}
