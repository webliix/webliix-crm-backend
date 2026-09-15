-- Demo Data for Webliix
-- This file populates demo data for sales demos and testing

-- Demo Leads (20)
INSERT INTO leads (company_name, contact_person, email, phone, website, address, city, state, country, requirements, estimated_value, status, source, next_follow_up_date, notes, created_at, updated_at) VALUES
('Alice Johnson Consulting', 'Alice Johnson', 'alice@example.com', '+911234567891', 'www.alice-consulting.com', '123 Main St', 'New York', 'NY', 'USA', 'CRM implementation', 5000.00, 'OPEN', 'WEBSITE', CURRENT_DATE + INTERVAL '7 days', 'Initial inquiry received', NOW(), NOW()),
('Bob Smith Tech', 'Bob Smith', 'bob@example.com', '+911234567892', 'www.bobsmith-tech.com', '456 Oak Ave', 'Los Angeles', 'CA', 'USA', 'Software development', 12000.00, 'CONTACTED', 'REFERRAL', CURRENT_DATE + INTERVAL '5 days', 'Follow-up scheduled', NOW(), NOW()),
('Carol White Solutions', 'Carol White', 'carol@example.com', '+911234567893', 'www.carolwhite-solutions.com', '789 Pine Rd', 'Chicago', 'IL', 'USA', 'Enterprise software', 25000.00, 'QUALIFIED', 'EMAIL', CURRENT_DATE + INTERVAL '10 days', 'Demo completed', NOW(), NOW()),
('David Brown Industries', 'David Brown', 'david@example.com', '+911234567894', 'www.davisbrown-ind.com', '321 Elm St', 'Houston', 'TX', 'USA', 'ERP system', 15000.00, 'OPEN', 'WEBSITE', CURRENT_DATE + INTERVAL '3 days', 'Initial contact', NOW(), NOW()),
('Eve Davis Corp', 'Eve Davis', 'eve@example.com', '+911234567895', 'www.eve-davis-corp.com', '654 Maple Ave', 'Phoenix', 'AZ', 'USA', 'Analytics platform', 8000.00, 'CONTACTED', 'REFERRAL', CURRENT_DATE + INTERVAL '6 days', 'Technical discussion pending', NOW(), NOW()),
('Frank Wilson Ltd', 'Frank Wilson', 'frank@example.com', '+911234567896', 'www.frank-wilson.com', '987 Cedar Ln', 'Philadelphia', 'PA', 'USA', 'Customer portal', 9500.00, 'QUALIFIED', 'PHONE', CURRENT_DATE + INTERVAL '8 days', 'Proposal sent', NOW(), NOW()),
('Grace Miller Enterprises', 'Grace Miller', 'grace@example.com', '+911234567897', 'www.grace-miller.com', '147 Birch St', 'San Antonio', 'TX', 'USA', 'Mobile app development', 18000.00, 'OPEN', 'WEBSITE', CURRENT_DATE + INTERVAL '4 days', 'Initial inquiry', NOW(), NOW()),
('Henry Taylor Group', 'Henry Taylor', 'henry@example.com', '+911234567898', 'www.henry-taylor.com', '258 Spruce Rd', 'San Diego', 'CA', 'USA', 'Cloud migration', 22000.00, 'CONTACTED', 'EMAIL', CURRENT_DATE + INTERVAL '7 days', 'Technical assessment ongoing', NOW(), NOW()),
('Iris Anderson Inc', 'Iris Anderson', 'iris@example.com', '+911234567899', 'www.iris-anderson.com', '369 Willow Ave', 'Dallas', 'TX', 'USA', 'AI integration', 30000.00, 'QUALIFIED', 'REFERRAL', CURRENT_DATE + INTERVAL '12 days', 'Final negotiations', NOW(), NOW()),
('Jack Thomas Services', 'Jack Thomas', 'jack@example.com', '+911234567800', 'www.jack-thomas.com', '741 Oak St', 'San Jose', 'CA', 'USA', 'Security audit', 7000.00, 'OPEN', 'WEBSITE', CURRENT_DATE + INTERVAL '2 days', 'Awaiting response', NOW(), NOW()),
('Kate Jackson Partners', 'Kate Jackson', 'kate@example.com', '+911234567801', 'www.kate-jackson.com', '852 Pine Ave', 'Austin', 'TX', 'USA', 'Data warehouse', 20000.00, 'CONTACTED', 'PHONE', CURRENT_DATE + INTERVAL '5 days', 'Initial meeting scheduled', NOW(), NOW()),
('Liam White Associates', 'Liam White', 'liam@example.com', '+911234567802', 'www.liam-white.com', '963 Elm Rd', 'Jacksonville', 'FL', 'USA', 'Integration services', 11000.00, 'QUALIFIED', 'EMAIL', CURRENT_DATE + INTERVAL '9 days', 'Contract review', NOW(), NOW()),
('Mia Harris Consulting', 'Mia Harris', 'mia@example.com', '+911234567803', 'www.mia-harris.com', '147 Maple St', 'Fort Worth', 'TX', 'USA', 'Process automation', 13000.00, 'OPEN', 'REFERRAL', CURRENT_DATE + INTERVAL '3 days', 'First contact', NOW(), NOW()),
('Noah Martin Technologies', 'Noah Martin', 'noah@example.com', '+911234567804', 'www.noah-martin.com', '258 Birch Ave', 'Columbus', 'OH', 'USA', 'API development', 10000.00, 'CONTACTED', 'WEBSITE', CURRENT_DATE + INTERVAL '6 days', 'Demo requested', NOW(), NOW()),
('Olivia Garcia Solutions', 'Olivia Garcia', 'olivia@example.com', '+911234567805', 'www.olivia-garcia.com', '369 Cedar St', 'Charlotte', 'NC', 'USA', 'Business intelligence', 16000.00, 'QUALIFIED', 'PHONE', CURRENT_DATE + INTERVAL '11 days', 'Pilot project proposed', NOW(), NOW()),
('Peter Rodriguez Corp', 'Peter Rodriguez', 'peter@example.com', '+911234567806', 'www.peter-rodriguez.com', '741 Spruce Ave', 'San Francisco', 'CA', 'USA', 'DevOps consulting', 14000.00, 'OPEN', 'EMAIL', CURRENT_DATE + INTERVAL '4 days', 'Awaiting decision', NOW(), NOW()),
('Quinn Martinez Group', 'Quinn Martinez', 'quinn@example.com', '+911234567807', 'www.quinn-martinez.com', '852 Willow St', 'Indianapolis', 'IN', 'USA', 'Staff augmentation', 8500.00, 'CONTACTED', 'REFERRAL', CURRENT_DATE + INTERVAL '7 days', 'Initial discussions', NOW(), NOW()),
('Rachel Lee Enterprises', 'Rachel Lee', 'rachel@example.com', '+911234567808', 'www.rachel-lee.com', '963 Oak Rd', 'Austin', 'TX', 'USA', 'Testing services', 6000.00, 'QUALIFIED', 'WEBSITE', CURRENT_DATE + INTERVAL '8 days', 'Ready for contract', NOW(), NOW()),
('Samuel Walker Solutions', 'Samuel Walker', 'samuel@example.com', '+911234567809', 'www.samuel-walker.com', '147 Pine St', 'Memphis', 'TN', 'USA', 'System integration', 19000.00, 'OPEN', 'PHONE', CURRENT_DATE + INTERVAL '5 days', 'Initial inquiry', NOW(), NOW()),
('Tina Hall Limited', 'Tina Hall', 'tina@example.com', '+911234567810', 'www.tina-hall.com', '258 Elm Ave', 'Boston', 'MA', 'USA', 'Modernization', 23000.00, 'CONTACTED', 'EMAIL', CURRENT_DATE + INTERVAL '10 days', 'Proposal being prepared', NOW(), NOW());

