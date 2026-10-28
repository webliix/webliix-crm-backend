-- V67: Create job openings and candidate applications tables, default positions, and permissions

CREATE TABLE IF NOT EXISTS job_openings (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    department VARCHAR(100) NOT NULL,
    type VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    experience VARCHAR(100),
    description TEXT,
    requirements TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_applications (
    id BIGSERIAL PRIMARY KEY,
    job_opening_id BIGINT REFERENCES job_openings(id) ON DELETE SET NULL,
    job_title VARCHAR(255) NOT NULL,
    application_type VARCHAR(50) DEFAULT 'job',
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(100),
    location VARCHAR(255),
    experience VARCHAR(100),
    portfolio_url TEXT,
    resume_link TEXT,
    message TEXT,
    payout_method VARCHAR(100),
    payout_details TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed default openings matching Webliix Careers
INSERT INTO job_openings (title, department, type, location, experience, description, requirements, active)
VALUES
('Frontend React / UI Engineer', 'Engineering', 'Full-Time / Internship', 'Remote (India)', '1-3 Years',
 'Build high-performance, spatial web applications using React, Vite, TailwindCSS, and Framer Motion with sub-second loading speeds.',
 'Strong proficiency in React.js, TailwindCSS & modern ES6+; Eye for micro-interactions, responsive UI & Core Web Vitals; Familiarity with Git and component architecture', true),
('Full-Stack Node.js Developer', 'Backend & APIs', 'Full-Time / Contract', 'Remote (India)', '2-4 Years',
 'Architect scalable Node.js/Express backends, RESTful APIs, database schemas (PostgreSQL / MongoDB), and secure payment gateway webhooks.',
 'Hands-on experience with Node.js, Express, REST APIs & SQL/NoSQL; Integration experience with Razorpay/Stripe and Auth systems; Clean code practices, error handling & automated testing', true),
('UI/UX & Spatial Product Designer', 'Design & Spatial', 'Contract / Part-Time', 'Remote (India)', '1-3 Years',
 'Design fluid spatial user interfaces, interactive 3D components, and polished micro-interactions in Figma and Tailwind.',
 'Advanced Figma expertise with auto-layout & design tokens; Portfolio demonstrating responsive web apps; Understanding of spatial & glassmorphic design principles', true),
('Growth & Business Development Partner', 'Growth & Sales', 'Partner / Commission', 'Remote (India)', 'Open to All',
 'Refer enterprise clients, manage deal lifecycles, and earn industry-leading up to 20% commission on every closed web & app contract.',
 'Network of businesses needing web/app solutions; Excellent communication & client management skills; Motivation to drive high-ticket digital agency sales', true)
ON CONFLICT DO NOTHING;

-- Permissions registration
INSERT INTO permissions (code, module, action, description)
VALUES 
('JOBS_VIEW', 'JOBS', 'READ', 'View job openings and candidate applications'),
('JOBS_MANAGE', 'JOBS', 'MANAGE', 'Create, update, and manage job openings and applicant statuses'),
('AUTOMATION_MANAGE', 'AUTOMATION', 'MANAGE', 'Manage automation rules, workflows, and automated actions'),
('EMAIL_TEMPLATES_MANAGE', 'NOTIFICATIONS', 'MANAGE', 'Manage, edit, and preview automated transactional email templates')
ON CONFLICT (code) DO NOTHING;

-- Grant permissions to Admin and Super Admin
INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'ALL'
FROM roles r, permissions p
WHERE r.name IN ('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN', 'HR', 'ROLE_HR')
  AND p.code IN ('JOBS_VIEW', 'JOBS_MANAGE', 'AUTOMATION_MANAGE', 'EMAIL_TEMPLATES_MANAGE')
ON CONFLICT (role_id, permission_id) DO UPDATE SET scope = 'ALL';
