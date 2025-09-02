package com.college.service;

import com.college.model.*;
import com.college.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProgramRepository programRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String username, String password, String email, String fullName, String studentId, Long programId, String year, MultipartFile profileImage) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role("ROLE_USER")
                .email(email)
                .build();
        // Validate year for program
        if (programId != null) {
            programRepository.findById(programId).ifPresent(program -> {
                if (year != null && (year.equals("1st Year") || year.equals("2nd Year") || year.equals("3rd Year"))) {
                    program.setYear(year);
                }
                user.setProgram(program);
            });
        }
        userRepository.save(user);

        byte[] imageBytes = null;
        String imageContentType = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                imageBytes = profileImage.getBytes();
                imageContentType = profileImage.getContentType();
            } catch (IOException e) {
                // Log or handle error as needed
            }
        }

        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(fullName)
                .studentId(studentId)
                .profileImage(imageBytes)
                .imageContentType(imageContentType)
                .build();
        userProfileRepository.save(profile);
        user.setUserProfile(profile);
        return user;
    }
}