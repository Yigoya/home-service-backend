package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.home.service.models.BusinessLocation;
import com.home.service.models.enums.Coordinates;
import com.home.service.models.enums.LocationType;
import com.home.service.repositories.BusinessLocationRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@Service
public class BusinessLocationService {

    private final BusinessLocationRepository businessLocationRepository;

    public BusinessLocationService(BusinessLocationRepository businessLocationRepository) {
        this.businessLocationRepository = businessLocationRepository;
    }

    public static class BusinessLocationDTO {
        public Long id;
        public String street;

        public String city;

        public String state;

        public String postalCode;

        public String country;

        public String name;
        public LocationType type;
        public Long parentLocationId;
        public Coordinates coordinates;

        public BusinessLocationDTO() {
        }

        public BusinessLocationDTO(BusinessLocation location) {
            this.id = location.getId();
            this.street = location.getStreet();
            this.city = location.getCity();
            this.state = location.getState();
            this.postalCode = location.getPostalCode();
            this.country = location.getCountry();
            this.name = location.getName();
            this.type = location.getType();
            this.parentLocationId = location.getParentLocation() != null ? location.getParentLocation().getId() : null;
            this.coordinates = location.getCoordinates();
        }
    }

    public BusinessLocationDTO createLocation(@Valid BusinessLocationDTO dto) {
        BusinessLocation location = new BusinessLocation();
        location.setName(dto.name);
        location.setType(dto.type);
        if (dto.parentLocationId != null) {
            BusinessLocation parent = businessLocationRepository.findById(dto.parentLocationId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent location not found with ID: " + dto.parentLocationId));
            location.setParentLocation(parent);
        }
        location.setCoordinates(dto.coordinates);
        location.setState(dto.state);
        location.setCity(dto.city);
        location.setPostalCode(dto.postalCode);
        location.setStreet(dto.street);
        location.setCountry(dto.country);

        BusinessLocation savedLocation = businessLocationRepository.save(location);
        return new BusinessLocationDTO(savedLocation);
    }

    public BusinessLocation getLocationById(Long id) {
        return businessLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + id));
    }

    public BusinessLocationDTO updateLocation(Long id, @Valid BusinessLocationDTO dto) {
        BusinessLocation location = getLocationById(id);
        location.setName(dto.name);
        location.setType(dto.type);
        if (dto.parentLocationId != null) {
            BusinessLocation parent = businessLocationRepository.findById(dto.parentLocationId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent location not found with ID: " + dto.parentLocationId));
            location.setParentLocation(parent);
        } else {
            location.setParentLocation(null);
        }
        location.setCoordinates(dto.coordinates);

        BusinessLocation savedLocation = businessLocationRepository.save(location);
        return new BusinessLocationDTO(savedLocation);
    }

    public void deleteLocation(Long id) {
        BusinessLocation location = getLocationById(id);
        businessLocationRepository.delete(location);
    }

    public Page<BusinessLocationDTO> getAllLocations(String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (type != null) {
            Page<BusinessLocation> locations = businessLocationRepository
                    .findByType(LocationType.valueOf(type.toUpperCase()), pageable);
            return locations.map(BusinessLocationDTO::new);
        }
        Page<BusinessLocation> locations = businessLocationRepository.findAll(pageable);
        return locations.map(BusinessLocationDTO::new);
    }

    public Page<BusinessLocationDTO> getNearbyLocations(double latitude, double longitude, double radius, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Assume a custom repository method for geospatial query
        Page<BusinessLocation> locations = businessLocationRepository.findNearby(latitude, longitude, radius, pageable);
        return locations.map(BusinessLocationDTO::new);
    }
}