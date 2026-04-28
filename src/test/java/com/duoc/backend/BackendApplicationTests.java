package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {}

    // ✅ Test agregado directamente aquí para cubrir Constants.java
    @Test
    void testConstantsConstructor() {
        Constants constants = new Constants();
        assertNotNull(constants);
    }
}
