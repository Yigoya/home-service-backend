-- Seed subscription plans aligned to the frontend catalog

-- Marketplace Suppliers
INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Free Membership', 0, 1, 'MARKETPLACE'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Free Membership' AND plan_type = 'MARKETPLACE');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('List up to 10 products'),
    ('30-day basic company profile'),
    ('Limited search visibility'),
    ('Foundational customer support')
) AS f(feature) ON TRUE
WHERE p.name = 'Free Membership' AND p.plan_type = 'MARKETPLACE'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Gold Supplier', 399, 12, 'MARKETPLACE'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Gold Supplier' AND plan_type = 'MARKETPLACE');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('30 product listings with showcase tiles'),
    ('60-day listing duration'),
    ('Basic catalogue display'),
    ('Email & phone support'),
    ('Priority search ranking (medium)')
) AS f(feature) ON TRUE
WHERE p.name = 'Gold Supplier' AND p.plan_type = 'MARKETPLACE'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Platinum Supplier', 999, 12, 'MARKETPLACE'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Platinum Supplier' AND plan_type = 'MARKETPLACE');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('100 product listings'),
    ('90-day visibility window'),
    ('Top-of-search placement'),
    ('Personal account manager'),
    ('Advanced analytics dashboard'),
    ('Featured listing inside category'),
    ('Verified supplier badge'),
    ('Lead management toolkit'),
    ('Phone + email support')
) AS f(feature) ON TRUE
WHERE p.name = 'Platinum Supplier' AND p.plan_type = 'MARKETPLACE'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Diamond Supplier', 1299, 12, 'MARKETPLACE'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Diamond Supplier' AND plan_type = 'MARKETPLACE');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('Unlimited product listings'),
    ('120-day high-impact placements'),
    ('Homepage & category featuring'),
    ('Dedicated account manager'),
    ('Premium catalogue design'),
    ('Trade show promotions'),
    ('RFQ alert automation')
) AS f(feature) ON TRUE
WHERE p.name = 'Diamond Supplier' AND p.plan_type = 'MARKETPLACE'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Enterprise Custom', 14999, 12, 'MARKETPLACE'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Enterprise Custom' AND plan_type = 'MARKETPLACE');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('Unlimited & custom listing strategy'),
    ('Industry-specific promotions'),
    ('API & system integrations'),
    ('Multi-user workflows'),
    ('Branding solutions & amplification'),
    ('Dedicated technical team'),
    ('White-label experiences')
) AS f(feature) ON TRUE
WHERE p.name = 'Enterprise Custom' AND p.plan_type = 'MARKETPLACE'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

-- Home & Professional Services
INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Free', 0, 1, 'HOME_PROFESSIONAL'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Free' AND plan_type = 'HOME_PROFESSIONAL');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('Basic profile listing'),
    ('Customer reviews'),
    ('Direct contact by customers (call/message)')
) AS f(feature) ON TRUE
WHERE p.name = 'Free' AND p.plan_type = 'HOME_PROFESSIONAL'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Professional 3-Month', 499, 3, 'HOME_PROFESSIONAL'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Professional 3-Month' AND plan_type = 'HOME_PROFESSIONAL');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('All Free features'),
    ('Receive lead notifications'),
    ('Profile highlighting in search'),
    ('Basic performance analytics'),
    ('Receive customer requests'),
    ('Portfolio photos gallery'),
    ('Booking with location support')
) AS f(feature) ON TRUE
WHERE p.name = 'Professional 3-Month' AND p.plan_type = 'HOME_PROFESSIONAL'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Professional 6-Month', 799, 6, 'HOME_PROFESSIONAL'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Professional 6-Month' AND plan_type = 'HOME_PROFESSIONAL');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('All Free features'),
    ('Lead notifications & requests'),
    ('Highlighted search placement'),
    ('Performance analytics'),
    ('Portfolio photos & booking')
) AS f(feature) ON TRUE
WHERE p.name = 'Professional 6-Month' AND p.plan_type = 'HOME_PROFESSIONAL'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Professional Annual', 1299, 12, 'HOME_PROFESSIONAL'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Professional Annual' AND plan_type = 'HOME_PROFESSIONAL');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('All Professional features'),
    ('Unlimited portfolio photos'),
    ('Location-based booking'),
    ('Lead notifications & analytics')
) AS f(feature) ON TRUE
WHERE p.name = 'Professional Annual' AND p.plan_type = 'HOME_PROFESSIONAL'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Enterprise / Agency', 9999, 12, 'HOME_PROFESSIONAL'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Enterprise / Agency' AND plan_type = 'HOME_PROFESSIONAL');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('All Professional features'),
    ('Multiple user accounts'),
    ('API access'),
    ('Custom performance reporting'),
    ('Dedicated account manager'),
    ('Unlimited portfolio photos'),
    ('Location-based booking')
) AS f(feature) ON TRUE
WHERE p.name = 'Enterprise / Agency' AND p.plan_type = 'HOME_PROFESSIONAL'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

