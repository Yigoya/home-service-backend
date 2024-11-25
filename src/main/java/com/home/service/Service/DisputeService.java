package com.home.service.Service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.home.service.config.exceptions.GeneralException;
import com.home.service.dto.DisputeDTO;
import com.home.service.dto.DisputeRequest;
import com.home.service.dto.admin.AddressDTO;
import com.home.service.dto.admin.BookingDetailDTO;
import com.home.service.dto.admin.CustomerDTO;
import com.home.service.dto.admin.DisputeDetailDTO;
import com.home.service.dto.admin.TechnicianDTO;
import com.home.service.models.Dispute;
import com.home.service.models.Operator;
import com.home.service.models.Technician;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.DisputeRepository;
import com.home.service.repositories.OperatorRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import com.home.service.models.Address;
import com.home.service.models.Booking;
import com.home.service.models.CustomDetails;
import com.home.service.models.Customer;
import com.home.service.models.enums.DisputeStatus;
import com.home.service.models.enums.UserRole;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final OperatorRepository operatorRepository;

    public DisputeService(DisputeRepository disputeRepository, BookingRepository bookingRepository,
            UserService userService, OperatorRepository operatorRepository) {
        this.disputeRepository = disputeRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.operatorRepository = operatorRepository;
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

    @Transactional
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

        // CustomDetails User = userService.getCurrentUser();
        // if (User.getRole() == UserRole.ADMIN) {
        // Specification<Dispute> spec =
        // Specification.where(DisputeSpecification.hasCustomerName(customerName))
        // .and(DisputeSpecification.hasTechnicianName(technicianName))
        // .and(DisputeSpecification.hasStatus(status));

        // return disputeRepository.findAll(spec,
        // pageable).map(this::convertToDisputeDetailDTO);
        // } else if (User.getRole() == UserRole.OPERATOR) {
        // Operator operator = operatorRepository.findByUser_Id(User.getId())
        // .orElseThrow(() -> new EntityNotFoundException("Operator not found"));
        // Specification<Dispute> spec =
        // Specification.where(DisputeSpecification.hasCustomerName(customerName))
        // .and(DisputeSpecification.hasTechnicianName(technicianName))
        // .and(DisputeSpecification.hasStatus(status))
        // .and(DisputeSpecification.hasSubcity(operator.getAssignedRegion()));

        // return disputeRepository.findAll(spec,
        // pageable).map(this::convertToDisputeDetailDTO);
        // }
        // throw new GeneralException("Your Are Not Authorized");
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
        dto.setService(booking.getService().getTranslations().stream()
                .filter(t -> t.getLang().equals(booking.getTechnician().getUser().getPreferredLanguage()))
                .findFirst().get().getName());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setTimeSchedule(booking.getTimeSchedule());
        dto.setStatus(booking.getStatus());
        dto.setDescription(booking.getDescription());
        dto.setServiceLocation(convertToAddressDTO(booking.getServiceLocation()));
        return dto;
    }

    private AddressDTO convertToAddressDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setSubcity(address.getSubcity());
        dto.setWereda(address.getWereda());
        dto.setCountry(address.getCountry());
        dto.setLatitude(address.getLatitude());
        dto.setLongitude(address.getLongitude());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
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
