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

import java.util.Map;
import java.util.Optional;

/*  Tells Spring that this class handles web requests and directly serializes the response objects
    Sets the base URL path for all endpoints defined in this controller.
    Every endpoint inside this class will start with /api/auth.*/
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository, RefreshTokenService refreshTokenService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenService = refreshTokenService;
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
            return ResponseEntity.badRequest().body("Email already in use.");
        }

        //new entity will be created once email is not in use
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        Optional<User> userOptional = userRepository.findByEmail(request.email());

        if(userOptional.isEmpty()){
            return ResponseEntity.status(401).body("Invalid email or password.");
        }

        User user = userOptional.get();

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())){
            return ResponseEntity.status(401).body("Invalid email or password.");
        }

        RefreshToken newREfreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(
                "Login successful, welcome " + user.getName() +
                        ".\nToken: " + newREfreshToken.getToken());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest payload){
        return refreshTokenRepository.findByToken(payload.getToken()).map(token -> {
            if(refreshTokenService.isTokenExpired(token)){
                refreshTokenRepository.delete(token);
                return ResponseEntity.badRequest().body("Refresh token expired. Please login again.");
            }

            RefreshToken newREfreshToken = refreshTokenService.createRefreshToken(token.getUser().getId());
            return ResponseEntity.ok(Map.of("token", newREfreshToken.getToken()));
        }).orElse(ResponseEntity.badRequest().body("Invalid refresh token."));
    }

}
