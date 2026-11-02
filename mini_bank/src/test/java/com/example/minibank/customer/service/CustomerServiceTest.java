package com.example.minibank.customer.service;

import com.example.minibank.auth.entity.User;
import com.example.minibank.common.exception.DuplicateResourceException;
import com.example.minibank.customer.entity.Customer;
import com.example.minibank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldCreateCustomerSuccessfully() {

        // Arrange
        User user = new User();
        user.setUsername("john");

        when(customerRepository.existsByPhone("08123456789"))
                .thenReturn(false);

        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Customer result = customerService.createCustomer(
                user,
                "John Doe",
                "08123456789",
                "Jakarta"
        );

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals("John Doe", result.getFullName());
        assertEquals("08123456789", result.getPhone());
        assertEquals("Jakarta", result.getAddress());

        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {

        // Arrange
        User user = new User();

        when(customerRepository.existsByPhone("08123456789"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                DuplicateResourceException.class,
                () -> customerService.createCustomer(
                        user,
                        "John Doe",
                        "08123456789",
                        "Jakarta"
                )
        );

        verify(customerRepository, never()).save(any());
    }
}