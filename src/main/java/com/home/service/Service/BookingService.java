package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.home.service.dto.AddressDTO;
import com.home.service.dto.AnswerDTO;
import com.home.service.dto.AnswerRequest;
import com.home.service.dto.BookingRequest;
import com.home.service.dto.BookingResponseDTO;
import com.home.service.dto.BookingUpdateRequest;
import com.home.service.dto.QuestionDTO;
import com.home.service.dto.QuestionOptionDTO;
import com.home.service.dto.QuestionWithAnswerDTO;
import com.home.service.models.Address;
import com.home.service.models.Answer;
import com.home.service.models.Booking;
import com.home.service.models.CustomDetails;
import com.home.service.models.Customer;
import com.home.service.models.Operator;
import com.home.service.models.Question;
import com.home.service.models.Review;
import com.home.service.models.Services;
import com.home.service.models.Technician;
import com.home.service.models.Transaction;
import com.home.service.models.User;
import com.home.service.models.enums.BookingStatus;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.models.enums.NotificationType;
import com.home.service.models.enums.TransactionType;
import com.home.service.repositories.AddressRepository;
import com.home.service.repositories.AnswerRepository;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.OperatorRepository;
import com.home.service.repositories.QuestionRepository;
import com.home.service.repositories.ReviewRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.TechnicianRepository;
import com.home.service.repositories.TransactionRepository;
import com.home.service.dto.ReviewDTO;
import com.home.service.dto.SingleBookingResponseDTO;
import com.home.service.dto.admin.BookingDetailDTO;
import com.home.service.dto.admin.CustomerDTO;
import com.home.service.dto.admin.TechnicianDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    TechnicianRepository technicianRepository;

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking saveBooking(Booking booking) {
        // Retrieve and set Customer, Technician, and Service entities
        Customer customer = customerRepository.findById(booking.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Technician technician = technicianRepository.findById(booking.getTechnicianId())
                .orElseThrow(() -> new IllegalArgumentException("Technician not found"));
        Services service = serviceRepository.findById(booking.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        booking.setCustomer(customer);
        booking.setTechnician(technician);
        booking.setService(service);

        // Set up Address
        Address address = booking.getServiceLocation(); // assuming serviceLocation is already populated
        address.setCustomer(customer); // Set the customer association on the address
        Address savedAddress = addressRepository.save(address); // Save address first

        // Set the saved Address to the booking and save booking
        booking.setServiceLocation(savedAddress);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking createBooking(BookingRequest request) {
        // Fetch Customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        // Fetch Technician
        Technician technician = technicianRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

        // Fetch Service
        Services service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        // Check if customer has enough coins
        int serviceFee = service.getServiceFee().intValue();
        if (customer.getCoinBalance() < serviceFee) {
            throw new IllegalArgumentException("Insufficient coins for booking");
        }

        // Deduct coins from customer balance
        customer.setCoinBalance(customer.getCoinBalance() - serviceFee);
        customerRepository.save(customer);

        System.out.println(service.getTranslations().toString());

        // Record transaction for coin usage
        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(-serviceFee);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription("Coins used for booking service: " + service.getTranslations().stream()
                .filter(t -> t.getLang().equals(customer.getUser().getPreferredLanguage()))
                .findFirst()
                .orElse(service.getTranslations().stream()
                        .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Translation not found for English")))
                .getName());
        transaction.setTransactionType(TransactionType.USAGE);
        transactionRepository.save(transaction);

        // Create Address from request
        Address address = new Address();
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setSubcity(request.getSubcity());
        address.setWereda(request.getWereda());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setCustomer(customer);
        address = addressRepository.save(address);

        // Create Booking
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setTechnician(technician);
        booking.setService(service);
        booking.setScheduledDate(request.getScheduledDate());
        booking.setServiceLocation(address);
        booking.setDescription(request.getDescription());
        booking.setTimeSchedule(request.getTimeSchedule());
        booking.setStatus(BookingStatus.PENDING);

        Booking newBooking = bookingRepository.save(booking);

        String title = "New booking request";
        String message = customer.getUser().getName() + " requested a booking for " + service.getTranslations().stream()
                .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH)).findFirst().get().getName();

        if (technician.getUser().getPreferredLanguage() == EthiopianLanguage.AMHARIC) {
            title = "አዲስ በክፍል ጥያቄ";
            message = customer.getUser().getName() + " እንዴት እንደሚሰራ " + service.getTranslations().stream()
                    .filter(t -> t.getLang().equals(EthiopianLanguage.AMHARIC)).findFirst().get().getName()
                    + " እንዴት እንደሚሰራ";
        } else if (technician.getUser().getPreferredLanguage() == EthiopianLanguage.TIGRINYA) {
            title = "አዲስ በክፍል ጥያቄ";
            message = customer.getUser().getName() + " እንዴት እንደሚሰራ " + service.getTranslations().stream()
                    .filter(t -> t.getLang().equals(EthiopianLanguage.TIGRINYA)).findFirst().get().getName()
                    + " እንዴት እንደሚሰራ";
        }

        // Send Notification to Technician
        notificationService.sendNotification(
                technician.getUser().getId(),
                title, message,
                NotificationType.BOOKING_REQUEST,
                newBooking.getId());

        return newBooking;
    }

    @Transactional
    public Booking updateBooking(Long bookingId, BookingUpdateRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        if (request.getScheduledDate() != null) {
            booking.setScheduledDate(request.getScheduledDate());
        }

        if (request.getStreet() != null || request.getCity() != null) {
            Address address = booking.getServiceLocation() != null ? booking.getServiceLocation() : new Address();
            address.setStreet(request.getStreet());
            address.setCity(request.getCity());
            address.setSubcity(request.getSubcity());
            address.setWereda(request.getWereda());
            address.setState(request.getState());
            address.setCountry(request.getCountry());
            address.setZipCode(request.getZipCode());
            address.setLatitude(request.getLatitude());
            address.setLongitude(request.getLongitude());
            booking.setServiceLocation(addressRepository.save(address));
        }

        if (request.getTimeSchedule() != null) {
            booking.setTimeSchedule(request.getTimeSchedule());
        }

        if (request.getStatus() != null) {
            booking.setStatus(request.getStatus());
        }

        booking.setDescription(request.getDescription());

        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        // Fetch the booking by ID
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        if (newStatus == BookingStatus.CUSTOMER_STARTED && booking.getStatus() == BookingStatus.TECHNICIAN_STARTED) {
            booking.setStatus(BookingStatus.STARTED);
        } else if (newStatus == BookingStatus.TECHNICIAN_STARTED
                && booking.getStatus() == BookingStatus.CUSTOMER_STARTED) {
            booking.setStatus(BookingStatus.STARTED);
        } else {
            booking.setStatus(newStatus);
        }

        if (newStatus == BookingStatus.ACCEPTED) {
            notificationService.sendNotification(booking.getCustomer().getUser().getId(), "Booking accepted",
                    booking.getTechnician().getUser().getName() + " has accepted your booking",
                    NotificationType.BOOKING_ACCEPTANCE, booking.getId());
        } else if (newStatus == BookingStatus.CUSTOMER_STARTED) {
            notificationService.sendNotification(booking.getTechnician().getUser().getId(), "Customer started",
                    booking.getCustomer().getUser().getName() + " has started the booking",
                    NotificationType.BOOKING_START, booking.getId());
        } else if (newStatus == BookingStatus.TECHNICIAN_STARTED) {
            notificationService.sendNotification(booking.getCustomer().getUser().getId(), "Technician started",
                    booking.getTechnician().getUser().getName() + " has started the booking",
                    NotificationType.BOOKING_START, booking.getId());
        } else if (booking.getStatus() == BookingStatus.STARTED) {
            notificationService.sendNotification(booking.getCustomer().getUser().getId(), "Booking started",
                    "has started the booking",
                    NotificationType.BOOKING_START, booking.getId());
            notificationService.sendNotification(booking.getTechnician().getUser().getId(), "Booking started",
                    "has started the booking",
                    NotificationType.BOOKING_START, booking.getId());
        } else if (newStatus == BookingStatus.PENDING) {
            notificationService.sendNotification(booking.getTechnician().getUser().getId(), "Booking pending",
                    booking.getCustomer().getUser().getName() + " has canceled the booking",
                    NotificationType.BOOKING_CANCELLATION, booking.getId());
        } else if (newStatus == BookingStatus.DENIED) {
            notificationService.sendNotification(booking.getCustomer().getUser().getId(), "Booking denied",
                    booking.getTechnician().getUser().getName() + " has denied your booking",
                    NotificationType.BOOKING_DENIAL, booking.getId());
        } else if (newStatus == BookingStatus.COMPLETED) {
            notificationService.sendNotification(booking.getCustomer().getUser().getId(), "Booking completed",
                    booking.getTechnician().getUser().getName() + " has completed your booking",
                    NotificationType.BOOKING_COMPLETE, booking.getId());
        } else if (newStatus == BookingStatus.CANCELED) {
            notificationService.sendNotification(booking.getTechnician().getUser().getId(), "Booking canceled",
                    booking.getCustomer().getUser().getName() + " has canceled the booking",
                    NotificationType.BOOKING_CANCELLATION, booking.getId());
        } else if (newStatus == BookingStatus.CONFIRMED) {
            notificationService.sendNotification(booking.getCustomer().getUser().getId(), "Booking confirmed",
                    booking.getTechnician().getUser().getName() + " has confirmed the booking",
                    NotificationType.BOOKING_CONFIRMATION, booking.getId());
        }

        // Save and return the updated booking
        return bookingRepository.save(booking);
    }

    @Transactional
    public Page<BookingResponseDTO> getBookingsForCustomer(Long customerId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByCustomerIdOrderByStatusPriority(customerId,
                Pageable.unpaged());
        return bookings.map(this::convertToDTO);
    }

    @Transactional
    public Page<BookingResponseDTO> getBookingsForTechnician(Long technicianId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByTechnicianIdOrderByStatusPriority(technicianId,
                Pageable.unpaged());
        return bookings.map(this::convertToDTO);
    }

    // Convert Booking entity to BookingResponseDTO
    private BookingResponseDTO convertToDTO(Booking booking) {
        ReviewDTO review = reviewRepository.findByBookingId(booking.getId())
                .map(ReviewDTO::new)
                .orElse(null);
        return new BookingResponseDTO(
                booking.getId(),
                booking.getCustomer().getUser().getName(),
                booking.getTechnician().getUser().getName(),
                booking.getTechnician().getUser().getProfileImage(),
                booking.getCustomer().getUser().getProfileImage(),
                booking.getService().getTranslations().stream()
                        .filter(t -> t.getLang().equals(booking.getTechnician().getUser().getPreferredLanguage()))
                        .findFirst().get().getName(),
                booking.getScheduledDate(),
                booking.getStatus(),
                booking.getDescription(),
                booking.getServiceLocation() != null ? new AddressDTO(booking.getServiceLocation()) : null,
                review);

    }

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Set status to CANCELED
        booking.setStatus(BookingStatus.CANCELED);

        // Save the updated booking
        bookingRepository.save(booking);
    }

    public void acceptBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Set status to ACCEPTED
        booking.setStatus(BookingStatus.ACCEPTED);

        // Save the updated booking
        bookingRepository.save(booking);
    }

    public void denyBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Set status to DENIED
        booking.setStatus(BookingStatus.DENIED);

        // Save the updated booking
        bookingRepository.save(booking);
    }

    public void completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Set status to COMPLETED
        booking.setStatus(BookingStatus.COMPLETED);

        // Save the updated booking
        bookingRepository.save(booking);
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    @Transactional
    public void saveAnswers(AnswerRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        List<Answer> answers = request.getAnswers().stream().map(answerRequest -> {
            Question question = questionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException("Question not found"));

            Answer answer = new Answer();
            answer.setQuestion(question);
            answer.setBooking(booking);
            answer.setCustomer(customer);
            answer.setResponse(answerRequest.getResponse());
            return answer;
        }).collect(Collectors.toList());

        answerRepository.saveAll(answers);
    }

    @Transactional
    public SingleBookingResponseDTO getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Customer customer = booking.getCustomer();
        Technician technician = booking.getTechnician();
        Services service = booking.getService();
        Address serviceLocation = booking.getServiceLocation();
        List<Question> questions = questionRepository.findByServices(service);

        // Convert to DTO
        SingleBookingResponseDTO dto = new SingleBookingResponseDTO();
        dto.setBookingId(booking.getId());
        dto.setCustomerId(customer.getId());
        dto.setCustomerName(customer.getUser().getName());
        dto.setTechnicianId(technician.getId());
        dto.setTechnicianName(technician.getUser().getName());
        dto.setServiceId(service.getId());
        dto.setServiceName(service.getTranslations().stream()
                .filter(t -> t.getLang().equals(booking.getTechnician().getUser().getPreferredLanguage()))
                .findFirst().get().getName());
        dto.setServiceDescription(service.getTranslations().stream()
                .filter(t -> t.getLang().equals(booking.getTechnician().getUser().getPreferredLanguage()))
                .findFirst().get().getName());
        dto.setScheduledDate(booking.getScheduledDate());
        dto.setStatus(booking.getStatus().name());
        dto.setTotalCost(booking.getTotalCost());
        dto.setTimeSchedule(booking.getTimeSchedule());

        // Convert Address to DTO
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(serviceLocation.getId());
        addressDTO.setStreet(serviceLocation.getStreet());
        addressDTO.setCity(serviceLocation.getCity());
        addressDTO.setSubcity(serviceLocation.getSubcity());
        addressDTO.setWereda(serviceLocation.getWereda());
        addressDTO.setState(serviceLocation.getState());
        addressDTO.setCountry(serviceLocation.getCountry());
        addressDTO.setZipCode(serviceLocation.getZipCode());
        addressDTO.setLatitude(serviceLocation.getLatitude());
        addressDTO.setLongitude(serviceLocation.getLongitude());
        dto.setServiceLocation(addressDTO);

        // Convert questions to DTOs
        List<QuestionWithAnswerDTO> questionDTOs = questions.stream().map(q -> {
            QuestionWithAnswerDTO questionDTO = new QuestionWithAnswerDTO();
            questionDTO.setQuestionId(q.getId());
            questionDTO.setText(q.getText());
            questionDTO.setType(q.getType().name());

            // Map options for each question
            List<QuestionOptionDTO> optionsDTO = q.getOptions().stream().map(o -> {
                QuestionOptionDTO optionDTO = new QuestionOptionDTO();
                optionDTO.setOptionId(o.getId());
                optionDTO.setOptionText(o.getOptionText());
                return optionDTO;
            }).toList();

            questionDTO.setOptions(optionsDTO);

            // Map answers for each question
            List<AnswerDTO> answerDTOs = answerRepository.findByQuestionAndBooking(q, booking).stream().map(a -> {
                AnswerDTO answerDTO = new AnswerDTO();
                answerDTO.setAnswerId(a.getId());
                answerDTO.setResponse(a.getResponse());
                answerDTO.setCustomerId(a.getCustomer().getId());
                answerDTO.setCustomerName(a.getCustomer().getUser().getName());
                return answerDTO;
            }).toList();

            questionDTO.setAnswers(answerDTOs);

            return questionDTO;
        }).toList();

        dto.setQuestions(questionDTOs);

        return dto;
    }

    @Transactional
    public Page<BookingDetailDTO> getFilteredBookings(String name, String service, BookingStatus status,
            Pageable pageable) {
        // CustomDetails User = userService.getCurrentUser();
        // Operator operator = operatorRepository.findByUser_Id(User.getId())
        // .orElseThrow(() -> new EntityNotFoundException("Operator not found"));

        Specification<Booking> spec = Specification.where(BookingSpecification.hasCustomerName(name))
                .and(BookingSpecification.hasService(service))
                .and(BookingSpecification.hasStatus(status))
                .and(BookingSpecification.orderByCreatedAtDesc());
        // .and(BookingSpecification.hasSubcity(operator.getAssignedRegion()));

        return bookingRepository.findAll(spec, pageable).map(this::convertToBookingDetailDTO);
    }

    private BookingDetailDTO convertToBookingDetailDTO(Booking booking) {
        BookingDetailDTO dto = new BookingDetailDTO();
        dto.setBookingId(booking.getId());
        dto.setDescription("Description of booking"); // Populate with actual description as per requirement
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setService(booking.getService().getTranslations().stream()
                .filter(t -> t.getLang().equals(booking.getTechnician().getUser().getPreferredLanguage()))
                .findFirst().get().getName());
        dto.setStatus(booking.getStatus());
        dto.setServiceLocation(convertToAddressDTO(booking.getServiceLocation()));
        dto.setCustomer(convertToCustomerDTO(booking.getCustomer()));
        dto.setTechnician(convertToTechnicianDTO(booking.getTechnician()));
        dto.setReview(
                reviewRepository.findByBookingId(booking.getId()).map(this::convertToAdminReviewDTO).orElse(null));
        return dto;
    }

    private CustomerDTO convertToCustomerDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        User user = customer.getUser();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfileImage(user.getProfileImage());
        return dto;
    }

    private TechnicianDTO convertToTechnicianDTO(Technician technician) {
        TechnicianDTO dto = new TechnicianDTO();
        User user = technician.getUser();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfileImage(user.getProfileImage());
        return dto;
    }

    private com.home.service.dto.admin.ReviewDTO convertToAdminReviewDTO(Review review) {
        com.home.service.dto.admin.ReviewDTO dto = new com.home.service.dto.admin.ReviewDTO();
        dto.setRating(review.getRating());
        dto.setReviewText(review.getReviewText());
        return dto;
    }

    private com.home.service.dto.admin.AddressDTO convertToAddressDTO(Address address) {
        com.home.service.dto.admin.AddressDTO dto = new com.home.service.dto.admin.AddressDTO();
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setSubcity(address.getSubcity());
        dto.setWereda(address.getWereda());
        dto.setState(address.getState());
        dto.setCountry(address.getCountry());
        dto.setZipCode(address.getZipCode());
        dto.setLatitude(address.getLatitude());
        dto.setLongitude(address.getLongitude());
        return dto;
    }

    @Transactional
    public List<Map<String, Object>> getTechnicianSchedule(Long technicianId) {
        List<Booking> bookings = bookingRepository.findAllByTechnician_Id(technicianId);

        return bookings.stream()
                .filter(booking -> booking.getScheduledDate() != null)
                .map(booking -> {
                    Map<String, Object> schedule = new HashMap<>();
                    schedule.put("id", booking.getId());
                    schedule.put("title", booking.getService().getTranslations().stream()
                            .filter(t -> t.getLang().equals(booking.getTechnician().getUser().getPreferredLanguage()))
                            .findFirst().get().getName()); // Use service name as the title
                    schedule.put("start", booking.getScheduledDate());
                    schedule.put("description", booking.getDescription());
                    schedule.put("location", booking.getServiceLocation().getSubcity());

                    // Calculate end time based on estimatedDuration or default to 2 hours
                    LocalDateTime end = booking.getScheduledDate();
                    try {
                        LocalTime estimatedDuration = booking.getService().getEstimatedDuration();
                        if (estimatedDuration != null) {
                            end = end.plusHours(estimatedDuration.getHour()).plusMinutes(estimatedDuration.getMinute());
                        } else {
                            end = end.plusHours(2); // Default duration
                        }
                    } catch (Exception e) {
                        end = end.plusHours(2); // Fallback in case of parsing error
                    }
                    schedule.put("end", end);

                    return schedule;
                }).collect(Collectors.toList());
    }
}
