package com.chatserver.service;

import com.chatserver.model.User;
import com.chatserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(String username) throws Exception {
        if (userRepository.existsByUsername(username)) {
            throw new Exception("Username already exists");
        }
        
        User user = new User(username);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void updateUserOnlineStatus(String username, boolean isOnline) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOnline(isOnline);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
