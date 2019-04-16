package com.apmats.rentflix.repository;

import com.apmats.rentflix.model.Customer;
import com.apmats.rentflix.model.PhysicalMedia;
import com.apmats.rentflix.model.Rental;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByPhysicalMediaAndReturnDateIsNull(PhysicalMedia physicalMedia);

    List<Rental> findByCustomer(Customer customer);
}
