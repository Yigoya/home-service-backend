package com.home.service.dto;

import jakarta.validation.constraints.Pattern;

public class OpeningHoursDTO {
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String mondayOpen;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String mondayClose;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String tuesdayOpen;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String tuesdayClose;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String wednesdayOpen;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String wednesdayClose;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String thursdayOpen;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String thursdayClose;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String fridayOpen;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String fridayClose;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String saturdayOpen;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String saturdayClose;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String sundayOpen;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    private String sundayClose;

    // Getters and Setters
    public String getMondayOpen() { return mondayOpen; }
    public void setMondayOpen(String mondayOpen) { this.mondayOpen = mondayOpen; }
    
    public String getMondayClose() { return mondayClose; }
    public void setMondayClose(String mondayClose) { this.mondayClose = mondayClose; }
    
    public String getTuesdayOpen() { return tuesdayOpen; }
    public void setTuesdayOpen(String tuesdayOpen) { this.tuesdayOpen = tuesdayOpen; }
    
    public String getTuesdayClose() { return tuesdayClose; }
    public void setTuesdayClose(String tuesdayClose) { this.tuesdayClose = tuesdayClose; }
    
    public String getWednesdayOpen() { return wednesdayOpen; }
    public void setWednesdayOpen(String wednesdayOpen) { this.wednesdayOpen = wednesdayOpen; }
    
    public String getWednesdayClose() { return wednesdayClose; }
    public void setWednesdayClose(String wednesdayClose) { this.wednesdayClose = wednesdayClose; }
    
    public String getThursdayOpen() { return thursdayOpen; }
    public void setThursdayOpen(String thursdayOpen) { this.thursdayOpen = thursdayOpen; }
    
    public String getThursdayClose() { return thursdayClose; }
    public void setThursdayClose(String thursdayClose) { this.thursdayClose = thursdayClose; }
    
    public String getFridayOpen() { return fridayOpen; }
    public void setFridayOpen(String fridayOpen) { this.fridayOpen = fridayOpen; }
    
    public String getFridayClose() { return fridayClose; }
    public void setFridayClose(String fridayClose) { this.fridayClose = fridayClose; }
    
    public String getSaturdayOpen() { return saturdayOpen; }
    public void setSaturdayOpen(String saturdayOpen) { this.saturdayOpen = saturdayOpen; }
    
    public String getSaturdayClose() { return saturdayClose; }
    public void setSaturdayClose(String saturdayClose) { this.saturdayClose = saturdayClose; }
    
    public String getSundayOpen() { return sundayOpen; }
    public void setSundayOpen(String sundayOpen) { this.sundayOpen = sundayOpen; }
    
    public String getSundayClose() { return sundayClose; }
    public void setSundayClose(String sundayClose) { this.sundayClose = sundayClose; }
}
