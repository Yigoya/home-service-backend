package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.home.service.dto.AuthenticationResponse;
import com.home.service.dto.LoginRequest;
import com.home.service.dto.ProfileUpdateDTO;
import com.home.service.dto.ReviewDTO;
import com.home.service.dto.ServiceDTO;
import com.home.service.dto.SetServicePriceDTO;
import com.home.service.dto.TechnicianDTO;
import com.home.service.dto.TechnicianProfileDTO;
import com.home.service.dto.TechnicianResponse;
import com.home.service.dto.TechnicianSearchFilterDTO;
import com.home.service.dto.TechnicianSignupRequest;
import com.home.service.dto.TechnicianWeeklyScheduleResponse;
import com.home.service.dto.admin.TechnicianAddressDTO;
import com.home.service.dto.admin.TechnicianDetailDTO;
import com.home.service.dto.TechnicianWeeklyScheduleDTO;
import com.home.service.dto.records.SingleTechnician;
import com.home.service.config.JwtUtil;
import com.home.service.config.exceptions.EmailException;
import com.home.service.config.exceptions.GeneralException;
import com.home.service.models.Services;
import com.home.service.models.Technician;
import com.home.service.models.TechnicianAddress;
import com.home.service.models.TechnicianServicePrice;
import com.home.service.models.TechnicianWeeklySchedule;
import com.home.service.models.User;
import com.home.service.repositories.ReviewRepository;
import com.home.service.models.Address;
import com.home.service.models.Booking;
import com.home.service.models.CustomDetails;
import com.home.service.models.Operator;
import com.home.service.models.Review;
import com.home.service.models.enums.BookingStatus;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.TechnicianAddressRepository;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.OperatorRepository;
import com.home.service.repositories.TechnicianRepository;
import com.home.service.repositories.TechnicianServicePriceRepository;
import com.home.service.repositories.TechnicianWeeklyScheduleRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class TechnicianService {

        private final UserService userService;
        private final FileStorageService fileStorageService;
        private final TechnicianRepository technicianRepository;
        private final UserRepository userRepository;
        private final TechnicianAddressRepository technicianAddressRepository;
        private final JwtUtil jwtUtil;
        private ServiceRepository serviceRepository;
        private TechnicianWeeklyScheduleRepository technicianWeeklyScheduleRepository;
        private TechnicianServicePriceRepository technicianServicePriceRepository;
        private ReviewRepository reviewRepository;
        private BookingRepository bookingRepository;
        private OperatorRepository operatorRepository;
        private final BookingService bookingService;

        public TechnicianService(UserService userService, FileStorageService fileStorageService,
                        TechnicianRepository technicianRepository, UserRepository userRepository,
                        TechnicianAddressRepository technicianAddressRepository, JwtUtil jwtUtil,
                        ServiceRepository serviceRepository,
                        TechnicianWeeklyScheduleRepository technicianWeeklyScheduleRepository,
                        TechnicianServicePriceRepository technicianServicePriceRepository,
                        ReviewRepository reviewRepository, BookingRepository bookingRepository,
                        OperatorRepository operatorRepository, BookingService bookingService) {
                this.userService = userService;
                this.fileStorageService = fileStorageService;
                this.technicianRepository = technicianRepository;
                this.userRepository = userRepository;
                this.technicianAddressRepository = technicianAddressRepository;
                this.jwtUtil = jwtUtil;
                this.serviceRepository = serviceRepository;
                this.technicianWeeklyScheduleRepository = technicianWeeklyScheduleRepository;
                this.technicianServicePriceRepository = technicianServicePriceRepository;
                this.reviewRepository = reviewRepository;
                this.bookingRepository = bookingRepository;
                this.operatorRepository = operatorRepository;
                this.bookingService = bookingService;

        }

        public List<Technician> getAllTechnicians() {
                return technicianRepository.findAll();
        }

        public SingleTechnician getTechnicianById(Long id) {
                // Fetch the technician, or throw an exception if not found
                Technician technician = technicianRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

                // Fetch the weekly schedule and address details
                TechnicianWeeklySchedule schedule = technicianWeeklyScheduleRepository.findByTechnicianId(id);
                Optional<TechnicianAddress> technicianAddressOpt = technicianAddressRepository.findByTechnicianId(id);

                // Initialize city with default value if present, else null
                String city = technicianAddressOpt.map(TechnicianAddress::getCity)
                                .orElse("Addis Ababa");

                // Safely handle nullable fields like `completedJobs`
                List<Booking> bookings = bookingRepository.findAllByTechnician_Id(id);
                List<Review> reviews = reviewRepository.findAllByTechnicianId(id);
                List<ReviewDTO> reviewDTOs = reviews.stream().map(review -> new ReviewDTO(review))
                                .collect(Collectors.toList());
                // Construct and return the SingleTechnician record
                return new SingleTechnician(
                                technician.getId(),
                                technician.getUser().getName(),
                                technician.getUser().getEmail(),
                                city,
                                technician.getUser().getPhoneNumber(),
                                technician.getUser().getProfileImage(),
                                technician.getBio(),
                                technician.getUser().getRole().name(),
                                technician.getServices(),
                                Optional.ofNullable(technician.getRating()).orElse(0.0), // Handle null rating with
                                bookings.size(),
                                schedule != null ? new TechnicianWeeklyScheduleDTO(schedule) : null,
                                reviewDTOs);
        }

        public Set<Services> getServicesForTechnician(Long technicianId) {
                Technician technician = technicianRepository.findById(technicianId)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));
                return technician.getServices();
        }

        @Transactional
        public Technician addServiceToTechnician(Long technicianId, Long serviceId) {
                Technician technician = technicianRepository.findById(technicianId)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));
                Services service = serviceRepository.findById(serviceId)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
                technician.getServices().add(service);
                return technicianRepository.save(technician);
        }

        @Transactional
        public Technician removeServiceFromTechnician(Long technicianId, Long serviceId) {
                Technician technician = technicianRepository.findById(technicianId)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));
                Services service = serviceRepository.findById(serviceId)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
                technician.getServices().remove(service);
                return technicianRepository.save(technician);
        }

        @Transactional
        public String signupTechnician(TechnicianSignupRequest signupRequest) {
                // Check if the email is already in use
                if (userRepository.existsByEmail(signupRequest.getEmail())) {
                        throw new EmailException("Email already in use");
                }
                System.out.println(signupRequest.getServiceIds());
                List<Services> services = serviceRepository.findAllById(signupRequest.getServiceIds());

                // Create and save the User entity
                User user = new User();
                user.setName(signupRequest.getName());
                user.setEmail(signupRequest.getEmail());
                user.setPhoneNumber(signupRequest.getPhoneNumber());
                user.setPassword(signupRequest.getPassword()); // Remember to hash the password in production
                user.setRole(UserRole.TECHNICIAN);
                String profileImagePath = fileStorageService.storeFile(signupRequest.getProfileImage());
                user.setProfileImage(profileImagePath);
                userService.saveUser(user);

                // Create and save the Technician entity
                Technician technician = new Technician();
                technician.setUser(user);
                technician.setBio(signupRequest.getBio());

                // Retrieve and associate services with Technician
                technician.setServices(new HashSet<>(services));

                // Store profile image and set the path
                String idCardImagePath = fileStorageService.storeFile(signupRequest.getIdCardImage());
                technician.setIdCardImage(idCardImagePath);

                // Store documents and set the paths
                List<String> documentPaths = signupRequest.getDocuments().stream()
                                .map(fileStorageService::storeFile)
                                .collect(Collectors.toList());
                technician.setDocuments(documentPaths);

                // Save the Technician entity
                Technician newTechnician = technicianRepository.save(technician);
                System.out.println("technician = " + technician.toString());
                // Create and save TechnicianAddress
                TechnicianAddress technicianAddress = new TechnicianAddress();
                technicianAddress.setTechnician(newTechnician);
                technicianAddress.setStreet(signupRequest.getStreet());
                technicianAddress.setCity(signupRequest.getCity());
                technicianAddress.setSubcity(signupRequest.getSubcity());
                technicianAddress.setWereda(signupRequest.getWereda());
                technicianAddress.setCountry(signupRequest.getCountry());
                technicianAddress.setZipCode(signupRequest.getZipCode());
                technicianAddress.setLatitude(signupRequest.getLatitude());
                technicianAddress.setLongitude(signupRequest.getLongitude());

                newTechnician.getTechnicianAddresses().add(technicianAddressRepository.save(technicianAddress));
                return "Technician created successfully";
        }

        @Transactional
        public List<Technician> findTechniciansByService(Long serviceId) {

                return technicianRepository.findByServices(serviceRepository.findById(serviceId)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found")));
        }

        public List<Technician> searchTechnicians(String query, Double minPrice,
                        Double maxPrice, Double minRating,
                        String location) {
                return technicianRepository.findTechnicians(query);
        }

        public AuthenticationResponse loginTechnician(LoginRequest loginRequest) {
                return userService.authenticate(loginRequest);
        }

        @Transactional
        public TechnicianWeeklyScheduleResponse setWeeklySchedule(Long technicianId, LocalTime mondayStart,
                        LocalTime mondayEnd,
                        LocalTime tuesdayStart, LocalTime tuesdayEnd,
                        LocalTime wednesdayStart, LocalTime wednesdayEnd,
                        LocalTime thursdayStart, LocalTime thursdayEnd,
                        LocalTime fridayStart, LocalTime fridayEnd,
                        LocalTime saturdayStart, LocalTime saturdayEnd,
                        LocalTime sundayStart, LocalTime sundayEnd) {
                TechnicianWeeklySchedule schedule = technicianWeeklyScheduleRepository.findByTechnicianId(technicianId);
                Technician technician = technicianRepository.findById(technicianId)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));
                if (schedule == null) {
                        schedule = new TechnicianWeeklySchedule();
                        schedule.setTechnician(technician);
                }

                schedule.setMondayStart(mondayStart);
                schedule.setMondayEnd(mondayEnd);
                schedule.setTuesdayStart(tuesdayStart);
                schedule.setTuesdayEnd(tuesdayEnd);
                schedule.setWednesdayStart(wednesdayStart);
                schedule.setWednesdayEnd(wednesdayEnd);
                schedule.setThursdayStart(thursdayStart);
                schedule.setThursdayEnd(thursdayEnd);
                schedule.setFridayStart(fridayStart);
                schedule.setFridayEnd(fridayEnd);
                schedule.setSaturdayStart(saturdayStart);
                schedule.setSaturdayEnd(saturdayEnd);
                schedule.setSundayStart(sundayStart);
                schedule.setSundayEnd(sundayEnd);

                technicianWeeklyScheduleRepository.save(schedule);

                return new TechnicianWeeklyScheduleResponse(
                                technicianId,
                                mondayStart, mondayEnd,
                                tuesdayStart, tuesdayEnd,
                                wednesdayStart, wednesdayEnd,
                                thursdayStart, thursdayEnd,
                                fridayStart, fridayEnd,
                                saturdayStart, saturdayEnd,
                                sundayStart, sundayEnd);
        }

        // Retrieve a technician's weekly schedule
        public TechnicianWeeklySchedule getWeeklySchedule(Long technicianId) {
                return technicianWeeklyScheduleRepository.findByTechnicianId(technicianId);
        }

        @Transactional
        public List<Technician> filterTechnicians(
                        String name, Double minPrice, Double maxPrice, String city,
                        String subcity, String wereda, Double minLatitude, Double maxLatitude,
                        Double minLongitude, Double maxLongitude) {

                Specification<Technician> spec = TechnicianSpecification.getTechnicianFilter(
                                name, minPrice, maxPrice, city, subcity, wereda,
                                minLatitude, maxLatitude, minLongitude, maxLongitude);

                return technicianRepository.findAll(spec);
        }

        @Transactional
        public TechnicianServicePrice setServicePrice(SetServicePriceDTO setServicePriceDTO) {
                // Find the technician and service by their IDs
                Technician technician = technicianRepository.findById(setServicePriceDTO.getTechnicianId())
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

                Services service = serviceRepository.findById(setServicePriceDTO.getServiceId())
                                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

                // Check if there's already a price set for this technician-service combination
                TechnicianServicePrice technicianServicePrice = technicianServicePriceRepository
                                .findByTechnicianAndService(technician, service)
                                .orElse(new TechnicianServicePrice(technician, service, setServicePriceDTO.getPrice()));

                // Update the price if it already exists
                technicianServicePrice.setPrice(setServicePriceDTO.getPrice());

                // Save and return the updated/new TechnicianServicePrice
                return technicianServicePriceRepository.save(technicianServicePrice);
        }

        public TechnicianProfileDTO getTechnicianProfile(Long technicianId) {
                Technician technician = technicianRepository.findById(technicianId)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));
                TechnicianWeeklySchedule schedule = technicianWeeklyScheduleRepository.findByTechnicianId(technicianId);
                List<Map<String, Object>> calender = bookingService.getTechnicianSchedule(technicianId);
                TechnicianProfileDTO dto = new TechnicianProfileDTO();
                dto.setId(technician.getId());
                dto.setName(technician.getUser().getName());
                dto.setEmail(technician.getUser().getEmail());
                dto.setPhoneNumber(technician.getUser().getPhoneNumber());
                dto.setBio(technician.getBio());
                dto.setIdCardImage(technician.getIdCardImage());
                dto.setServices(technician.getServices().stream().map(Services::getName).collect(Collectors.toSet()));
                dto.setRating(technician.getRating());
                dto.setCompletedJobs(technician.getCompletedJobs());
                dto.setWeeklySchedule(schedule != null ? new TechnicianWeeklyScheduleDTO(schedule) : null);
                dto.setCalender(calender);
                return dto;
        }

        @Transactional
        public void updateTechnicianProfile(Long technicianId, ProfileUpdateDTO updateDTO) {
                Technician technician = technicianRepository.findById(technicianId)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));
                technician.getUser().setName(updateDTO.getName());
                technician.setBio(updateDTO.getBio());
                technicianRepository.save(technician);
        }

        public Technician saveTechnician(Technician technician) {
                return technicianRepository.save(technician);
        }

        public Page<TechnicianProfileDTO> getAllTechnicians(Pageable pageable) {
                Page<Technician> technicians = technicianRepository.findAll(pageable);
                List<TechnicianProfileDTO> technicianProfiles = technicians.stream()
                                .map(technician -> new TechnicianProfileDTO(technician))
                                .collect(Collectors.toList());
                return new PageImpl<>(technicianProfiles, pageable, technicians.getTotalElements());
        }

        public void deleteTechnician(Long id) {
                Technician technician = technicianRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));
                technicianRepository.delete(technician);
        }

        public List<TechnicianDTO> getTopFiveTechniciansByRating() {
                return technicianRepository.findAll().stream()
                                .sorted((t1, t2) -> Double.compare(
                                                Optional.ofNullable(t2.getRating()).orElse(0.0),
                                                Optional.ofNullable(t1.getRating()).orElse(0.0)))
                                .limit(5)
                                .map(technician -> new TechnicianDTO(technician))
                                .collect(Collectors.toList());
        }

        @Transactional
        public boolean toggleAvailability(Long technicianId) {
                Technician technician = technicianRepository.findById(technicianId)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

                // Toggle the availability: if "Available" switch to "Unavailable" and vice
                // versa
                String currentAvailability = technician.getAvailability();
                String newAvailability = currentAvailability.equals("Available") ? "Unavailable" : "Available";
                technician.setAvailability(newAvailability);

                technicianRepository.save(technician);
                return newAvailability.equals("Available");
        }

        @Transactional
        public Page<Technician> findAvailableTechniciansByServiceAndSchedule(Long serviceId, LocalDate date,
                        LocalTime time, Pageable pageable) {
                String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
                System.out.println("dayOfWeek = " + dayOfWeek);
                return technicianRepository.findAvailableTechniciansByServiceAndSchedule(serviceId, dayOfWeek, time,
                                pageable);
        }

        public Page<Technician> filterTechnicians(
                        String name,
                        Double minPrice,
                        Double maxPrice,
                        String city,
                        String subcity,
                        String wereda,
                        Double minLatitude,
                        Double maxLatitude,
                        Double minLongitude,
                        Double maxLongitude,
                        Boolean availability,
                        LocalTime time,
                        String dayOfWeek,
                        Long serviceId,
                        Pageable pageable) {

                Specification<Technician> specification = TechnicianSpecificationSchedule.filterTechnicians(
                                name, minPrice, maxPrice, city, subcity, wereda, minLatitude, maxLatitude, minLongitude,
                                maxLongitude,
                                availability, time, dayOfWeek, serviceId);

                return technicianRepository.findAll(specification, pageable);
        }

        public Page<TechnicianDetailDTO> getFilteredTechnicians(String name, Pageable pageable) {
                // CustomDetails User = userService.getCurrentUser();
                // if (User.getRole() == UserRole.ADMIN) {
                // Specification<Technician> spec = Specification
                // .where(TechnicianAdminSpecification.hasName(name));
                // return technicianRepository.findAll(spec,
                // pageable).map(this::convertToTechnicianDetailDTO);
                // } else if (User.getRole() == UserRole.OPERATOR) {
                // Operator operator = operatorRepository.findByUser_Id(User.getId())
                // .orElseThrow(() -> new EntityNotFoundException("Operator not found"));

                // Specification<Technician> spec =
                // Specification.where(TechnicianAdminSpecification.hasName(name))

                // .and(TechnicianAdminSpecification.hasSubcity(operator.getAssignedRegion()));
                // return technicianRepository.findAll(spec,
                // pageable).map(this::convertToTechnicianDetailDTO);
                // }
                // throw new GeneralException("Your Are Not Authorized");
                Specification<Technician> spec = Specification
                                .where(TechnicianAdminSpecification.hasName(name));
                return technicianRepository.findAll(spec, pageable).map(this::convertToTechnicianDetailDTO);

        }

        private TechnicianDetailDTO convertToTechnicianDetailDTO(Technician technician) {
                TechnicianDetailDTO dto = new TechnicianDetailDTO();
                User user = technician.getUser();
                dto.setTechnicianId(technician.getId());
                dto.setName(user.getName());
                dto.setEmail(user.getEmail());
                dto.setPhoneNumber(user.getPhoneNumber());
                dto.setProfileImage(user.getProfileImage());
                dto.setBio(technician.getBio());
                dto.setRating(technician.getRating());
                dto.setServices(technician.getServices().stream().map(ServiceDTO::new).collect(Collectors.toSet()));
                dto.setCompletedJobs(technician.getCompletedJobs());
                Optional<TechnicianAddress> primaryAddress = technicianAddressRepository
                                .findByTechnicianId(technician.getId());
                primaryAddress.ifPresent(
                                technicianAddress -> dto.setAddress(convertToTechnicianAddressDTO(technicianAddress)));

                return dto;
        }

        private TechnicianAddressDTO convertToTechnicianAddressDTO(TechnicianAddress address) {
                TechnicianAddressDTO dto = new TechnicianAddressDTO();
                dto.setStreet(address.getStreet());
                dto.setCity(address.getCity());
                dto.setSubcity(address.getSubcity());
                dto.setWereda(address.getWereda());
                dto.setCountry(address.getCountry());
                dto.setZipCode(address.getZipCode());
                dto.setLatitude(address.getLatitude());
                dto.setLongitude(address.getLongitude());
                return dto;
        }
}
