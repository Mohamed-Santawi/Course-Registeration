package com.college.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToMany
    @JoinTable(
        name = "program_courses",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "program_id")
    )
    private Set<Program> programs = new HashSet<>();

    @Column(name = "year")
    private String year; // e.g. "A", "B", "C"

    @Column(name = "type")
    private String type; // e.g. "Mandatory", "Elective"

    @Column(nullable = false)
    private String semester;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LectureSchedule> schedules;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments;

    @Column
    private String instructor;

    public String getFirstProgramName() {
        if (programs != null && !programs.isEmpty()) {
            return programs.iterator().next().getName();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return id != null && id.equals(course.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", type='" + type + '\'' +
                ", semester='" + semester + '\'' +
                '}';
    }
}