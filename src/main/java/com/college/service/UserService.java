package com.college.service;

import com.college.model.*;
import com.college.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProgramRepository programRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String username, String password, String email, String fullName, String studentId, Long programId, MultipartFile profileImage) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role("ROLE_USER")
                .email(email)
                .build();
        if (programId != null) {
            programRepository.findById(programId).ifPresent(user::setProgram);
        }
        user = userRepository.save(user);
        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(fullName)
                .studentId(studentId)
                .profileImage(null)
                .build();
        userProfileRepository.save(profile);
        return user;
    }
}