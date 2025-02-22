package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.home.service.dto.AgencyBookingDTO;
import com.home.service.dto.AgencyDashboardDTO;
import com.home.service.dto.AgencyProfileDTO;
import com.home.service.dto.AgencyProfileRequest;
import com.home.service.dto.AgencySearchCriteria;
import com.home.service.dto.AgencyServiceRequest;
import com.home.service.dto.ServiceDTO;
import com.home.service.models.AgencyBooking;
import com.home.service.models.AgencyProfile;
import com.home.service.models.AgencyProfileServices;
import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.Services;
import com.home.service.models.User;
import com.home.service.models.enums.AccountStatus;
import com.home.service.models.enums.BookingStatus;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.models.enums.UserRole;
import com.home.service.models.enums.VerificationStatus;
import com.home.service.repositories.AgencyBookingRepository;
import com.home.service.repositories.AgencyProfileRepository;
import com.home.service.repositories.AgencyProfileServicesRepository;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgencyProfileService {

        @Autowired
        private AgencyProfileRepository agencyProfileRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private FileStorageService fileStorageService;

        @Autowired
        private ServiceRepository serviceRepository;

        @Autowired
        private AgencyBookingRepository bookingRepository;

        @Autowired
        private ServiceCategoryRepository serviceCategoryRepository;

        @Autowired
        private AgencyProfileServicesRepository agencyProfileServicesRepository;

        public AgencyProfileDTO createAgencyProfile(AgencyProfileRequest agencyProfileDTO) throws IOException {
                // Fetch the service by ID
                Services service = serviceRepository.findById(agencyProfileDTO.getServiceId())
                                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
                // Create User
                User user = new User();
                user.setName(agencyProfileDTO.getName());
                user.setPhoneNumber(agencyProfileDTO.getPhoneNumber());
                user.setEmail(agencyProfileDTO.getEmail());
                user.setPassword(passwordEncoder.encode(agencyProfileDTO.getPassword()));
                user.setRole(UserRole.AGENCY);
                user.setStatus(AccountStatus.INACTIVE); // Inactive until verified by admin
                user = userRepository.save(user);
                System.out.println(agencyProfileDTO.getCompanyPhoneNumber());
                // Create Agency Profile
                AgencyProfile agencyProfile = new AgencyProfile();
                agencyProfile.setUser(user);
                agencyProfile.setBusinessName(agencyProfileDTO.getBusinessName());
                agencyProfile.setDescription(agencyProfileDTO.getDescription());
                agencyProfile.setAddress(agencyProfileDTO.getAddress());
                agencyProfile.setCity(agencyProfileDTO.getCity());
                agencyProfile.setState(agencyProfileDTO.getState());
                agencyProfile.setZip(agencyProfileDTO.getZip());
                agencyProfile.setCountry(agencyProfileDTO.getCountry());
                agencyProfile.setPhone(agencyProfileDTO.getCompanyPhoneNumber());
                agencyProfile.setWebsite(agencyProfileDTO.getWebsite());
                agencyProfile.setService(service);
                String documentPath = fileStorageService.storeFile(agencyProfileDTO.getDocument());

                agencyProfile.setDocument(documentPath); // Save document

                if (agencyProfileDTO.getImage() != null) {
                        String imagePath = fileStorageService.storeFile(agencyProfileDTO.getImage());
                        agencyProfile.setImage(imagePath); // Save image
                }

                return new AgencyProfileDTO(agencyProfileRepository.save(agencyProfile));
        }

        public List<AgencyProfileDTO> getAllPendingAgencies() {
                return agencyProfileRepository.findByVerificationStatus(VerificationStatus.PENDING)
                                .stream()
                                .map(AgencyProfileDTO::new)
                                .collect(Collectors.toList());
        }

        public void verifyAgency(Long agencyId, VerificationStatus status) {
                AgencyProfile agencyProfile = agencyProfileRepository.findById(agencyId)
                                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));
                agencyProfile.setVerificationStatus(status);
                agencyProfileRepository.save(agencyProfile);

                // Activate user account if approved
                if (status == VerificationStatus.APPROVED) {
                        User user = agencyProfile.getUser();
                        user.setStatus(AccountStatus.ACTIVE);
                        userRepository.save(user);
                }
        }

        public List<AgencyProfileDTO> getAllAgencies() {
                return agencyProfileRepository.findAll()
                                .stream()
                                .map(AgencyProfileDTO::new)
                                .collect(Collectors.toList());
        }

        public AgencyProfileDTO getAgencyById(Long id) {
                AgencyProfile agencyProfile = agencyProfileRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));

                // Fetch the services associated with the agency
                List<Services> services = agencyProfileServicesRepository.findByAgencyProfile(agencyProfile)
                                .stream()
                                .map(AgencyProfileServices::getService)
                                .collect(Collectors.toList());

                // Convert the services to DTOs if needed
                List<ServiceDTO> serviceDTOs = services.stream()
                                .map(service -> new ServiceDTO(service, EthiopianLanguage.ENGLISH))
                                .collect(Collectors.toList());

                // Create the AgencyProfileDTO and set the services
                AgencyProfileDTO agencyProfileDTO = new AgencyProfileDTO(agencyProfile);
                agencyProfileDTO.setServices(serviceDTOs);

                return agencyProfileDTO;
        }

        public AgencyProfileDTO updateAgencyProfile(Long id, AgencyProfileRequest agencyProfileRequest) {
                AgencyProfile agencyProfile = agencyProfileRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));

                agencyProfile.setBusinessName(agencyProfileRequest.getBusinessName());
                agencyProfile.setDescription(agencyProfileRequest.getDescription());
                agencyProfile.setAddress(agencyProfileRequest.getAddress());
                agencyProfile.setCity(agencyProfileRequest.getCity());
                agencyProfile.setState(agencyProfileRequest.getState());
                agencyProfile.setZip(agencyProfileRequest.getZip());
                agencyProfile.setCountry(agencyProfileRequest.getCountry());
                agencyProfile.setPhone(agencyProfileRequest.getCompanyPhoneNumber());
                agencyProfile.setWebsite(agencyProfileRequest.getWebsite());

                AgencyProfile updatedAgencyProfile = agencyProfileRepository.save(agencyProfile);
                return new AgencyProfileDTO(updatedAgencyProfile);
        }

        public void deleteAgencyProfile(Long id) {
                AgencyProfile agencyProfile = agencyProfileRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));
                agencyProfileRepository.delete(agencyProfile);
        }

        public List<AgencyProfileDTO> searchAgencies(AgencySearchCriteria criteria) {
                return agencyProfileRepository.searchAgencies(criteria)
                                .stream()
                                .map(AgencyProfileDTO::new)
                                .collect(Collectors.toList());
        }

        @Transactional
        public String addService(Long agencyId, AgencyServiceRequest agencyServiceRequest) {
                AgencyProfile agencyProfile = agencyProfileRepository.findById(agencyId)
                                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));

                ServiceCategory category = serviceCategoryRepository.findById(4L)
                                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
                Services service = new Services();
                service.setCategory(category);
                service.setEstimatedDuration(agencyServiceRequest.getEstimatedDuration());
                service.setServiceFee(agencyServiceRequest.getServiceFee());

                if (agencyServiceRequest.getIcon() != null) {
                        String icon = fileStorageService.storeFile(agencyServiceRequest.getIcon());
                        service.setIcon(icon);
                }

                // Create the translation and link it to the service
                ServiceTranslation translation = new ServiceTranslation();
                translation.setLang(agencyServiceRequest.getLang());
                translation.setName(agencyServiceRequest.getName());
                translation.setDescription(agencyServiceRequest.getDescription());
                translation.setService(service);
                service.getTranslations().add(translation);
                serviceRepository.save(service); // Save the service

                // Create the AgencyProfileServices entity to link the service with the agency
                AgencyProfileServices agencyProfileServices = new AgencyProfileServices();
                agencyProfileServices.setAgencyProfile(agencyProfile);
                agencyProfileServices.setService(service);
                agencyProfileServicesRepository.save(agencyProfileServices);

                return "Service added successfully with ID: " + service.getId();
        }

        @Transactional
        public String addExistingServiceToAgency(Long agencyId, Long serviceId) {
                AgencyProfile agencyProfile = agencyProfileRepository.findById(agencyId)
                                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));

                Services service = serviceRepository.findById(serviceId)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

                AgencyProfileServices agencyProfileServices = new AgencyProfileServices();
                agencyProfileServices.setAgencyProfile(agencyProfile);
                agencyProfileServices.setService(service);

                agencyProfileServicesRepository.save(agencyProfileServices);

                return "Service added to agency successfully with ID: " + service.getId();
        }

        // Remove a service from an agency
        public void removeService(Long agencyId, Long serviceId) {
                agencyProfileRepository.findById(agencyId)
                                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));

                Services service = serviceRepository.findById(serviceId)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

                if (!service.getAgency().getId().equals(agencyId)) {
                        throw new IllegalArgumentException("Service does not belong to the agency");
                }

                serviceRepository.delete(service); // Delete the service
        }

        // Get all bookings for an agency
        public List<AgencyBookingDTO> getAgencyBookings(Long agencyId) {
                AgencyProfile agencyProfile = agencyProfileRepository.findById(agencyId)
                                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));

                return bookingRepository.findByAgency_Id(agencyProfile.getId())
                                .stream()
                                .map(AgencyBookingDTO::new)
                                .collect(Collectors.toList());
        }

        public AgencyDashboardDTO getAgencyDashboard(Long agencyId) {
                AgencyProfile agencyProfile = agencyProfileRepository.findById(agencyId)
                                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));
                AgencyProfileDTO agencyProfileDTO = new AgencyProfileDTO(agencyProfile);

                List<Services> servicesList = serviceRepository.findByAgency_IdOrderByIdAsc(agencyProfile.getId());
                List<ServiceDTO> services = servicesList.stream()
                                .map(service -> new ServiceDTO(service, EthiopianLanguage.ENGLISH))
                                .collect(Collectors.toList());
                List<AgencyBooking> bookingsList = bookingRepository.findByAgency_Id(agencyProfile.getId());
                List<AgencyBookingDTO> bookings = bookingsList.stream()
                                .map(AgencyBookingDTO::new)
                                .collect(Collectors.toList());

                Double totalRevenue = bookings.stream()
                                .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
                                .mapToDouble(AgencyBookingDTO::getTotalCost)
                                .sum();

                Long totalBookings = (long) bookings.size();
                Long pendingBookings = bookings.stream()
                                .filter(booking -> booking.getStatus() == BookingStatus.PENDING)
                                .count();
                Long completedBookings = bookings.stream()
                                .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
                                .count();

                AgencyDashboardDTO dashboardDTO = new AgencyDashboardDTO();
                dashboardDTO.setAgencyProfile(agencyProfileDTO);
                dashboardDTO.setServices(services);
                dashboardDTO.setBookings(bookings);
                dashboardDTO.setTotalRevenue(totalRevenue);
                dashboardDTO.setTotalBookings(totalBookings);
                dashboardDTO.setPendingBookings(pendingBookings);
                dashboardDTO.setCompletedBookings(completedBookings);

                return dashboardDTO;
        }

        public List<AgencyProfileDTO> findByServiceId(Long serviceId) {
                return agencyProfileRepository.findByServiceId(serviceId)
                                .stream()
                                .map(AgencyProfileDTO::new)
                                .collect(Collectors.toList());
        }
}