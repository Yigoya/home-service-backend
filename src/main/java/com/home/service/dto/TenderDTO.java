
package com.home.service.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.home.service.models.Tender;
import com.home.service.models.enums.TenderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class TenderDTO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime closingDate;
    private String contactInfo;
    private TenderStatus status;
    private Long serviceId;
    private String document;
    private LocalDateTime datePosted;
    private String categoryName;
    private LocalDateTime questionDeadline;
    @JsonProperty("isFree")
    private boolean free;
    private String extraJson;

    public static TenderDTO createWithFullDetails(Tender tender) {
        TenderDTO tenderDTO = new TenderDTO();
        tenderDTO.setId(tender.getId());
        tenderDTO.setTitle(tender.getTitle());
        tenderDTO.setDescription(tender.getDescription());
        tenderDTO.setLocation(tender.getLocation());
        tenderDTO.setClosingDate(tender.getClosingDate());
        tenderDTO.setContactInfo(tender.getContactInfo());
        tenderDTO.setStatus(tender.getStatus());
        tenderDTO.setServiceId(tender.getService().getId());
        tenderDTO.setDocument(tender.getDocumentPath());
        tenderDTO.setDatePosted(tender.getDatePosted());
        tenderDTO.setCategoryName(tender.getService().getTranslations().stream().findFirst().get().getName());
        tenderDTO.setQuestionDeadline(tender.getQuestionDeadline());
        tenderDTO.setFree(Boolean.TRUE.equals(tender.getFree()));
        tenderDTO.setExtraJson(tender.getExtraJson());

        return tenderDTO;
    }

    public static TenderDTO createWithoutSensitiveDetails(Tender tender) {
        TenderDTO tenderDTO = new TenderDTO();
        tenderDTO.setId(tender.getId());
        tenderDTO.setTitle(tender.getTitle());
        tenderDTO.setDescription(tender.getDescription());
        tenderDTO.setLocation(tender.getLocation());
        tenderDTO.setClosingDate(tender.getClosingDate());
        tenderDTO.setStatus(tender.getStatus());
        tenderDTO.setServiceId(tender.getService().getId());
        tenderDTO.setDatePosted(tender.getDatePosted());
        tenderDTO.setCategoryName(tender.getService().getTranslations().stream().findFirst().get().getName());
        tenderDTO.setQuestionDeadline(tender.getQuestionDeadline());
        tenderDTO.setFree(Boolean.TRUE.equals(tender.getFree()));
        tenderDTO.setExtraJson(tender.getExtraJson());
        return tenderDTO;
    }
}
