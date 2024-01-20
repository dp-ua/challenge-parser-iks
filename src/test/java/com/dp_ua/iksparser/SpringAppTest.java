package com.dp_ua.iksparser;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAppTest {
    @MockBean
    App app;

    @Test
    public void shouldStartContext() {
        Assert.assertTrue(true);
    }
}