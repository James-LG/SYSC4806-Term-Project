package ca.carleton.models;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "user", path = "user")
public interface UserRepository extends PagingAndSortingRepository<Users, Integer> {
    List<Users> findByName(@Param("name") String name);

    Users findByUsername(@Param("username") String username);
}
