package com.home.service.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyScheduleRequest {
    private LocalTime mondayStart;
    private LocalTime mondayEnd;
    private LocalTime tuesdayStart;
    private LocalTime tuesdayEnd;
    private LocalTime wednesdayStart;
    private LocalTime wednesdayEnd;
    private LocalTime thursdayStart;
    private LocalTime thursdayEnd;
    private LocalTime fridayStart;
    private LocalTime fridayEnd;
    private LocalTime saturdayStart;
    private LocalTime saturdayEnd;
    private LocalTime sundayStart;
    private LocalTime sundayEnd;
}
