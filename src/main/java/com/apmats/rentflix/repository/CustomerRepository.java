package com.apmats.rentflix.repository;

import com.apmats.rentflix.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findById(Long customerId);

    // A natural join to get users that have at least some activity of renting
    // movies
    @Query(value = "SELECT DISTINCT c FROM Customer c, Rental r WHERE c = r.customer")
    List<Customer> findRentedAtLeastOnce();
}
