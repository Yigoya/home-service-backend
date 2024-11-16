package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.ContactUs;

public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {
}
