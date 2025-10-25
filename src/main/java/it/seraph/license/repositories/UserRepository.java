package it.seraph.license.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.seraph.license.entities.Users;

public interface UserRepository extends JpaRepository<Users, Long> {

	Optional<Users> findByLicenza(String licenza);

}
