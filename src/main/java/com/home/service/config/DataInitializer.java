package com.home.service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.home.service.models.SubscriptionPlan;
import com.home.service.models.enums.PlanType;
import com.home.service.repositories.SubscriptionPlanRepository;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(SubscriptionPlanRepository planRepository) {
        return args -> {
            if (planRepository.count() == 0) {
                List<SubscriptionPlan> plans = List.of(
                        // Marketplace Suppliers
                        new SubscriptionPlan("Free Membership", new BigDecimal("0"), 1, PlanType.MARKETPLACE,
                                List.of("List up to 10 products", "30-day basic company profile",
                                        "Limited search visibility", "Foundational customer support"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Gold Supplier", new BigDecimal("399"), 12, PlanType.MARKETPLACE,
                                List.of("30 product listings with showcase tiles", "60-day listing duration",
                                        "Basic catalogue display", "Email & phone support",
                                        "Priority search ranking (medium)"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Platinum Supplier", new BigDecimal("999"), 12, PlanType.MARKETPLACE,
                                List.of("100 product listings", "90-day visibility window",
                                        "Top-of-search placement", "Personal account manager",
                                        "Advanced analytics dashboard", "Featured listing inside category",
                                        "Verified supplier badge", "Lead management toolkit",
                                        "Phone + email support"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Diamond Supplier", new BigDecimal("1299"), 12, PlanType.MARKETPLACE,
                                List.of("Unlimited product listings", "120-day high-impact placements",
                                        "Homepage & category featuring", "Dedicated account manager",
                                        "Premium catalogue design", "Trade show promotions",
                                        "RFQ alert automation"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Enterprise Custom", new BigDecimal("14999"), 12, PlanType.MARKETPLACE,
                                List.of("Unlimited & custom listing strategy", "Industry-specific promotions",
                                        "API & system integrations", "Multi-user workflows",
                                        "Branding solutions & amplification", "Dedicated technical team",
                                        "White-label experiences"),
                                List.of(), List.of()),

                        // Home & Professional Services
                        new SubscriptionPlan("Free", new BigDecimal("0"), 1, PlanType.HOME_PROFESSIONAL,
                                List.of("Basic profile listing", "Customer reviews",
                                        "Direct contact by customers (call/message)"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Professional 3-Month", new BigDecimal("499"), 3,
                                PlanType.HOME_PROFESSIONAL,
                                List.of("All Free features", "Receive lead notifications",
                                        "Profile highlighting in search", "Basic performance analytics",
                                        "Receive customer requests", "Portfolio photos gallery",
                                        "Booking with location support"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Professional 6-Month", new BigDecimal("799"), 6,
                                PlanType.HOME_PROFESSIONAL,
                                List.of("All Free features", "Lead notifications & requests",
                                        "Highlighted search placement", "Performance analytics",
                                        "Portfolio photos & booking"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Professional Annual", new BigDecimal("1299"), 12,
                                PlanType.HOME_PROFESSIONAL,
                                List.of("All Professional features", "Unlimited portfolio photos",
                                        "Location-based booking", "Lead notifications & analytics"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Enterprise / Agency", new BigDecimal("9999"), 12,
                                PlanType.HOME_PROFESSIONAL,
                                List.of("All Professional features", "Multiple user accounts", "API access",
                                        "Custom performance reporting", "Dedicated account manager",
                                        "Unlimited portfolio photos", "Location-based booking"),
                                List.of(), List.of()),

                        // Tender Intelligence
                        new SubscriptionPlan("Free", new BigDecimal("0"), 1, PlanType.TENDER,
                                List.of("Limited tender alerts", "Basic search tools", "5 tender views per day",
                                        "Limited email notifications", "Direct supplier contact"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Starter (3 Months)", new BigDecimal("1499"), 3, PlanType.TENDER,
                                List.of("Unlimited website access", "Daily email notifications",
                                        "Unlimited tender downloads", "Unlimited keyword search", "Bid dashboard",
                                        "1 user account", "24/7 customer support"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Growth (6 Months)", new BigDecimal("2499"), 6, PlanType.TENDER,
                                List.of("All Starter benefits", "Priority support channel",
                                        "Saved searches & alerts", "Bid dashboard analytics"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Annual Advantage", new BigDecimal("3000"), 12, PlanType.TENDER,
                                List.of("All Growth benefits", "Tender pipeline analytics",
                                        "Document storage & notes"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Enterprise Intelligence", new BigDecimal("9999"), 12, PlanType.TENDER,
                                List.of("Unlimited access & downloads", "API integration", "5 user accounts",
                                        "5 alert email IDs", "Tender analytics & reporting",
                                        "Dedicated support team"),
                                List.of(), List.of()),

                        // Yellow Pages Directory
                        new SubscriptionPlan("Free Membership", new BigDecimal("0"), 12, PlanType.YELLOW_PAGES,
                                List.of("Directory submission", "Company name & info",
                                        "Business contact number", "Business address",
                                        "Company website URL"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Gold Listing", new BigDecimal("2999"), 12, PlanType.YELLOW_PAGES,
                                List.of("All Free features", "Company logo", "Image gallery",
                                        "Business contact email", "Business enquiry form"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Platinum Listing", new BigDecimal("4999"), 12,
                                PlanType.YELLOW_PAGES,
                                List.of("All Gold features", "Google Analytics integration",
                                        "Business opening hours", "Social media links",
                                        "Customer reviews highlighting"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Diamond Listing", new BigDecimal("10499"), 12,
                                PlanType.YELLOW_PAGES,
                                List.of("All Platinum features", "Done-for-you copywriting",
                                        "Multiple photo galleries", "Social media management",
                                        "Guaranteed category placement", "Review management", "24/7 support"),
                                List.of(), List.of()),

                        // Job Board Packages
                        new SubscriptionPlan("New User", new BigDecimal("5000"), 12, PlanType.JOBS,
                                List.of("1-5 job postings", "Jobs live for 30 days",
                                        "Post with job category fields", "Unlimited candidate views",
                                        "Advanced search filters"),
                                List.of(), List.of()),
                        new SubscriptionPlan("10 Jobs Plan", new BigDecimal("15000"), 12, PlanType.JOBS,
                                List.of("10 job postings", "30-day job display", "Unlimited candidate views",
                                        "Advanced talent search", "Featured company placement",
                                        "Dashboard management", "Featured job slots"),
                                List.of(), List.of()),
                        new SubscriptionPlan("50 Jobs Plan", new BigDecimal("30000"), 12, PlanType.JOBS,
                                List.of("50 job postings", "45-day job display", "Unlimited candidate views",
                                        "Advanced search & filters", "Featured company status",
                                        "Dashboard management", "Featured jobs carousel"),
                                List.of(), List.of()),
                        new SubscriptionPlan("100 Jobs Plan", new BigDecimal("50000"), 12, PlanType.JOBS,
                                List.of("100 job postings", "30-day job display", "Unlimited candidate views",
                                        "Advanced search suite", "Featured company status",
                                        "Dashboard management", "Fully branded career page",
                                        "Featured employer spotlights"),
                                List.of(), List.of()),
                        new SubscriptionPlan("Unlimited Plan", new BigDecimal("120000"), 12, PlanType.JOBS,
                                List.of("Unlimited job postings", "30-day job display cycles",
                                        "Unlimited candidate views", "Advanced search & filters",
                                        "Dashboard management", "Featured employer placement",
                                        "Branded career page", "24-hour customer support"),
                                List.of(), List.of()));
                planRepository.saveAll(plans);
                System.out.println("Subscription plans loaded into database.");
            }
        };
    }
}