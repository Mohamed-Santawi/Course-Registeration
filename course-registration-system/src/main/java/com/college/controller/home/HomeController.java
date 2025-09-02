package com.college.controller.home;

import com.college.model.User;
import com.college.model.Program;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.college.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.college.service.UserService;
import com.college.repository.UserProfileRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.college.model.UserProfile;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import com.college.repository.ProgramRepository;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserProfileRepository userProfileRepository;
    private final ProgramRepository programRepository;

    @Autowired(required = false)
    private AuthenticationManager authenticationManager;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        if (Boolean.TRUE.equals(session.getAttribute("loginSuccess"))) {
            model.addAttribute("loginSuccess", true);
            session.removeAttribute("loginSuccess");
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        List<String> programNames = List.of("Computer Science", "Electrical Engineering", "Business Administration");
        List<String> years = List.of("1st Year", "2nd Year", "3rd Year");
        model.addAttribute("programNames", programNames);
        model.addAttribute("years", years);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               @RequestParam String fullName,
                               @RequestParam(required = false) String studentId,
                               @RequestParam(required = false) String programName,
                               @RequestParam(required = false) String year,
                               @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
                               Model model, HttpSession session) {
        System.out.println("DEBUG: POST /register called");
        // Validate required fields
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            model.addAttribute("error", "All fields are required");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        // Password strength: min 8 chars, at least one letter and one number
        if (password.length() < 8 || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            model.addAttribute("error", "Password must be at least 8 characters long and contain at least one letter and one number.");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        // Email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            model.addAttribute("error", "Invalid email format.");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        // Full name: only letters and spaces, min 2 chars
        if (!fullName.matches("^[A-Za-z ]{2,}$")) {
            model.addAttribute("error", "Full Name must contain only letters and spaces (min 2 characters).");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        // Username: alphanumeric, 4-20 chars
        if (!username.matches("^[A-Za-z0-9]{4,20}$")) {
            model.addAttribute("error", "Username must be 4-20 characters, letters and numbers only.");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        // Validate studentId is numeric
        if (studentId == null || !studentId.matches("\\d+")) {
            model.addAttribute("error", "Student ID must be numeric.");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        // Validate programName is in valid list
        List<String> validProgramNames = programRepository.findAllOrderByNameAndYear().stream().map(Program::getName).distinct().toList();
        if (programName == null || !validProgramNames.contains(programName)) {
            model.addAttribute("error", "Invalid program selected.");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists. Please choose a different username.");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email already exists. Please use a different email address.");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        if (userProfileRepository.existsByStudentId(studentId)) {
            model.addAttribute("error", "Student ID already exists. Please use a different student ID.");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        // Find the program by name and year
        Program program = programRepository.findAllOrderByNameAndYear().stream()
            .filter(p -> p.getName().equals(programName) && p.getYear().equals(year))
            .findFirst().orElse(null);
        if (program == null) {
            model.addAttribute("error", "Selected program and year combination is invalid.");
            List<Program> allPrograms = programRepository.findAllOrderByNameAndYear();
            List<String> programNames = allPrograms.stream().map(Program::getName).distinct().toList();
            List<String> years = allPrograms.stream().map(Program::getYear).distinct().toList();
            model.addAttribute("programNames", programNames);
            model.addAttribute("years", years);
            return "register";
        }
        try {
            userService.registerUser(username, password, email, fullName, studentId, program.getId(), year, profileImage);
            userRepository.flush();
            User user = userRepository.findByUsername(username).orElseThrow();
            if (authenticationManager != null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
                try {
                    Authentication auth = authenticationManager.authenticate(authToken);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                    session.setAttribute("loginSuccess", true);
                } catch (Exception e) {
                    System.out.println("DEBUG: Auto-login failed: " + e.getMessage());
                }
            }
            model.addAttribute("success", "Registration successful");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String userProfile(Model model, @RequestParam(value = "success", required = false) String success, @RequestParam(value = "error", required = false) String error) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        String fullName = user.getUserProfile() != null ? user.getUserProfile().getFullName() : "";
        model.addAttribute("fullName", fullName);
        String studentId = user.getUserProfile() != null ? user.getUserProfile().getStudentId() : "";
        model.addAttribute("studentId", studentId);
        String program = user.getProgram() != null ? user.getProgram().getName() : "";
        String year = user.getProgram() != null ? user.getProgram().getYear() : "";
        model.addAttribute("program", program);
        model.addAttribute("year", year);
        String profileImage = (user.getUserProfile() != null && user.getUserProfile().getProfileImage() != null)
            ? "/profile-image/" + user.getUsername()
            : "/images/default-profile.png";
        model.addAttribute("profileImageUrl", profileImage);
        if (success != null) model.addAttribute("success", success);
        if (error != null) model.addAttribute("error", error);
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("fullName", user.getUserProfile() != null ? user.getUserProfile().getFullName() : "");
        model.addAttribute("email", user.getEmail());
        model.addAttribute("studentId", user.getUserProfile() != null ? user.getUserProfile().getStudentId() : "");
        model.addAttribute("program", user.getProgram() != null ? user.getProgram().getName() : "");
        model.addAttribute("year", user.getProgram() != null ? user.getProgram().getYear() : "");
        String profileImage = (user.getUserProfile() != null && user.getUserProfile().getProfileImage() != null)
            ? "/profile-image/" + user.getUsername()
            : "/images/default-profile.png";
        model.addAttribute("profileImageUrl", profileImage);

        // Add program names for dropdown
        List<String> programNames = programRepository.findAllOrderByNameAndYear().stream()
            .map(Program::getName).distinct().toList();
        model.addAttribute("programNames", programNames);

        return "profile_edit";
    }

    @PostMapping("/profile/edit")
    public String editProfile(@RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam String studentId,
                              @RequestParam String program,
                              @RequestParam String year,
                              @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
                              RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            redirectAttributes.addAttribute("error", "User not found or not logged in.");
            return "redirect:/profile";
        }
        // Email format (profile update)
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            redirectAttributes.addAttribute("error", "Invalid email format.");
            return "redirect:/profile";
        }
        // Full name: only letters and spaces, min 2 chars (profile update)
        if (!fullName.matches("^[A-Za-z ]{2,}$")) {
            redirectAttributes.addAttribute("error", "Full Name must contain only letters and spaces (min 2 characters).");
            return "redirect:/profile";
        }
        // Validate studentId is numeric
        if (studentId == null || !studentId.matches("\\d+")) {
            redirectAttributes.addAttribute("error", "Student ID must be numeric.");
            return "redirect:/profile";
        }
        // Validate program is in valid list
        List<String> validProgramNames = programRepository.findAllOrderByNameAndYear().stream().map(Program::getName).distinct().toList();
        if (program == null || !validProgramNames.contains(program)) {
            redirectAttributes.addAttribute("error", "Invalid program selected.");
            return "redirect:/profile";
        }

        // Check if email is being changed and if the new email already exists
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            redirectAttributes.addAttribute("error", "Email already exists. Please use a different email address.");
            return "redirect:/profile";
        }

        // Check if student ID is being changed and if the new student ID already exists
        String currentStudentId = user.getUserProfile() != null ? user.getUserProfile().getStudentId() : null;
        if (currentStudentId != null && !currentStudentId.equals(studentId) && userProfileRepository.existsByStudentId(studentId)) {
            redirectAttributes.addAttribute("error", "Student ID already exists. Please use a different student ID.");
            return "redirect:/profile";
        }

        try {
            // Update user fields
            user.setEmail(email);

            // Find and update the program association
            Program newProgram = programRepository.findAllOrderByNameAndYear().stream()
                .filter(p -> p.getName().equals(program) && p.getYear().equals(year))
                .findFirst().orElse(null);
            if (newProgram != null) {
                user.setProgram(newProgram);
            }

            // Update profile fields
            if (user.getUserProfile() != null) {
                user.getUserProfile().setFullName(fullName);
                user.getUserProfile().setStudentId(studentId);
                // Save new image if uploaded
                if (profileImage != null && !profileImage.isEmpty()) {
                    try {
                        byte[] imageBytes = profileImage.getBytes();
                        user.getUserProfile().setProfileImage(imageBytes);
                        user.getUserProfile().setImageContentType(profileImage.getContentType());
                    } catch (IOException e) {
                        redirectAttributes.addAttribute("error", "Failed to process image: " + e.getMessage());
                        return "redirect:/profile";
                    }
                }
            }
            userRepository.save(user);
            redirectAttributes.addAttribute("success", "Profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/profile-image/{username}")
    public ResponseEntity<byte[]> getProfileImageByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getUserProfile() == null || user.getUserProfile().getProfileImage() == null) {
            // Optionally, return a default image here
            return ResponseEntity.notFound().build();
        }
        // Optionally, detect content type
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(user.getUserProfile().getProfileImage());
    }

    @GetMapping("/help")
    public String help() {
        return "help";
    }

    @ModelAttribute
    public void addUserProfileToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null && user.getUserProfile() != null && user.getUserProfile().getProfileImage() != null) {
                model.addAttribute("profileImageUrl", "/profile-image/" + user.getUsername());
            } else {
                model.addAttribute("profileImageUrl", "/images/default-profile.png");
            }
        } else {
            model.addAttribute("profileImageUrl", "/images/default-profile.png");
        }
    }
}