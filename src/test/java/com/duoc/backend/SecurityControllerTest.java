package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

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

        // 1. REGISTRO: Usamos un Map estructurado para evitar el error de Jackson
        // (GrantedAuthority)
        Map<String, String> registerReq = new HashMap<>();
        registerReq.put("username", uniqueUser);
        registerReq.put("password", "admin123");
        registerReq.put("email", uniqueUser + "@duoc.cl");

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // 2. LOGIN: Validamos que el usuario recién creado pueda autenticarse
        mockMvc.perform(post("/login").param("user", uniqueUser).param("password", "admin123"))
                .andExpect(status().isOk());
    }
}
