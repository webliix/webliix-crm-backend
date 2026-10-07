package com.webliix.hr.employee.service.impl;

import com.webliix.hr.department.entity.Department;
import com.webliix.hr.department.repository.DepartmentRepository;
import com.webliix.hr.designation.entity.Designation;
import com.webliix.hr.designation.repository.DesignationRepository;
import com.webliix.hr.employee.dto.EmployeeRequest;
import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.employee.dto.EmployeeStatisticsResponse;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.hr.employee.service.EmployeeService;
import com.webliix.hr.employee.util.EmployeeCodeGenerator;
import com.webliix.security.entity.Role;
import com.webliix.security.entity.User;
import com.webliix.security.repository.RoleRepository;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.webliix.notifications.service.EmailService emailService;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required for employee creation");
        }
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (employeeRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("An employee profile already exists with email: " + normalizedEmail);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));

        String prefix = EmployeeCodeGenerator.currentPrefix();
        Employee last = employeeRepository.findTopByEmployeeCodeStartingWithOrderByIdDesc(prefix);
        String employeeCode = last != null
                ? EmployeeCodeGenerator.next(last.getEmployeeCode())
                : prefix + "000001";

        Boolean active = request.getActive();
        if (active == null) {
            active = Boolean.TRUE;
        }

        // Automated account provisioning with password generation
        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = generateSecurePassword();
        }

        User linkedUser = null;
        if (userRepository.existsByEmail(normalizedEmail)) {
            // If user already exists, link to it and ensure EMPLOYEE role is assigned
            linkedUser = userRepository.findByEmail(normalizedEmail).get();
            Role employeeRole = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new ResourceNotFoundException("Role EMPLOYEE not found"));
            if (linkedUser.getRoles() != null && !linkedUser.getRoles().contains(employeeRole)) {
                linkedUser.getRoles().add(employeeRole);
            }
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                linkedUser.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            userRepository.save(linkedUser);
        } else {
            Role employeeRole = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new ResourceNotFoundException("Role EMPLOYEE not found"));

            User user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(normalizedEmail)
                    .password(passwordEncoder.encode(rawPassword))
                    .phone(request.getPhone())
                    .department(department.getDepartmentName())
                    .jobTitle(designation.getDesignationName())
                    .enabled(active)
                    .emailVerified(true) // Pre-verified since provisioned by admin
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .roles(Set.of(employeeRole))
                    .build();
            linkedUser = userRepository.save(user);
            log.info("Provisioned user login account for employee: {}", normalizedEmail);
        }

        Employee employee = Employee.builder()
                .employeeCode(employeeCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(normalizedEmail)
                .phone(request.getPhone())
                .department(department)
                .designation(designation)
                .joiningDate(request.getJoiningDate())
                .salary(request.getSalary())
                .employmentType(request.getEmploymentType())
                .active(active)
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .emergencyContact(request.getEmergencyContact())
                .user(linkedUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Employee saved = employeeRepository.save(employee);

        // Send automated welcome email with credentials from noreply@webliix.com
        sendEmployeeWelcomeEmail(saved, rawPassword);

        return toResponse(saved);
    }

    @Override
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));

        String oldEmail = employee.getEmail();
        String newEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : oldEmail;

        if (!oldEmail.equalsIgnoreCase(newEmail)) {
            if (employeeRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already in use by another employee: " + newEmail);
            }
            employee.setEmail(newEmail);
            if (employee.getUser() != null) {
                User user = employee.getUser();
                user.setEmail(newEmail);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setDepartment(department);
        employee.setDesignation(designation);
        employee.setJoiningDate(request.getJoiningDate());
        employee.setSalary(request.getSalary());
        employee.setEmploymentType(request.getEmploymentType());

        if (request.getActive() != null) {
            employee.setActive(request.getActive());
            // Sync status to linked user login account
            if (employee.getUser() != null) {
                User user = employee.getUser();
                user.setEnabled(request.getActive());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        }

        employee.setAddress(request.getAddress());
        employee.setCity(request.getCity());
        employee.setState(request.getState());
        employee.setCountry(request.getCountry());
        employee.setEmergencyContact(request.getEmergencyContact());
        employee.setUpdatedAt(LocalDateTime.now());

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (employee.getUser() != null) {
                User user = employee.getUser();
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            } else {
                // If user didn't exist previously, provision it now
                Role employeeRole = roleRepository.findByName("EMPLOYEE")
                        .orElseThrow(() -> new ResourceNotFoundException("Role EMPLOYEE not found"));
                User user = User.builder()
                        .firstName(employee.getFirstName())
                        .lastName(employee.getLastName())
                        .email(employee.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .phone(employee.getPhone())
                        .department(department.getDepartmentName())
                        .jobTitle(designation.getDesignationName())
                        .enabled(employee.getActive())
                        .emailVerified(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .roles(Set.of(employeeRole))
                        .build();
                User savedUser = userRepository.save(user);
                employee.setUser(savedUser);
            }
        }

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (employee.getUser() != null) {
            User user = employee.getUser();
            user.setEnabled(false);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
        employeeRepository.deleteById(id);
    }

    @Override
    public EmployeeStatisticsResponse getEmployeeStatistics() {
        List<Employee> employees = employeeRepository.findAll();
        long total = employees.size();
        long active = employees.stream().filter(emp -> Boolean.TRUE.equals(emp.getActive())).count();
        long inactive = total - active;
        long newThisMonth = employees.stream()
                .map(Employee::getJoiningDate)
                .filter(date -> date != null && date.getMonthValue() == LocalDate.now().getMonthValue() && date.getYear() == LocalDate.now().getYear())
                .count();

        EmployeeStatisticsResponse response = new EmployeeStatisticsResponse();
        response.setTotalEmployees(total);
        response.setActiveEmployees(active);
        response.setInactiveEmployees(inactive);
        response.setNewThisMonth(newThisMonth);
        return response;
    }

    private EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        if (employee.getDepartment() != null) {
            response.setDepartmentId(employee.getDepartment().getId());
            response.setDepartmentName(employee.getDepartment().getDepartmentName());
        }
        if (employee.getDesignation() != null) {
            response.setDesignationId(employee.getDesignation().getId());
            response.setDesignationName(employee.getDesignation().getDesignationName());
        }
        response.setJoiningDate(employee.getJoiningDate());
        response.setSalary(employee.getSalary());
        response.setEmploymentType(employee.getEmploymentType());
        response.setActive(employee.getActive());
        response.setAddress(employee.getAddress());
        response.setCity(employee.getCity());
        response.setState(employee.getState());
        response.setCountry(employee.getCountry());
        response.setEmergencyContact(employee.getEmergencyContact());
        response.setUserId(employee.getUser() != null ? employee.getUser().getId() : null);
        response.setHasLoginAccount(employee.getUser() != null);
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }

    private String generateSecurePassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder("Wbx@");
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        sb.append("!");
        return sb.toString();
    }

    private void sendEmployeeWelcomeEmail(Employee employee, String rawPassword) {
        try {
            String subject = "Welcome to Webliix – Your Employee Account Credentials";
            String template = loadEmailTemplate("employee-welcome");
            String fullName = (employee.getFirstName() != null ? employee.getFirstName() : "") + " "
                    + (employee.getLastName() != null ? employee.getLastName() : "").trim();
            if (fullName.isBlank()) {
                fullName = "Team Member";
            }

            String htmlBody;
            if (template != null) {
                htmlBody = template
                        .replace("{{NAME}}", fullName)
                        .replace("{{EMPLOYEE_CODE}}", employee.getEmployeeCode() != null ? employee.getEmployeeCode() : "N/A")
                        .replace("{{EMAIL}}", employee.getEmail())
                        .replace("{{PASSWORD}}", rawPassword);
            } else {
                htmlBody = "<html><body>"
                        + "<h2>Welcome to Webliix!</h2>"
                        + "<p>Dear " + fullName + ",</p>"
                        + "<p>Your employee account has been created. Here are your credentials:</p>"
                        + "<ul>"
                        + "<li><strong>Employee Code:</strong> " + employee.getEmployeeCode() + "</li>"
                        + "<li><strong>Portal:</strong> <a href='https://employee.webliix.com'>https://employee.webliix.com</a></li>"
                        + "<li><strong>Email:</strong> " + employee.getEmail() + "</li>"
                        + "<li><strong>Password:</strong> " + rawPassword + "</li>"
                        + "</ul>"
                        + "<p>Please log in and update your password.</p>"
                        + "<p>Regards,<br>Webliix HR & Operations</p>"
                        + "</body></html>";
            }

            emailService.sendAutomatedHtmlEmail(employee.getEmail(), subject, htmlBody);
            log.info("Dispatched automated employee welcome email to {}", employee.getEmail());
        } catch (Exception ex) {
            log.warn("Could not dispatch automated employee welcome email to {}: {}", employee.getEmail(), ex.getMessage());
        }
    }

    private String loadEmailTemplate(String templateName) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/templates/emails/" + templateName + ".html");
            if (is != null) {
                return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Failed to load email template: {}", templateName, e);
        }
        return null;
    }
}
