package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest
class KafkaManagementControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/kafka/topics should return 200")
    void testGetTopics() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/kafka/topics"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