-- Demo Customers (10)
INSERT INTO customers (company_name, customer_code, contact_person, email, phone, website, gst_number, address, city, state, country, lifetime_value, customer_since, active, created_at, updated_at) VALUES
('TechCorp Inc', 'CUST-001', 'John Adams', 'contact@techcorp.com', '+911234567811', 'www.techcorp.com', '36AABCU9603R1Z0', '100 Tech Park', 'Bangalore', 'KA', 'India', 150000.00, '2022-01-15', TRUE, NOW(), NOW()),
('DesignHub Ltd', 'CUST-002', 'Sarah Mitchell', 'hello@designhub.com', '+911234567812', 'www.designhub.com', '33AABCO1234H1Z2', '200 Design Plaza', 'Hyderabad', 'TS', 'India', 95000.00, '2022-06-20', TRUE, NOW(), NOW()),
('StartupXYZ Inc', 'CUST-003', 'Mike Chen', 'info@startupxyz.com', '+911234567813', 'www.startupxyz.com', '32AABCP5678H1Z3', '300 Innovation Blvd', 'Pune', 'MH', 'India', 120000.00, '2023-02-10', TRUE, NOW(), NOW()),
('GlobalTrade LLC', 'CUST-004', 'Emma Wilson', 'support@globaltrade.com', '+911234567814', 'www.globaltrade.com', '30AABCQ9012H1Z4', '400 Trade Center', 'Mumbai', 'MH', 'India', 200000.00, '2021-08-05', TRUE, NOW(), NOW()),
('FinancePro Ltd', 'CUST-005', 'Chris Johnson', 'finance@financepro.com', '+911234567815', 'www.financepro.com', '31AABCR3456H1Z5', '500 Finance Tower', 'Delhi', 'DL', 'India', 175000.00, '2022-03-30', TRUE, NOW(), NOW()),
('WebServices Inc', 'CUST-006', 'Priya Sharma', 'web@webservices.com', '+911234567816', 'www.webservices.com', '29AABCS7890H1Z6', '600 Tech Street', 'Chennai', 'TN', 'India', 110000.00, '2023-05-12', TRUE, NOW(), NOW()),
('CloudSoft Inc', 'CUST-007', 'Rajesh Kumar', 'cloud@cloudsoft.com', '+911234567817', 'www.cloudsoft.com', '28AABCT1234H1Z7', '700 Cloud Avenue', 'Kolkata', 'WB', 'India', 130000.00, '2022-09-25', TRUE, NOW(), NOW()),
('DataDrivenCo LLC', 'CUST-008', 'Neha Gupta', 'data@datadriven.com', '+911234567818', 'www.datadriven.com', '27AABCU5678H1Z8', '800 Data Drive', 'Ahmedabad', 'GJ', 'India', 105000.00, '2023-01-08', TRUE, NOW(), NOW()),
('ConversionExperts Inc', 'CUST-009', 'Anita Patel', 'experts@conversion.com', '+911234567819', 'www.conversion.com', '26AABCV9012H1Z9', '900 Conversion Plaza', 'Jaipur', 'RJ', 'India', 85000.00, '2023-04-18', TRUE, NOW(), NOW()),
('Marketing Pro Ltd', 'CUST-010', 'Vikram Singh', 'marketing@marketingpro.com', '+911234567820', 'www.marketingpro.com', '25AABCW3456H1Z0', '1000 Marketing Lane', 'Chandigarh', 'CH', 'India', 92000.00, '2022-11-03', TRUE, NOW(), NOW());

