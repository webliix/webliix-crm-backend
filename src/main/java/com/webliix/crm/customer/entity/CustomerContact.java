package com.webliix.crm.customer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    private String name;

    private String designation;

    private String email;

    private String phone;

    @Column(name = "is_primary")
    private Boolean isPrimary;
}
