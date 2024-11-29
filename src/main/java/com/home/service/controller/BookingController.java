package com.home.service.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.home.service.models.Question;
import com.home.service.models.QuestionRequest;

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

    @PostMapping("/request")
    public ResponseEntity<Map<String, Long>> requestBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        Booking booking = bookingService.createBooking(bookingRequest);
        Map<String, Long> response = new HashMap<>();
        response.put("bookingId", booking.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<String> updateBooking(
            @PathVariable Long bookingId,
            @RequestBody BookingUpdateRequest updateRequest) {
        Booking booking = bookingService.updateBooking(bookingId, updateRequest);
        return new ResponseEntity<>("Booking updated successfully with ID: " + booking.getId(), HttpStatus.OK);
    }

    @PutMapping("/update-status")
    public ResponseEntity<String> updateBookingStatus(
            @Valid @RequestBody UpdateBookingStatusDTO updateBookingStatusDTO) {
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
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

}
