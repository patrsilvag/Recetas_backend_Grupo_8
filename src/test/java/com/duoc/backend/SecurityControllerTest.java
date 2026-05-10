package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Importante: Limpia la BD después de cada test
@SuppressWarnings("null")
class SecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLoginAndRegisterFlow() throws Exception {
        String uniqueUser = "user_" + System.currentTimeMillis();

        // 1. REGISTRO: Usamos el LoginDTO (o el Map) para enviar un JSON válido
        LoginDTO registerReq = new LoginDTO(uniqueUser, "admin123", uniqueUser + "@duoc.cl");

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // 2. LOGIN: ✅ CORRECCIÓN - Ahora enviamos un JSON, no parámetros de URL
        // Usamos el mismo DTO para el login (el campo email puede ir nulo o ignorarse)
        LoginDTO loginReq = new LoginDTO(uniqueUser, "admin123", null);

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq))).andExpect(status().isOk());
    }
    
    @Test
    void testLoginFailures() throws Exception {
    // Caso 1: Usuario que no existe
    LoginDTO wrongUser = new LoginDTO("usuario_fantasma", "password", null);
    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(wrongUser)))
            .andExpect(status().isUnauthorized());

    // Caso 2: Contraseña incorrecta
    // Primero registramos uno
    String user = "test_fail_" + System.currentTimeMillis();
    LoginDTO reg = new LoginDTO(user, "real_pass", user + "@test.com");
    mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reg)));

    // Intentamos login con pass mala
    LoginDTO wrongPass = new LoginDTO(user, "wrong_pass", null);
    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(wrongPass)))
            .andExpect(status().isUnauthorized());
}

}
