package com.home.service.Service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.home.service.dto.DisputeDTO;
import com.home.service.dto.DisputeRequest;
import com.home.service.dto.admin.BookingDetailDTO;
import com.home.service.dto.admin.CustomerDTO;
import com.home.service.dto.admin.DisputeDetailDTO;
import com.home.service.dto.admin.TechnicianDTO;
import com.home.service.models.Dispute;
import com.home.service.models.Technician;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.DisputeRepository;
import com.home.service.models.Booking;
import com.home.service.models.Customer;
import com.home.service.models.enums.DisputeStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final BookingRepository bookingRepository;

    public DisputeService(DisputeRepository disputeRepository, BookingRepository bookingRepository) {
        this.disputeRepository = disputeRepository;
        this.bookingRepository = bookingRepository;
    }

    public void submitDispute(DisputeRequest disputeRequest) {
        Dispute dispute = new Dispute();
        Booking booking = bookingRepository.findById(disputeRequest.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        dispute.setBooking(booking);
        dispute.setCustomer(booking.getCustomer());
        dispute.setTechnician(booking.getTechnician());
        dispute.setReason(disputeRequest.getTitle());
        dispute.setDescription(disputeRequest.getDescription());
        dispute.setStatus(DisputeStatus.PENDING);
        disputeRepository.save(dispute);
    }

    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }

    public List<DisputeDTO> getDisputesByCustomerId(Long customerId) {
        List<Dispute> disputes = disputeRepository.findByCustomerId(customerId);
        return disputes.stream()
                .map(dispute -> new DisputeDTO(dispute.getId(), dispute.getTechnician().getUser().getName(),
                        dispute.getReason(), dispute.getDescription(), dispute.getStatus(), dispute.getCreatedAt(),
                        dispute.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public Page<DisputeDetailDTO> getFilteredDisputes(String customerName, String technicianName, DisputeStatus status,
            Pageable pageable) {
        Specification<Dispute> spec = Specification.where(DisputeSpecification.hasCustomerName(customerName))
                .and(DisputeSpecification.hasTechnicianName(technicianName))
                .and(DisputeSpecification.hasStatus(status));

        return disputeRepository.findAll(spec, pageable).map(this::convertToDisputeDetailDTO);
    }

    private DisputeDetailDTO convertToDisputeDetailDTO(Dispute dispute) {
        DisputeDetailDTO dto = new DisputeDetailDTO();
        dto.setDisputeId(dispute.getId());
        dto.setReason(dispute.getReason());
        dto.setDescription(dispute.getDescription()); // Assuming Dispute entity has a description field
        dto.setCreatedAt(dispute.getCreatedAt());
        dto.setStatus(dispute.getStatus());
        dto.setBooking(convertToBookingDetailDTO(dispute.getBooking()));
        dto.setCustomer(convertToCustomerDTO(dispute.getCustomer()));
        dto.setTechnician(convertToTechnicianDTO(dispute.getTechnician()));
        return dto;
    }

    private BookingDetailDTO convertToBookingDetailDTO(Booking booking) {
        // Reuse method from BookingService or create a new one if needed
        BookingDetailDTO dto = new BookingDetailDTO();
        dto.setBookingId(booking.getId());
        dto.setService(booking.getService().getName());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setTimeSchedule(booking.getTimeSchedule());
        dto.setStatus(booking.getStatus());
        return dto;
    }

    private CustomerDTO convertToCustomerDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(customer.getId());
        dto.setName(customer.getUser().getName());
        dto.setEmail(customer.getUser().getEmail());
        dto.setPhoneNumber(customer.getUser().getPhoneNumber());
        return dto;
    }

    private TechnicianDTO convertToTechnicianDTO(Technician technician) {
        TechnicianDTO dto = new TechnicianDTO();
        dto.setTechnicianId(technician.getId());
        dto.setName(technician.getUser().getName());
        dto.setEmail(technician.getUser().getEmail());
        dto.setPhoneNumber(technician.getUser().getPhoneNumber());
        return dto;
    }
}
