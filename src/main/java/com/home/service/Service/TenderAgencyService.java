package com.home.service.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.home.service.dto.AgencyRegistrationRequest;
import com.home.service.dto.RecentActivity;
import com.home.service.dto.TenderAgencyDashboardDTO;
import com.home.service.dto.TenderStats;
import com.home.service.models.Tender;
import com.home.service.models.TenderAgencyProfile;
import com.home.service.models.User;
import com.home.service.models.enums.AccountStatus;
import com.home.service.models.enums.TenderStatus;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.TenderAgencyProfileRepository;
import com.home.service.repositories.TenderRepository;
import com.home.service.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class TenderAgencyService {
        private final TenderRepository tenderRepository;
        private final TenderAgencyProfileRepository agencyRepository;
        private final NotificationService notificationService;
        private final UserService userService;
        private final UserRepository userRepository;

        public TenderAgencyService(TenderRepository tenderRepository,
                        TenderAgencyProfileRepository agencyRepository,
                        NotificationService notificationService, UserService userService,
                        UserRepository userRepository) {
                this.tenderRepository = tenderRepository;
                this.agencyRepository = agencyRepository;
                this.notificationService = notificationService;
                this.userService = userService;
                this.userRepository = userRepository;
        }

        public TenderAgencyDashboardDTO getDashboard(Long agencyId) {
                TenderAgencyProfile agency = agencyRepository.findById(agencyId)
                                .orElseThrow(() -> new RuntimeException("Agency not found"));

                TenderAgencyDashboardDTO dashboard = new TenderAgencyDashboardDTO();
                dashboard.setAgencyProfile(agency);

                dashboard.setTotalTenders(tenderRepository.countByAgencyId(agencyId));
                dashboard.setActiveTenders(tenderRepository.countByAgencyIdAndStatus(agencyId, TenderStatus.OPEN));
                dashboard.setClosedTenders(tenderRepository.countByAgencyIdAndStatus(agencyId, TenderStatus.CLOSED));

                dashboard.setTenderStats(getTenderStats(agencyId));
                dashboard.setRecentActivities(getRecentActivities(agencyId));

                return dashboard;
        }

        private List<TenderStats> getTenderStats(Long agencyId) {
                // Get stats for the last 6 months
                LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

                // Fetch tenders from the repository
                List<Tender> tenders = tenderRepository.findTendersByAgencyIdAndDate(agencyId, sixMonthsAgo);

                // Group tenders by date (truncated to LocalDate) and status, then count
                Map<LocalDate, Map<TenderStatus, Long>> statsMap = tenders.stream()
                                .collect(Collectors.groupingBy(
                                                tender -> tender.getDatePosted().toLocalDate(), // Truncate to date
                                                Collectors.groupingBy(
                                                                Tender::getStatus,
                                                                Collectors.counting() // Count tenders per group
                                                )));

                // Transform the grouped data into a list of TenderStats
                return statsMap.entrySet().stream()
                                .flatMap(dateEntry -> dateEntry.getValue().entrySet().stream()
                                                .map(statusEntry -> new TenderStats(
                                                                dateEntry.getKey(), // LocalDate
                                                                statusEntry.getValue(), // Count (Long)
                                                                statusEntry.getKey() // TenderStatus
                                                )))
                                .sorted((stats1, stats2) -> stats2.getDate().compareTo(stats1.getDate())) // Sort by
                                                                                                          // date DESC
                                .collect(Collectors.toList());
        }

        // private List<TenderStats> getTenderStats(Long agencyId) {
        // // Get stats for the last 6 months
        // LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        // return tenderRepository.findTenderStatsByAgencyId(agencyId, sixMonthsAgo);
        // }

        private List<RecentActivity> getRecentActivities(Long agencyId) {
                // Implement logic to fetch recent activities (e.g., from an audit log)
                return Collections.emptyList(); // Placeholder
        }

        @Transactional
        public Tender updateTenderStatus(Long tenderId, TenderStatus status, Long agencyId) {
                Tender tender = tenderRepository.findByIdAndAgencyId(tenderId, agencyId)
                                .orElseThrow(() -> new RuntimeException("Tender not found"));

                tender.setStatus(status);
                Tender updatedTender = tenderRepository.save(tender);

                // Notify subscribed customers
                notificationService.notifySubscribers(updatedTender);

                return updatedTender;
        }

        public TenderAgencyProfile registerAgency(AgencyRegistrationRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Email already exists");
                }

                if (agencyRepository.existsByCompanyName(request.getCompanyName())) {
                        throw new RuntimeException("Company name already exists");
                }

                User user = new User();
                user.setEmail(request.getEmail());
                user.setPassword(request.getPassword());
                user.setName(request.getContactPerson());
                user.setPhoneNumber(request.getPhoneNumber());
                user.setStatus(AccountStatus.ACTIVE);

                user.setRole(UserRole.AGENCY);
                userService.saveUser(user);

                TenderAgencyProfile agency = new TenderAgencyProfile();
                agency.setUser(user);
                agency.setCompanyName(request.getCompanyName());
                agency.setTinNumber(request.getTinNumber());
                agency.setContactPerson(request.getContactPerson());

                return agencyRepository.save(agency);
        }
}