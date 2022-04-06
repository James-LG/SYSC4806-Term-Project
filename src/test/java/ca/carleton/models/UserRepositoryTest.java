package ca.carleton.models;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Date;
import java.util.List;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void findByUsername_shouldReturnSameAdmin() {
        // arrange
        Admin admin = new Admin("a", "a", "a");

        userRepository.save(admin);

        // act
        Admin admin2 = (Admin)userRepository.findByUsername("a");

        // assert
        assertThat(admin).isInstanceOf(Admin.class);
        assertThat(admin.getUsername()).isEqualTo(admin2.getUsername());
    }

    @Test
    public void findByUsername_shouldReturnSameCustomer() {
        // arrange
        Customer customer = new Customer("a", "a", "a");

        userRepository.save(customer);

        // act
        Customer customer2 = (Customer)userRepository.findByUsername("a");

        // assert
        assertThat(customer.getUsername()).isEqualTo(customer2.getUsername());
        assertThat(customer.getAccessExpiration()).isEqualTo(customer2.getAccessExpiration());
    }

    @Test
    public void findByName_shouldReturnEveryUserWithName() {
        // arrange
        User customer = new Customer("a", "a", "a");
        User admin = new Admin("a2", "a", "a");

        userRepository.save(customer);
        userRepository.save(admin);

        // act
        List<User> users = userRepository.findByName("a");

        // assert
        assertThat(users).hasSize(2);

        assertThat(users.get(0).getUsername()).isEqualTo(customer.getUsername());
        assertThat(users.get(0)).isInstanceOf(Customer.class);

        assertThat(users.get(1).getUsername()).isEqualTo(admin.getUsername());
        assertThat(users.get(1)).isInstanceOf(Admin.class);
    }
}
