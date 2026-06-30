package com.imcs.security;

import com.imcs.entity.UserEntity;
import com.imcs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Value("${security.max-login-attempts:3}")
    private int maxAttempts;

    private final Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    private String currentUser = null;

    public enum LoginResult {
        SUCCESS, WRONG_PASSWORD, USER_NOT_FOUND, ACCOUNT_LOCKED
    }

    public LoginResult login(String username, String password) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return LoginResult.USER_NOT_FOUND;

        UserEntity user = userOpt.get();
        if (user.isLocked()) return LoginResult.ACCOUNT_LOCKED;

        if (encoder.matches(password, user.getPassword())) {
            user.setFailedAttempts(0);
            userRepository.save(user);
            currentUser = username;
            return LoginResult.SUCCESS;
        } else {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= maxAttempts) user.setLocked(true);
            userRepository.save(user);
            return LoginResult.WRONG_PASSWORD;
        }
    }

    public void logout() { currentUser = null; }
    public String getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }
}