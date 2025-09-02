package com.college.repository;

import com.college.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query("SELECT p FROM Program p ORDER BY p.name, p.year")
    List<Program> findAllOrderByNameAndYear();
}