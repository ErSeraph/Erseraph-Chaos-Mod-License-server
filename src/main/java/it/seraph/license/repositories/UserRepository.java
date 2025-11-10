package it.seraph.license.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import it.seraph.license.entities.Users;

public interface UserRepository extends JpaRepository<Users, Long> {

	Optional<Users> findByLicenza(String licenza);

	Optional<Users> findByUsername(String username);

	@Query("SELECT u FROM Users u WHERE u.bannato = FALSE")
    List<Users> findAllTikTokUsernames();

}
