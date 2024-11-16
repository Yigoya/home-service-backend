package com.home.service.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.home.service.models.TechnicianWeeklySchedule;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianWeeklyScheduleDTO {
    private Long id;
    private Long technicianId;
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

    public TechnicianWeeklyScheduleDTO(TechnicianWeeklySchedule schedule) {

        // Initialize fields from the schedule object
        this.id = schedule.getId();
        this.technicianId = schedule.getTechnician().getId();
        this.mondayStart = schedule.getMondayStart();
        this.mondayEnd = schedule.getMondayEnd();
        this.tuesdayStart = schedule.getTuesdayStart();
        this.tuesdayEnd = schedule.getTuesdayEnd();
        this.wednesdayStart = schedule.getWednesdayStart();
        this.wednesdayEnd = schedule.getWednesdayEnd();
        this.thursdayStart = schedule.getThursdayStart();
        this.thursdayEnd = schedule.getThursdayEnd();
        this.fridayStart = schedule.getFridayStart();
        this.fridayEnd = schedule.getFridayEnd();
        this.saturdayStart = schedule.getSaturdayStart();
        this.saturdayEnd = schedule.getSaturdayEnd();
        this.sundayStart = schedule.getSundayStart();
        this.sundayEnd = schedule.getSundayEnd();

    }
}
