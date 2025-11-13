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
                        // Technician Plans
                        new SubscriptionPlan("Basic", new BigDecimal("150"), 1, PlanType.TECHNICIAN,
                                List.of("5 bookings/month", "5km visibility"),
                                List.of("ወርሃዊ 5 ቦታ ማስያዝ", "5 ኪሜ መታየት"),
                                List.of("Booqiinsa 5/bulchiinsa", "5km mul’achuu")),
                        new SubscriptionPlan("Standard", new BigDecimal("400"), 3, PlanType.TECHNICIAN,
                                List.of("15 bookings/month", "15km visibility", "Priority listing"),
                                List.of("ወርሃዊ 15 ቦታ ማስያዝ", "15 ኪሜ መታየት", "ቅድሚያ መዘርዘር"),
                                List.of("Booqiinsa 15/bulchiinsa", "15km mul’achuu", "Durfama tarreeffamu")),
                        new SubscriptionPlan("Pro", new BigDecimal("750"), 6, PlanType.TECHNICIAN,
                                List.of("Unlimited bookings", "30km visibility", "Verified badge"),
                                List.of("ያልተገደቡ ቦታ ማስያዝ", "30 ኪሜ መታየት", "የተረጋገጠ ምልክት"),
                                List.of("Booqiinsa hin dandeenye", "30km mul’achuu", "Mallattoo mirkanaa’ee")),
                        new SubscriptionPlan("Premium", new BigDecimal("1200"), 12, PlanType.TECHNICIAN,
                                List.of("Nationwide visibility", "Featured profile", "3 promotion boosts/month"),
                                List.of("አገር አቀፍ መታየት", "ተለይቶ የቀረበ መገለጫ", "ወርሃዊ 3 ማስተዋወቂያ ጭማሪ"),
                                List.of("Mul’achuu biyya guutuu", "Profaayilii addatti argamu",
                                        "Jijjiirraa 3/bulchiinsa")),

                        // Business Plans
                        new SubscriptionPlan("Basic", new BigDecimal("200"), 1, PlanType.BUSINESS,
                                List.of("Basic listing", "5 inquiries/month"),
                                List.of("መሰረታዊ መዘርዘር", "ወርሃዊ 5 ጥያቄዎች"),
                                List.of("Tarreeffama salphaa", "Gaaffii 5/bulchiinsa")),
                        new SubscriptionPlan("Standard", new BigDecimal("550"), 3, PlanType.BUSINESS,
                                List.of("15 inquiries/month", "Priority listing", "Logo upload"),
                                List.of("ወርሃዊ 15 ጥያቄዎች", "ቅድሚያ መዘርዘር", "ሎጎ መጫን"),
                                List.of("Gaaffii 15/bulchiinsa", "Durfama tarreeffamu", "Logoo olkaa’uu")),
                        new SubscriptionPlan("Pro", new BigDecimal("1000"), 6, PlanType.BUSINESS,
                                List.of("Unlimited inquiries", "Verified badge", "Website link"),
                                List.of("ያልተገደቡ ጥያቄዎች", "የተረጋገጠ ምልክት", "ድህረ ገፅ አገናኝ"),
                                List.of("Gaaffii hin dandeenye", "Mallattoo mirkanaa’ee", "Linkii website")),
                        new SubscriptionPlan("Premium", new BigDecimal("1600"), 12, PlanType.BUSINESS,
                                List.of("Featured listing", "Multi-user access", "3 promotion boosts/month"),
                                List.of("ተለይቶ የቀረበ መዘርዘር", "ባለብዙ ተጠቃሚ መዳረሻ", "ወርሃዊ 3 ማስተዋወቂያ ጭማሪ"),
                                List.of("Tarreeffama addatti argamu", "Galtee warra hedduutiif",
                                        "Jijjiirraa 3/bulchiinsa")),

                        // Customer Tender Plans
                        new SubscriptionPlan("Basic Tender", new BigDecimal("100"), 1, PlanType.CUSTOMER_TENDER,
                                List.of("Access to 5 tenders/month", "Basic support"),
                                List.of("ወርሃዊ 5 ጨረታዎች መዳረሻ", "መሰረታዊ ድጋፍ"),
                                List.of("Tenderii 5/bulchiinsa galuun", "Gargaarsa salphaa")),
                        new SubscriptionPlan("Pro Tender", new BigDecimal("500"), 6, PlanType.CUSTOMER_TENDER,
                                List.of("Access to 20 tenders/month", "Priority notifications", "Tender analytics"),
                                List.of("ወርሃዊ 20 ጨረታዎች መዳረሻ", "ቅድሚያ ማሳወቂያ", "ጨረታ ትንታኔ"),
                                List.of("Tenderii 20/bulchiinsa galuun", "Ibsa durfama", "Qorannoo tenderii")),
                        new SubscriptionPlan("Elite Tender", new BigDecimal("900"), 12, PlanType.CUSTOMER_TENDER,
                                List.of("Unlimited tenders", "Premium support", "Tender bidding assistance"),
                                List.of("ያልተገደቡ ጨረታዎች", "ፕሪሚየም ድጋፍ", "ጨረታ መወዳደሪያ እርዳታ"),
                                List.of("Tenderii hunda galuun", "Gargaarsa sadarkaa guddaa",
                                        "Gargaarsa qabxii tenderii")));
                planRepository.saveAll(plans);
                System.out.println("Subscription plans loaded into database.");
            }
        };
    }
}