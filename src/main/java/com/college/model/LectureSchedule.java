package com.college.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "lecture_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "day_of_week")
    private String dayOfWeek; // e.g., MONDAY, TUESDAY

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    private String location;
}