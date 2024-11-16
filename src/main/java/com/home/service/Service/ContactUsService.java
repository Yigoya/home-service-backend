package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.home.service.dto.ContactUsRequest;
import com.home.service.models.ContactUs;
import com.home.service.repositories.ContactUsRepository;

@Service
public class ContactUsService {

    @Autowired
    private ContactUsRepository contactUsRepository;

    public ContactUs submitContactUs(ContactUsRequest contactUsRequest) {
        ContactUs contactUs = new ContactUs();
        contactUs.setName(contactUsRequest.getName());
        contactUs.setEmail(contactUsRequest.getEmail());
        contactUs.setPhoneNumber(contactUsRequest.getPhoneNumber());
        contactUs.setMessage(contactUsRequest.getMessage());
        return contactUsRepository.save(contactUs);
    }
}
