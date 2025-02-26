package com.home.service.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.Service.QuestionService;
import com.home.service.Service.TechnicianService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.home.service.models.Technician;
import com.home.service.models.TechnicianAddress;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.repositories.TechnicianAddressRepository;
import com.home.service.dto.QuestionDTO;
import com.home.service.dto.TechnicianDTO;

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/search")
public class SearchController {

        private final TechnicianService technicianService;
        private final QuestionService questionService;
        private final TechnicianAddressRepository technicianAddressRepository;

        public SearchController(TechnicianService technicianService, QuestionService questionService,
                        TechnicianAddressRepository technicianAddressRepository) {
                this.technicianService = technicianService;
                this.questionService = questionService;
                this.technicianAddressRepository = technicianAddressRepository;
        }

        @GetMapping("/service/{serviceId}")
        public ResponseEntity<List<TechnicianDTO>> searchForService(@PathVariable Long serviceId,
                        @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang) {
                List<Technician> technicians = technicianService.findTechniciansByService(serviceId);
                if (technicians.isEmpty()) {
                        return ResponseEntity.ok(List.of());
                }
                TechnicianAddress technicianAddress = technicianAddressRepository
                                .findByTechnicianId(technicians.get(0).getId()).orElse(null);
                List<TechnicianDTO> technicianDTOs = technicians.stream()
                                .map(technician -> {
                                        TechnicianDTO technicianDTO = new TechnicianDTO(technician, lang);
                                        technicianDTO.setCity(technicianAddress.getCity());
                                        technicianDTO.setSubcity(technicianAddress.getSubcity());
                                        technicianDTO.setWereda(technicianAddress.getWereda());
                                        technicianDTO.setCountry(technicianAddress.getCountry());
                                        technicianDTO.setLatitude(technicianAddress.getLatitude());
                                        technicianDTO.setLongitude(technicianAddress.getLongitude());
                                        return technicianDTO;
                                })
                                .collect(Collectors.toList());
                return ResponseEntity.ok(technicianDTOs);
        }

        @GetMapping("/tech")
        public ResponseEntity<List<TechnicianDTO>> searchForTechnician(
                        @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) Double minPrice,
                        @RequestParam(required = false) Double maxPrice,
                        @RequestParam(required = false) Double minRating,
                        @RequestParam(required = false) String location) {
                List<Technician> technicians = technicianService.searchTechnicians(query, minPrice, maxPrice, minRating,
                                location);
                List<TechnicianDTO> technicianDTOs = technicians.stream()
                                .map(technician -> new TechnicianDTO(technician, lang))
                                .collect(Collectors.toList());
                return ResponseEntity.ok(technicianDTOs);
        }

        @GetMapping("/technicians/{serviceId}")
        public ResponseEntity<Page<TechnicianDTO>> getTechnicians(
                        @PathVariable Long serviceId,
                        @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Double minPrice,
                        @RequestParam(required = false) Double maxPrice,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) String subcity,
                        @RequestParam(required = false) String wereda,
                        @RequestParam(required = false) Double minLatitude,
                        @RequestParam(required = false) Double maxLatitude,
                        @RequestParam(required = false) Double minLongitude,
                        @RequestParam(required = false) Double maxLongitude,
                        Pageable pageable) {

                Page<Technician> technicians = technicianService.filterTechnicians(
                                serviceId, name, minPrice, maxPrice, city, subcity, wereda,
                                minLatitude, maxLatitude, minLongitude, maxLongitude, pageable);

                Page<TechnicianDTO> technicianDTOs = technicians.map(technician -> new TechnicianDTO(technician, lang));
                return ResponseEntity.ok(technicianDTOs);
        }

        @GetMapping("/service-schedule/{serviceId}")
        public ResponseEntity<Page<TechnicianDTO>> searchTechniciansByService(
                        @PathVariable Long serviceId,
                        @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
                        Pageable pageable) {

                Page<Technician> technicians = technicianService.findAvailableTechniciansByServiceAndSchedule(serviceId,
                                date, time, pageable);

                Page<TechnicianDTO> technicianDTOs = technicians.map(technician -> new TechnicianDTO(technician, lang));
                return ResponseEntity.ok(technicianDTOs);
        }

        @GetMapping("/technicians-schedule")
        public ResponseEntity<Page<TechnicianDTO>> filterTechnicians(
                        @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Double minPrice,
                        @RequestParam(required = false) Double maxPrice,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) String subcity,
                        @RequestParam(required = false) String wereda,
                        @RequestParam(required = false) Double minLatitude,
                        @RequestParam(required = false) Double maxLatitude,
                        @RequestParam(required = false) Double minLongitude,
                        @RequestParam(required = false) Double maxLongitude,
                        @RequestParam(required = false) Boolean availability,
                        @RequestParam(required = false) LocalDate date,
                        @RequestParam(required = false) LocalTime time,
                        @RequestParam(required = false) Long serviceId,
                        Pageable pageable) {

                Page<Technician> technicians = technicianService.filterTechnicians(
                                name, minPrice, maxPrice, city, subcity, wereda, minLatitude, maxLatitude, minLongitude,
                                maxLongitude,
                                availability, time, date, serviceId, pageable);

                Page<TechnicianDTO> technicianDTOs = technicians.map(technician -> new TechnicianDTO(technician, lang));
                return ResponseEntity.ok(technicianDTOs);
        }

        @GetMapping("/question/{serviceId}")
        public ResponseEntity<List<QuestionDTO>> getQuestionsByService(@PathVariable Long serviceId) {
                List<QuestionDTO> questions = questionService.getQuestionsByServiceId(serviceId);
                return ResponseEntity.ok(questions);
        }

}