-- Tender Intelligence
INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Free', 0, 1, 'TENDER'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Free' AND plan_type = 'TENDER');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('Limited tender alerts'),
    ('Basic search tools'),
    ('5 tender views per day'),
    ('Limited email notifications'),
    ('Direct supplier contact')
) AS f(feature) ON TRUE
WHERE p.name = 'Free' AND p.plan_type = 'TENDER'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Starter (3 Months)', 1499, 3, 'TENDER'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Starter (3 Months)' AND plan_type = 'TENDER');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('Unlimited website access'),
    ('Daily email notifications'),
    ('Unlimited tender downloads'),
    ('Unlimited keyword search'),
    ('Bid dashboard'),
    ('1 user account'),
    ('24/7 customer support')
) AS f(feature) ON TRUE
WHERE p.name = 'Starter (3 Months)' AND p.plan_type = 'TENDER'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Growth (6 Months)', 2499, 6, 'TENDER'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Growth (6 Months)' AND plan_type = 'TENDER');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('All Starter benefits'),
    ('Priority support channel'),
    ('Saved searches & alerts'),
    ('Bid dashboard analytics')
) AS f(feature) ON TRUE
WHERE p.name = 'Growth (6 Months)' AND p.plan_type = 'TENDER'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Annual Advantage', 3000, 12, 'TENDER'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Annual Advantage' AND plan_type = 'TENDER');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('All Growth benefits'),
    ('Tender pipeline analytics'),
    ('Document storage & notes')
) AS f(feature) ON TRUE
WHERE p.name = 'Annual Advantage' AND p.plan_type = 'TENDER'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Enterprise Intelligence', 9999, 12, 'TENDER'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Enterprise Intelligence' AND plan_type = 'TENDER');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('Unlimited access & downloads'),
    ('API integration'),
    ('5 user accounts'),
    ('5 alert email IDs'),
    ('Tender analytics & reporting'),
    ('Dedicated support team')
) AS f(feature) ON TRUE
WHERE p.name = 'Enterprise Intelligence' AND p.plan_type = 'TENDER'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

-- Yellow Pages Directory
INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Free Membership', 0, 12, 'YELLOW_PAGES'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Free Membership' AND plan_type = 'YELLOW_PAGES');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('Directory submission'),
    ('Company name & info'),
    ('Business contact number'),
    ('Business address'),
    ('Company website URL')
) AS f(feature) ON TRUE
WHERE p.name = 'Free Membership' AND p.plan_type = 'YELLOW_PAGES'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Gold Listing', 2999, 12, 'YELLOW_PAGES'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Gold Listing' AND plan_type = 'YELLOW_PAGES');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('All Free features'),
    ('Company logo'),
    ('Image gallery'),
    ('Business contact email'),
    ('Business enquiry form')
) AS f(feature) ON TRUE
WHERE p.name = 'Gold Listing' AND p.plan_type = 'YELLOW_PAGES'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Platinum Listing', 4999, 12, 'YELLOW_PAGES'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Platinum Listing' AND plan_type = 'YELLOW_PAGES');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('All Gold features'),
    ('Google Analytics integration'),
    ('Business opening hours'),
    ('Social media links'),
    ('Customer reviews highlighting')
) AS f(feature) ON TRUE
WHERE p.name = 'Platinum Listing' AND p.plan_type = 'YELLOW_PAGES'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Diamond Listing', 10499, 12, 'YELLOW_PAGES'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Diamond Listing' AND plan_type = 'YELLOW_PAGES');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('All Platinum features'),
    ('Done-for-you copywriting'),
    ('Multiple photo galleries'),
    ('Social media management'),
    ('Guaranteed category placement'),
    ('Review management'),
    ('24/7 support')
) AS f(feature) ON TRUE
WHERE p.name = 'Diamond Listing' AND p.plan_type = 'YELLOW_PAGES'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

-- Job Board Packages
INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'New User', 5000, 12, 'JOBS'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'New User' AND plan_type = 'JOBS');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('1-5 job postings'),
    ('Jobs live for 30 days'),
    ('Post with job category fields'),
    ('Unlimited candidate views'),
    ('Advanced search filters')
) AS f(feature) ON TRUE
WHERE p.name = 'New User' AND p.plan_type = 'JOBS'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT '10 Jobs Plan', 15000, 12, 'JOBS'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = '10 Jobs Plan' AND plan_type = 'JOBS');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('10 job postings'),
    ('30-day job display'),
    ('Unlimited candidate views'),
    ('Advanced talent search'),
    ('Featured company placement'),
    ('Dashboard management'),
    ('Featured job slots')
) AS f(feature) ON TRUE
WHERE p.name = '10 Jobs Plan' AND p.plan_type = 'JOBS'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT '50 Jobs Plan', 30000, 12, 'JOBS'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = '50 Jobs Plan' AND plan_type = 'JOBS');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('50 job postings'),
    ('45-day job display'),
    ('Unlimited candidate views'),
    ('Advanced search & filters'),
    ('Featured company status'),
    ('Dashboard management'),
    ('Featured jobs carousel')
) AS f(feature) ON TRUE
WHERE p.name = '50 Jobs Plan' AND p.plan_type = 'JOBS'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT '100 Jobs Plan', 50000, 12, 'JOBS'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = '100 Jobs Plan' AND plan_type = 'JOBS');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('100 job postings'),
    ('30-day job display'),
    ('Unlimited candidate views'),
    ('Advanced search suite'),
    ('Featured company status'),
    ('Dashboard management'),
    ('Fully branded career page'),
    ('Featured employer spotlights')
) AS f(feature) ON TRUE
WHERE p.name = '100 Jobs Plan' AND p.plan_type = 'JOBS'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );

INSERT INTO subscription_plans (name, price, duration_months, plan_type)
SELECT 'Unlimited Plan', 120000, 12, 'JOBS'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Unlimited Plan' AND plan_type = 'JOBS');

INSERT INTO subscription_features_english (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
JOIN (VALUES
    ('Unlimited job postings'),
    ('30-day job display cycles'),
    ('Unlimited candidate views'),
    ('Advanced search & filters'),
    ('Dashboard management'),
    ('Featured employer placement'),
    ('Branded career page'),
    ('24-hour customer support')
) AS f(feature) ON TRUE
WHERE p.name = 'Unlimited Plan' AND p.plan_type = 'JOBS'
  AND NOT EXISTS (
      SELECT 1 FROM subscription_features_english fe
      WHERE fe.plan_id = p.id AND fe.feature = f.feature
  );