-- Demo Projects (5)
INSERT INTO projects (project_code, project_name, description, budget, start_date, expected_end_date, status, priority, customer_id, progress_percentage, billable, created_at, updated_at) VALUES
('PROJ-001', 'Website Redesign', 'Complete redesign of company website with modern UI/UX', 50000.00, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE + INTERVAL '30 days', 'IN_PROGRESS', 'HIGH', 1, 75, TRUE, NOW(), NOW()),
('PROJ-002', 'Mobile App Development', 'Native iOS and Android mobile application development', 120000.00, CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE + INTERVAL '60 days', 'IN_PROGRESS', 'HIGH', 2, 60, TRUE, NOW(), NOW()),
('PROJ-003', 'Cloud Migration', 'Migrate on-premise infrastructure to AWS cloud', 80000.00, CURRENT_DATE, CURRENT_DATE + INTERVAL '120 days', 'PLANNING', 'HIGH', 3, 10, TRUE, NOW(), NOW()),
('PROJ-004', 'ERP Implementation', 'Enterprise resource planning system setup and configuration', 150000.00, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '150 days', 'IN_PROGRESS', 'CRITICAL', 4, 40, TRUE, NOW(), NOW()),
('PROJ-005', 'API Integration', 'Third-party API integrations and payment gateway setup', 35000.00, CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE - INTERVAL '5 days', 'COMPLETED', 'MEDIUM', 5, 100, TRUE, NOW(), NOW());

-- Demo Employees (5)
INSERT INTO employees (employee_code, first_name, last_name, email, phone, department_id, designation_id, joining_date, salary, employment_type, active, address, city, state, country, emergency_contact, created_at, updated_at) VALUES
('EMP-001', 'John', 'Manager', 'john.manager@company.com', '+911234567821', NULL, NULL, '2020-01-15', 75000.00, 'PERMANENT', TRUE, '123 Manager Lane', 'Bangalore', 'KA', 'India', 'Jane Manager +919876543210', NOW(), NOW()),
('EMP-002', 'Sarah', 'Developer', 'sarah.dev@company.com', '+911234567822', NULL, NULL, '2021-06-20', 65000.00, 'PERMANENT', TRUE, '456 Developer Ave', 'Bangalore', 'KA', 'India', 'Robert Developer +919876543211', NOW(), NOW()),
('EMP-003', 'Mike', 'Designer', 'mike.designer@company.com', '+911234567823', NULL, NULL, '2022-03-10', 55000.00, 'PERMANENT', TRUE, '789 Designer Road', 'Bangalore', 'KA', 'India', 'Lisa Designer +919876543212', NOW(), NOW()),
('EMP-004', 'Emma', 'Marketer', 'emma.marketing@company.com', '+911234567824', NULL, NULL, '2023-02-01', 50000.00, 'PERMANENT', TRUE, '321 Marketing St', 'Bangalore', 'KA', 'India', 'Tom Marketer +919876543213', NOW(), NOW()),
('EMP-005', 'Chris', 'Support', 'chris.support@company.com', '+911234567825', NULL, NULL, '2023-08-15', 40000.00, 'PERMANENT', TRUE, '654 Support Drive', 'Bangalore', 'KA', 'India', 'Alice Support +919876543214', NOW(), NOW());

