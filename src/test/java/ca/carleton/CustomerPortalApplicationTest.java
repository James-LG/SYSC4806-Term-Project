package ca.carleton;

import static org.assertj.core.api.Assertions.assertThat;

import ca.carleton.controllers.AccountController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CustomerPortalApplicationTest {

    @Autowired
    private AccountController controller;

    @Test
    public void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }
}
