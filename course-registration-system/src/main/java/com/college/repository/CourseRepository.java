package com.college.repository;

import com.college.model.Course;
import com.college.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c JOIN c.programs p WHERE p = :program AND (c.year = :year OR c.year < :year)")
    List<Course> findByProgramAndYear(@Param("program") Program program, @Param("year") String year);

    @Query("SELECT c FROM Course c JOIN c.programs p WHERE p = :program")
    List<Course> findByProgram(@Param("program") Program program);

    @Query("SELECT c FROM Course c JOIN c.programs p WHERE p.name = :programName")
    List<Course> findAllByProgramName(@Param("programName") String programName);

    @Query("SELECT c FROM Course c JOIN c.programs p WHERE p.name = :programName AND c.year = :year AND c.semester = :semester AND (LOWER(c.title) LIKE %:q% OR LOWER(c.code) LIKE %:q%)")
    List<Course> searchCourses(@Param("programName") String programName, @Param("year") String year, @Param("semester") String semester, @Param("q") String q);

    @Query("SELECT c FROM Course c JOIN c.programs p WHERE p.id = :programId AND c.year = :year AND c.semester = :semester")
    List<Course> findByProgramYearSemester(@Param("programId") Long programId, @Param("year") String year, @Param("semester") String semester);

    Optional<Course> findByCode(String code);

    @Query("SELECT c FROM Course c JOIN c.programs p WHERE p.name = :programName AND c.year = :year AND c.semester = :semester")
    List<Course> findByProgramNameAndYearAndSemester(@Param("programName") String programName, @Param("year") String year, @Param("semester") String semester);
}