-- Demo Invoices (10)
INSERT INTO invoices (invoice_number, customer_id, project_id, issue_date, due_date, subtotal, tax_amount, discount_amount, total_amount, paid_amount, pending_amount, status, notes, created_at, updated_at) VALUES
('INV-001', 1, 1, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE, 4500.00, 810.00, 310.00, 5000.00, 5000.00, 0.00, 'PAID', 'Website redesign phase 1', NOW(), NOW()),
('INV-002', 2, 2, CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE + INTERVAL '10 days', 6750.00, 1215.00, 465.00, 7500.00, 0.00, 7500.00, 'PENDING', 'Mobile app development sprint 1', NOW(), NOW()),
('INV-003', 3, 3, CURRENT_DATE - INTERVAL '25 days', CURRENT_DATE + INTERVAL '5 days', 2880.00, 518.40, 198.40, 3200.00, 3200.00, 0.00, 'PAID', 'Cloud migration planning', NOW(), NOW()),
('INV-004', 4, 4, CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE + INTERVAL '15 days', 10800.00, 1944.00, 1344.00, 12000.00, 0.00, 12000.00, 'PENDING', 'ERP implementation phase 1', NOW(), NOW()),
('INV-005', 5, 5, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '30 days', 5850.00, 1053.00, 403.00, 6500.00, 6500.00, 0.00, 'PAID', 'API integration and payment gateway', NOW(), NOW()),
('INV-006', 1, NULL, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '20 days', 8010.00, 1441.80, 552.80, 8900.00, 0.00, 8900.00, 'PENDING', 'Support and maintenance', NOW(), NOW()),
('INV-007', 2, 2, CURRENT_DATE - INTERVAL '35 days', CURRENT_DATE - INTERVAL '5 days', 3780.00, 680.40, 260.40, 4200.00, 4200.00, 0.00, 'PAID', 'Mobile app development sprint 2', NOW(), NOW()),
('INV-008', 3, 3, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '25 days', 9900.00, 1782.00, 1482.00, 11000.00, 0.00, 11000.00, 'PENDING', 'Cloud migration execution', NOW(), NOW()),
('INV-009', 4, 4, CURRENT_DATE - INTERVAL '18 days', CURRENT_DATE + INTERVAL '12 days', 4950.00, 891.00, 341.00, 5500.00, 5500.00, 0.00, 'PAID', 'ERP implementation phase 2', NOW(), NOW()),
('INV-010', 5, NULL, CURRENT_DATE - INTERVAL '12 days', CURRENT_DATE + INTERVAL '18 days', 8820.00, 1588.20, 608.20, 9800.00, 0.00, 9800.00, 'PENDING', 'Additional services and support', NOW(), NOW());

-- Demo Tickets (3)
INSERT INTO tickets (ticket_number, title, description, customer_id, project_id, created_by, assigned_to_id, priority, status, category, due_date, sla_hours, created_at, updated_at) VALUES
('TKT-001', 'Payment Gateway Integration', 'Integrate Stripe payment gateway with existing system for online transactions', 1, 1, 'john.manager@company.com', 2, 'HIGH', 'OPEN', 'FEATURE_REQUEST', CURRENT_DATE + INTERVAL '7 days', 48, NOW(), NOW()),
('TKT-002', 'Email Notification Bug', 'Fix email notifications not sending correctly in production environment', 2, 2, 'sarah.dev@company.com', 1, 'MEDIUM', 'IN_PROGRESS', 'BUG', CURRENT_DATE + INTERVAL '3 days', 24, NOW(), NOW()),
('TKT-003', 'Performance Optimization', 'Optimize database queries for monthly reports to improve performance', 4, 4, 'chris.support@company.com', 2, 'LOW', 'OPEN', 'IMPROVEMENT', CURRENT_DATE + INTERVAL '14 days', 72, NOW(), NOW());
