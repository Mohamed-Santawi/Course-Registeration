package com.college.service;

import com.college.model.*;
import com.college.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private ProgramRepository programRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        String username = "testuser";
        String password = "password";
        String email = "test@univ.edu";
        String fullName = "Test User";
        String studentId = "S123";
        Long programId = 1L;
        Program program = Program.builder().id(programId).name("CS").year("1st").build();
        when(programRepository.findById(programId)).thenReturn(Optional.of(program));
        when(passwordEncoder.encode(password)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        User user = userService.registerUser(username, password, email, fullName, studentId, programId, "1st Year", null);
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals("encoded", user.getPassword());
        assertEquals(program, user.getProgram());
        verify(userProfileRepository).save(any(UserProfile.class));
    }
}