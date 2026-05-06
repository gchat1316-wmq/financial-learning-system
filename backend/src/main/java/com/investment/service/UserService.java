package com.investment.service;

import com.investment.dto.UserProfileUpdateRequest;
import com.investment.entity.User;
import com.investment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }

    @Transactional
    public User updateProfile(String username, UserProfileUpdateRequest request) {
        User user = getUserByUsername(username);

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getThemePreference() != null) {
            user.setThemePreference(request.getThemePreference());
        }

        return userRepository.save(user);
    }
}
