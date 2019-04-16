package com.apmats.rentflix.controller;

import com.apmats.rentflix.exception.ResourceNotFoundException;
import com.apmats.rentflix.model.Customer;
import com.apmats.rentflix.repository.CustomerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class CustomerDetailsController {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerDetailsController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // A simple route that returns a few details for the user,
    // currently only the full display name and the bonus points acquired
    @GetMapping(value = "/customer/{customerId}/details")
    public ResponseEntity<Map<String, String>> getDetails(@PathVariable Long customerId) {
        Optional<Customer> maybeCustomer = this.customerRepository.findById(customerId);
        if (!maybeCustomer.isPresent())
            new ResourceNotFoundException("Customer with id " + customerId + " not found");
        Customer customer = maybeCustomer.get();
        Map<String, String> respMap = new HashMap<>();
        respMap.put("full_name", customer.getFullDisplayName());
        respMap.put("bonus_points", customer.getBonusPoints().toString());
        return ResponseEntity.ok(respMap);
    }
}
