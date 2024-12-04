package com.home.service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "technician_weekly_schedule")
public class TechnicianWeeklySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    @Column(name = "monday_start")
    private LocalTime mondayStart;

    @Column(name = "monday_end")
    private LocalTime mondayEnd;

    @Column(name = "tuesday_start")
    private LocalTime tuesdayStart;

    @Column(name = "tuesday_end")
    private LocalTime tuesdayEnd;

    @Column(name = "wednesday_start")
    private LocalTime wednesdayStart;

    @Column(name = "wednesday_end")
    private LocalTime wednesdayEnd;

    @Column(name = "thursday_start")
    private LocalTime thursdayStart;

    @Column(name = "thursday_end")
    private LocalTime thursdayEnd;

    @Column(name = "friday_start")
    private LocalTime fridayStart;

    @Column(name = "friday_end")
    private LocalTime fridayEnd;

    @Column(name = "saturday_start")
    private LocalTime saturdayStart;

    @Column(name = "saturday_end")
    private LocalTime saturdayEnd;

    @Column(name = "sunday_start")
    private LocalTime sundayStart;

    @Column(name = "sunday_end")
    private LocalTime sundayEnd;
}
