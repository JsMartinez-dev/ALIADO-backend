package com.sebastian.spring.mas.aliado.aliado.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** El contrato HTTP debe rechazar decimales/cadenas antes de que Jackson los coaccione a Integer. */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PropuestaTarifaHttpJsonTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE = """
            {"solicitudId":"cccccccc-cccc-cccc-cccc-cccccccccc01",
             "disponibilidad":"26 nov 2026, 09:00",
             "tarifaCop":%s}
            """;

    @Test
    void tarifaDecimalSeRechazaCon400() throws Exception {
        mockMvc.perform(post("/api/v1/propuestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BASE.formatted("250000.50")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("TARIFA_INVALIDA")));
    }

    @Test
    void tarifaComoCadenaSeRechazaCon400() throws Exception {
        mockMvc.perform(post("/api/v1/propuestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BASE.formatted("\"250000\"")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("TARIFA_INVALIDA")));
    }
}
