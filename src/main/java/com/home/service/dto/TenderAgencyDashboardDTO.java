package com.home.service.dto;

import java.util.List;

import com.home.service.models.TenderAgencyProfile;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TenderAgencyDashboardDTO {
    private TenderAgencyProfile agencyProfile;
    private Long totalTenders;
    private Long activeTenders;
    private Long closedTenders;
    private List<TenderStats> tenderStats;
    private List<RecentActivity> recentActivities;
}
