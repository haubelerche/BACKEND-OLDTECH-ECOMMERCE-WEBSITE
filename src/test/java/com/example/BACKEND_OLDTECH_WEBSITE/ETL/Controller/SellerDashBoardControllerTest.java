package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.SellerDashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(SellerDashBoardController.class)
class SellerDashBoardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SellerDashboardService sellerDashboardService;

    @Test
    @DisplayName("GET /api/seller/dashboard/summary should return 200")
    void testGetSummary() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/seller/dashboard/summary"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
