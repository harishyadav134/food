package com.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Column(name = "fullname")
    private String fullname;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits")
    @Column(name = "mobilenumber")
    private String mobilenumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Column(name = "email")
    private String email;

    @NotBlank(message = "Address is required")
    @Column(name = "address")
    private String address;

    @NotNull(message = "Product ID is required")
    @Min(value = 1, message = "Invalid product ID")
    @Column(name = "product_id")
    private Long productId;

    @NotBlank(message = "Product name is required")
    @Column(name = "product_name")
    private String productName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity")
    private int quantity;
}