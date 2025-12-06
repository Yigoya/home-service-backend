package com.home.service.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TenderAgencyProfile extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String tinNumber;

    private String businessLicensePath;

    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
    private List<Tender> tenders = new ArrayList<>();

    private String website;
    private String contactPerson;
    private String verifiedStatus = "PENDING";

}