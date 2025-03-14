package com.home.service.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.home.service.models.TenderSubscriptionPlan;
import com.home.service.repositories.TenderSubscriptionPlanRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    private final TenderSubscriptionPlanRepository planRepository;

    public DataInitializer(TenderSubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public void run(String... args) {
        if (planRepository.count() == 0) {
            initializePlans();
        }
    }

    private void initializePlans() {
        List<TenderSubscriptionPlan> plans = Arrays.asList(
                new TenderSubscriptionPlan("free", "Free Membership", 0.0, "N/A",
                        Arrays.asList(
                                "Access State Tenders for Free (Website Only)",
                                "Download Tender Document for Free (Website Only)",
                                "Create a Free Business Listing")),
                new TenderSubscriptionPlan("monthly_1", "1 Month", 500.0, "1 Month",
                        Arrays.asList(
                                "Unlimited Tender Access",
                                "Tender Notification via Email, WhatsApp, Telegram",
                                "Online Dashboard Access",
                                "Access to Archive Tenders",
                                "Unlimited Keywords",
                                "Personal Dashboard",
                                "Advanced Search by Category, Location, etc.")),
                // Add other plans similarly...
                new TenderSubscriptionPlan("yearly", "1 Year", 2000.0, "12 Months",
                        Arrays.asList(
                                "Unlimited Tender Access",
                                "Tender Notification via Email, WhatsApp, Telegram",
                                "Online Dashboard Access",
                                "Access to Archive Tenders",
                                "Unlimited Keywords",
                                "Personal Dashboard",
                                "Advanced Search by Category, Location, etc.")));

        planRepository.saveAll(plans);
    }
}