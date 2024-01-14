package com.dp_ua.iksparser;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAppTest extends MockBotControllerTest {
    @Test
    public void shouldStartContext() {
        Assert.assertTrue(true);
    }

    @Override
    public void additionalSetUp() {
        // nothing to do
    }
}