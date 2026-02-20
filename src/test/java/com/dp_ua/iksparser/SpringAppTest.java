package com.dp_ua.iksparser;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class SpringAppTest {

    @MockBean
    App app;

    @Test
    void shouldStartContext() {
        assertTrue(TRUE);
    }

}
