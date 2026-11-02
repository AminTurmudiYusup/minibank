package com.example.minibank.customer.service;
import com.example.minibank.auth.entity.User;
import com.example.minibank.common.exception.DuplicateResourceException;
import com.example.minibank.customer.entity.Customer;
import com.example.minibank.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer createCustomer(
            User user,
            String fullName,
            String phone,
            String address) {

        if (customerRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("Phone number already exists");
        }

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setAddress(address);

        return customerRepository.save(customer);
    }
}
