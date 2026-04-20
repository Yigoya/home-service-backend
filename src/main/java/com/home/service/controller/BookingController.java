package com.home.service.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.AnswerService;
import com.home.service.Service.BookingService;
import com.home.service.Service.QuestionService;
import com.home.service.dto.AnswerDTO;
import com.home.service.dto.AnswerRequest;
import com.home.service.dto.BookingRequest;
import com.home.service.dto.BookingResponseDTO;
import com.home.service.dto.BookingUpdateRequest;
import com.home.service.dto.UpdateBookingStatusDTO;
import com.home.service.models.Booking;
import com.home.service.models.CustomDetails;
import com.home.service.models.Question;
import com.home.service.models.Technician;
import com.home.service.models.enums.BookingStatus;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.TechnicianRepository;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/request")
    public ResponseEntity<Map<String, Long>> requestBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        Booking booking = bookingService.createBooking(bookingRequest);
        Map<String, Long> response = new HashMap<>();
        response.put("bookingId", booking.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/{bookingId}")
    public ResponseEntity<String> updateBooking(
            @PathVariable Long bookingId,
            @RequestBody BookingUpdateRequest updateRequest) {
        Booking booking = bookingService.updateBooking(bookingId, updateRequest);
        return new ResponseEntity<>("Booking updated successfully with ID: " + booking.getId(), HttpStatus.OK);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/update-status")
    public ResponseEntity<String> updateBookingStatus(
            @Valid @RequestBody UpdateBookingStatusDTO updateBookingStatusDTO,
            @AuthenticationPrincipal CustomDetails currentUser) {
        Booking booking = enforceBookingStatusAccessAndGetBooking(currentUser, updateBookingStatusDTO.getBookingId());
        enforceStatusActionAccess(currentUser, booking, updateBookingStatusDTO.getStatus());

        // Call the service to update the booking status
        Booking updatedBooking = bookingService.updateBookingStatus(updateBookingStatusDTO.getBookingId(),
                updateBookingStatusDTO.getStatus());

        // Return success message
        return ResponseEntity.status(HttpStatus.OK).body("Booking status updated to " + updatedBooking.getStatus());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<BookingResponseDTO>> getBookingsForCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponseDTO> bookings = bookingService.getBookingsForCustomer(customerId, pageable);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Endpoint for technicians to view their bookings.
     * URL: POST /api/bookings/technician/{technicianId}
     * Response: 200 OK with list of bookings sorted by status priority.
     */
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<Page<BookingResponseDTO>> getBookingsForTechnician(
            @PathVariable Long technicianId,
            @AuthenticationPrincipal CustomDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        enforceTechnicianBookingsReadAccess(currentUser, technicianId);
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponseDTO> bookings = bookingService.getBookingsForTechnician(technicianId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getAllQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/answer")
    public ResponseEntity<String> submitAnswers(@Valid @RequestBody AnswerRequest request) {
        bookingService.saveAnswers(request);
        return ResponseEntity.status(201).body("Answers submitted successfully");
    }

    @GetMapping("/answers")
    public ResponseEntity<List<AnswerDTO>> getAnswers(@RequestParam Long bookingId, @RequestParam Long customerId) {
        List<AnswerDTO> answers = answerService.getNecessaryAnswerData(bookingId, customerId);
        return ResponseEntity.ok(answers);
    }

    private Booking enforceBookingStatusAccessAndGetBooking(CustomDetails currentUser, Long bookingId) {
        if (isPrivileged(currentUser)) {
            return bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Booking not found"));
        }

        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException("You are not authorized to modify this booking");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Booking not found"));

        Long customerUserId = booking.getCustomer() != null && booking.getCustomer().getUser() != null
                ? booking.getCustomer().getUser().getId()
                : null;
        Long technicianUserId = booking.getTechnician() != null && booking.getTechnician().getUser() != null
                ? booking.getTechnician().getUser().getId()
                : null;

        boolean isCustomerOwner = currentUser.getRole() == UserRole.CUSTOMER && currentUser.getId().equals(customerUserId);
        boolean isTechnicianOwner = currentUser.getRole() == UserRole.TECHNICIAN
                && currentUser.getId().equals(technicianUserId);

        if (!isCustomerOwner && !isTechnicianOwner) {
            throw new AccessDeniedException("You are not authorized to modify this booking");
        }

        return booking;
    }

    private void enforceStatusActionAccess(CustomDetails currentUser, Booking booking, BookingStatus requestedStatus) {
        if (isPrivileged(currentUser)) {
            return;
        }

        UserRole role = currentUser != null ? currentUser.getRole() : null;
        if (role == null) {
            throw new AccessDeniedException("You are not authorized to modify this booking status");
        }

        if (role == UserRole.TECHNICIAN) {
            // Technician can only execute technician-side workflow actions.
            if (requestedStatus != BookingStatus.ACCEPTED
                    && requestedStatus != BookingStatus.DENIED
                    && requestedStatus != BookingStatus.TECHNICIAN_STARTED
                    && requestedStatus != BookingStatus.CONFIRMED
                    && requestedStatus != BookingStatus.COMPLETED) {
                throw new AccessDeniedException("Technician is not allowed to set this status");
            }

            Long technicianUserId = booking.getTechnician() != null && booking.getTechnician().getUser() != null
                    ? booking.getTechnician().getUser().getId()
                    : null;

            if (technicianUserId == null || !technicianUserId.equals(currentUser.getId())) {
                throw new AccessDeniedException("Technician is not assigned to this booking");
            }
            return;
        }

        if (role == UserRole.CUSTOMER) {
            // Customer can only execute customer-side workflow actions.
            if (requestedStatus != BookingStatus.CUSTOMER_STARTED
                    && requestedStatus != BookingStatus.CANCELED
                    && requestedStatus != BookingStatus.PENDING) {
                throw new AccessDeniedException("Customer is not allowed to set this status");
            }

            Long customerUserId = booking.getCustomer() != null && booking.getCustomer().getUser() != null
                    ? booking.getCustomer().getUser().getId()
                    : null;

            if (customerUserId == null || !customerUserId.equals(currentUser.getId())) {
                throw new AccessDeniedException("Customer does not own this booking");
            }
            return;
        }

        throw new AccessDeniedException("You are not authorized to modify this booking status");
    }

    private void enforceTechnicianBookingsReadAccess(CustomDetails currentUser, Long technicianId) {
        if (isPrivileged(currentUser)) {
            return;
        }

        if (currentUser == null || currentUser.getId() == null || currentUser.getRole() != UserRole.TECHNICIAN) {
            throw new AccessDeniedException("You are not authorized to view these bookings");
        }

        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Technician not found"));

        if (technician.getUser() == null || !currentUser.getId().equals(technician.getUser().getId())) {
            throw new AccessDeniedException("You are not authorized to view these bookings");
        }
    }

    private boolean isPrivileged(CustomDetails currentUser) {
        if (currentUser == null || currentUser.getRole() == null) {
            return false;
        }
        return currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.OPERATOR;
    }

}
