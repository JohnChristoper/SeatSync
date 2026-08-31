package com.seatsync.backend.auth;

import com.seatsync.backend.user.User;
import com.seatsync.backend.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/*  Tells Spring that this class handles web requests and directly serializes the response objects
    Sets the base URL path for all endpoints defined in this controller.
    Every endpoint inside this class will start with /api/auth.*/
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Registration endpoint
    /*
        @Valid means, trigger the jakarta validation first on RegisterRequest object before
        executing the methods (which is the @Nullable and @Email).

        @RequestBody means, take the raw JSON object in the HTTP request and turn it into
        RegisterRequest object.

        ResponseEntity represents the entire HTTP response (status code, header & body).
        ? = allows returning different body type (e.g. success & error message)
    * */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            return ResponseEntity.badRequest().body("Email already in use");
        }

        //new entity will be created once email is not in use
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if(userOptional.isEmpty()){
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        User user = userOptional.get();

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        return ResponseEntity.ok("Login successful, welcome " + user.getName());
    }
}
