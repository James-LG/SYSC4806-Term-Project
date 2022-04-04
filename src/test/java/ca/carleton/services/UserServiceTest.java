package ca.carleton.services;

import ca.carleton.models.Admin;
import ca.carleton.models.Customer;
import ca.carleton.models.User;
import ca.carleton.models.UserRepository;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    @TestConfiguration
    static class UserServiceTestContextConfiguration {
        @Bean
        public BCryptPasswordEncoder bCryptPasswordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @MockBean
        private UserRepository userRepository;

        @Bean
        public UserService userService() {
            return new UserServiceImpl(userRepository, bCryptPasswordEncoder());
        }
    }

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    public void save_shouldEncryptPassword() {
        // arrange
        User user = new Admin("user", "bob", "password");

        // act
        userService.save(user);

        // assert
        assertThat(bCryptPasswordEncoder.matches("password", user.getPassword())).isTrue();
    }
}
