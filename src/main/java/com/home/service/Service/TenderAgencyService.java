package com.home.service.Service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.config.exceptions.EmailException;
import com.home.service.dto.AgencyStatisticsResponse;
import com.home.service.dto.TenderAgencyRegistrationRequest;
import com.home.service.dto.AuthenticationResponse;
import com.home.service.dto.TenderAgencyUpdateRequest;
import com.home.service.dto.TenderAgencyResponse;
import com.home.service.models.Tender;
import com.home.service.models.TenderAgencyProfile;
import com.home.service.models.User;
import com.home.service.models.enums.TenderStatus;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.TenderAgencyProfileRepository;
import com.home.service.repositories.TenderRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

// TenderAgencyService.java
@Service
@RequiredArgsConstructor
public class TenderAgencyService {

	private final TenderAgencyProfileRepository tenderAgencyProfileRepository;
	private final UserRepository userRepository;
	private final FileStorageService fileStorageService;
	private final PasswordEncoder passwordEncoder;
	private final TenderRepository tenderRepository;
	private final ServiceRepository servicesRepository;
	private final UserService userService;

	public AuthenticationResponse registerAgency(TenderAgencyRegistrationRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new EmailException("Email already in use");
		}
		User user = new User();
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setName(request.getCompanyName());
		user.setPhoneNumber(request.getContactPerson());
		user.setRole(UserRole.AGENCY);

		User savedUser = userRepository.save(user);

		TenderAgencyProfile agency = new TenderAgencyProfile();
		agency.setUser(savedUser);
		agency.setCompanyName(request.getCompanyName());
		agency.setTinNumber(request.getTinNumber());
		agency.setWebsite(request.getWebsite());
		agency.setContactPerson(request.getContactPerson());
		agency.setVerifiedStatus("PENDING");

		TenderAgencyProfile savedAgency = tenderAgencyProfileRepository.save(agency);

		// Return login-like auth payload for newly registered agency user
		return userService.buildAuthenticationResponse(savedUser);
	}

	public com.home.service.dto.TenderAgencyResponse getAgencyProfile(Long agencyId) {
		TenderAgencyProfile agency = tenderAgencyProfileRepository.findById(agencyId)
				.orElseThrow(() -> new EntityNotFoundException("Agency not found"));
		return mapToAgencyResponse(agency);
	}

    public com.home.service.dto.TenderAgencyResponse getAgencyProfileByUserId(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		TenderAgencyProfile agency = tenderAgencyProfileRepository.findByUser(user)
				.orElseThrow(() -> new EntityNotFoundException("Agency not found"));

		return mapToAgencyResponse(agency);
	}

	public com.home.service.dto.TenderAgencyResponse updateAgencyProfile(Long agencyId, TenderAgencyUpdateRequest request) {
		TenderAgencyProfile agency = tenderAgencyProfileRepository.findById(agencyId)
				.orElseThrow(() -> new EntityNotFoundException("Agency not found"));

		agency.setCompanyName(request.getCompanyName());
		agency.setWebsite(request.getWebsite());
		agency.setContactPerson(request.getContactPerson());

		TenderAgencyProfile updatedAgency = tenderAgencyProfileRepository.save(agency);
		return mapToAgencyResponse(updatedAgency);
	}

	public String uploadBusinessLicense(Long agencyId, MultipartFile file) {
		TenderAgencyProfile agency = tenderAgencyProfileRepository.findById(agencyId)
				.orElseThrow(() -> new EntityNotFoundException("Agency not found"));

		String filePath = fileStorageService.storeFile(file);
		agency.setBusinessLicensePath(filePath);
		tenderAgencyProfileRepository.save(agency);
		return filePath;
	}

	public AgencyStatisticsResponse getAgencyStatistics(Long agencyId) {
		TenderAgencyProfile agency = tenderAgencyProfileRepository.findById(agencyId)
				.orElseThrow(() -> new EntityNotFoundException("Agency not found"));

		List<Tender> tenders = agency.getTenders();
		long openTenders = tenders.stream().filter(t -> t.getStatus() == TenderStatus.OPEN).count();
		long closedTenders = tenders.stream().filter(t -> t.getStatus() == TenderStatus.CLOSED).count();
		long cancelledTenders = tenders.stream().filter(t -> t.getStatus() == TenderStatus.CANCELLED).count();

		return new AgencyStatisticsResponse(
				(long) tenders.size(),
				openTenders,
				closedTenders,
				cancelledTenders,
				agency.getVerifiedStatus());
	}

	private TenderAgencyResponse mapToAgencyResponse(TenderAgencyProfile agency) {
		return new TenderAgencyResponse(
				agency.getId(),
				agency.getCompanyName(),
				agency.getTinNumber(),
				agency.getWebsite(),
				agency.getContactPerson(),
				agency.getVerifiedStatus(),
				agency.getBusinessLicensePath());
	}
}
