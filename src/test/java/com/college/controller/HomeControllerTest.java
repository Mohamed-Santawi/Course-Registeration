package com.college.controller;

import com.college.model.Program;
import com.college.repository.ProgramRepository;
import com.college.repository.UserProfileRepository;
import com.college.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest
class HomeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserProfileRepository userProfileRepository;
    @MockBean
    private ProgramRepository programRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void testRegisterValidation() throws Exception {
        when(programRepository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                .param("username", "")
                .param("password", "")
                .param("email", "")
                .param("fullName", "")
                .param("studentId", ""))
                .andExpect(MockMvcResultMatchers.model().attributeExists("error"))
                .andExpect(MockMvcResultMatchers.view().name("register"));
    }

    @Test
    void testRegisterSuccess() throws Exception {
        when(programRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                .param("username", "testuser")
                .param("password", "password")
                .param("email", "test@univ.edu")
                .param("fullName", "Test User")
                .param("studentId", "S123"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("success"))
                .andExpect(MockMvcResultMatchers.view().name("login"));
    }
}