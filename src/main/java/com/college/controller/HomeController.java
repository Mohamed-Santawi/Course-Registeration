package com.college.controller;

import com.college.model.*;
import com.college.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProgramRepository programRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("programs", programRepository.findAll());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String email,
                                 @RequestParam String fullName,
                                 @RequestParam String studentId,
                                 @RequestParam(required = false) MultipartFile profileImage,
                                 @RequestParam(required = false) Long programId,
                                 Model model) {
        // Validate required fields
        if (username.isBlank() || password.isBlank() || email.isBlank() || fullName.isBlank() || studentId.isBlank()) {
            model.addAttribute("error", "All fields are required.");
            model.addAttribute("programs", programRepository.findAll());
            return "register";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists.");
            model.addAttribute("programs", programRepository.findAll());
            return "register";
        }
        // Save user
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role("ROLE_USER")
                .email(email)
                .build();
        if (programId != null) {
            Optional<Program> program = programRepository.findById(programId);
            program.ifPresent(user::setProgram);
        }
        user = userRepository.save(user);
        // Handle image upload
        String imagePath = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String filename = UUID.randomUUID() + "_" + StringUtils.cleanPath(profileImage.getOriginalFilename());
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(filename);
                profileImage.transferTo(filePath);
                imagePath = "/" + uploadDir + "/" + filename;
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload image.");
                model.addAttribute("programs", programRepository.findAll());
                return "register";
            }
        }
        // Save profile
        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(fullName)
                .studentId(studentId)
                .profileImage(imagePath)
                .build();
        userProfileRepository.save(profile);
        model.addAttribute("success", "Registration successful! Please login.");
        return "login";
    }
}