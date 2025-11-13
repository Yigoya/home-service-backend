
package com.home.service.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import com.home.service.models.enums.TenderStatus;

@Getter
@Setter
public class TenderRequest {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime closingDate;
    private String contactInfo;
    private TenderStatus status;
    private Long categoryId;
    private Boolean isFree; // optional, defaults to false when not provided

    private MultipartFile file;
}
