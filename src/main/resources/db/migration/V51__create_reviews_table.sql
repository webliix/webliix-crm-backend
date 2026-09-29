CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    author_name VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    email VARCHAR(255),
    rating INT NOT NULL DEFAULT 5,
    review_text TEXT NOT NULL,
    platform VARCHAR(50) NOT NULL DEFAULT 'WEBSITE',
    platform_url VARCHAR(500),
    service_used VARCHAR(255),
    approved BOOLEAN DEFAULT TRUE,
    featured BOOLEAN DEFAULT FALSE,
    publish_consent BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Initial High-Quality Reviews across Google, Website, and LinkedIn
INSERT INTO reviews (author_name, company_name, email, rating, review_text, platform, service_used, approved, featured, publish_consent, created_at)
VALUES 
('Aarav Mehta', 'AuraDrishti Eyewear', 'aarav@auradrishti.com', 5, 'Webliix engineered our complete e-commerce store and local SEO engine. We achieved #1 Google NCR rankings within 60 days. Highly professional team!', 'GOOGLE', 'Webliix LaunchKit & E-Commerce', true, true, true, CURRENT_TIMESTAMP - INTERVAL '15 days'),

('Priya Sharma', 'NexGen Digital Solutions', 'priya@nexgendigital.in', 5, 'The Webliix Hub platform simplified our entire workflow. Outstanding microservices architecture, blazing fast load speeds, and top-tier support.', 'WEBSITE', 'Custom Web Application', true, true, true, CURRENT_TIMESTAMP - INTERVAL '10 days'),

('Rahul Verma', 'FinServe NCR', 'rahul.verma@finservencr.com', 5, 'Webliix delivered our secure portal with Spring Boot 3 & React. Seamless integration with Brevo email notifications and PostgreSQL CRM.', 'LINKEDIN', 'Enterprise Software & Portal', true, true, true, CURRENT_TIMESTAMP - INTERVAL '5 days'),

('Vikram Singh', 'Delhi Tech Logistics', 'vikram@delhitechlogistics.com', 5, 'Webliix LaunchKit setup saved us months of development. Google Ads campaign generated over 150 qualified leads in the first 30 days.', 'GOOGLE', 'LaunchKit & Google Ads', true, true, true, CURRENT_TIMESTAMP - INTERVAL '2 days');
