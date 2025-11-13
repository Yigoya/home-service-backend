package com.home.service.dto;

public class AgencyStatisticsResponse {
    private Long totalTenders;
    private Long openTenders;
    private Long closedTenders;
    private Long cancelledTenders;
    private String verificationStatus;

    public AgencyStatisticsResponse(long totalTenders, long openTenders, long closedTenders, long cancelledTenders,
            String verifiedStatus) {
        this.totalTenders = totalTenders;
        this.openTenders = openTenders;
        this.closedTenders = closedTenders;
        this.cancelledTenders = cancelledTenders;
        this.verificationStatus = verifiedStatus;
    }

    // Getters and Setters
    public Long getTotalTenders() {
        return totalTenders;
    }

    public void setTotalTenders(Long totalTenders) {
        this.totalTenders = totalTenders;
    }

    public Long getOpenTenders() {
        return openTenders;
    }

    public void setOpenTenders(Long openTenders) {
        this.openTenders = openTenders;
    }

    public Long getClosedTenders() {
        return closedTenders;
    }

    public void setClosedTenders(Long closedTenders) {
        this.closedTenders = closedTenders;
    }

    public Long getCancelledTenders() {
        return cancelledTenders;
    }

    public void setCancelledTenders(Long cancelledTenders) {
        this.cancelledTenders = cancelledTenders;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
}
