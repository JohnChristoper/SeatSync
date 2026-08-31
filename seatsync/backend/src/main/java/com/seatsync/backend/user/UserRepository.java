package com.seatsync.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

//JpaRepository<Entity type, primary key data type>
public interface UserRepository extends JpaRepository<User, Long> {
    //Prevents NullPointerException. If no user exists with that email, it returns Optional.empty() instead of null
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
