package com.dp_ua.iksparser.api.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {CorsFilter.class, TestController.class})
public class CorsFilterTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCorsHeadersAreSet() throws Exception {
        mockMvc.perform(get("/test")
                        .header("Origin", "http://localhost")) // Mocking request with origin header
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", "GET, POST"))
                .andExpect(header().string("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With"));
    }
}