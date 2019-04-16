package com.apmats.rentflix.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.apmats.rentflix.repository.FilmRepository;
import com.apmats.rentflix.repository.PhysicalMediaRepository;
import com.apmats.rentflix.repository.CustomerRepository;
import com.apmats.rentflix.repository.RentalRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmats.rentflix.model.Film;
import com.apmats.rentflix.model.PhysicalMedia;
import com.apmats.rentflix.exception.NoCopyAvailableException;
import com.apmats.rentflix.exception.ResourceNotFoundException;
import com.apmats.rentflix.model.Customer;
import com.apmats.rentflix.model.Rental;

import static  java.time.temporal.ChronoUnit.DAYS;

@Service
public class RentService {

	private static final Logger logger = LoggerFactory.getLogger(RentService.class);	

	private final Clock clock;
	private final RentalRepository rentalRepository;
	private final FilmRepository filmRepository;
	private final PhysicalMediaRepository physicalMediaRepository;
	private final CustomerRepository customerRepository;

	@Autowired
	public RentService(Clock clock, RentalRepository rentalRepository, FilmRepository filmRepository,
			PhysicalMediaRepository physicalMediaRepository, CustomerRepository customerRepository) {
		this.clock = clock;
		this.rentalRepository = rentalRepository;
		this.filmRepository = filmRepository;
		this.physicalMediaRepository = physicalMediaRepository;
		this.customerRepository = customerRepository;
	}

	// A method that attempts to find a copy of each film and assign it to the customer
	// If it succeeds it returns the total initial cost
	// Transactional to avoid double assignments of the same physical copy to multiple concurrent customers

	@Transactional
	public Map.Entry<Double,List<Long>> rentFilms(Long customerId, List<Long> filmIds) {
		// find an available copy for each film id
		Customer customer = getCustomer(customerId);
		Double totalCost = 0.0;
		List<PhysicalMedia> rentedCopies = new ArrayList<>();
		for (Long filmId : filmIds) {
			Optional<Film> maybeFilm = filmRepository.findById(filmId);
			if (!maybeFilm.isPresent())
				throw new ResourceNotFoundException("Tried to rent film with ID " + filmId + " but no such film exists");
			Film film = maybeFilm.get();
			List<PhysicalMedia> availableCopies = physicalMediaRepository.findByAvailableTrueAndFilm(film);
			if (availableCopies.isEmpty())
				throw new NoCopyAvailableException("No copy available to rent for film with ID " + filmId);
			PhysicalMedia copyToRent = availableCopies.get(0);
			copyToRent.setAvailable(false);
			rentedCopies.add(copyToRent);
			Rental rental = new Rental(customer, copyToRent, LocalDate.now(clock));
			rentalRepository.save(rental);
			physicalMediaRepository.save(copyToRent);
			totalCost += film.getRecencyType().getInitialCharge();
			customer.setBonusPoints(customer.getBonusPoints()+film.getRecencyType().getBonus());
		}
		customerRepository.save(customer);
		return new AbstractMap.SimpleEntry<Double,List<Long>>(totalCost, rentedCopies.stream().map(PhysicalMedia::getId).collect(Collectors.toList()));

	}

	// A method to be called to return a set of physical copies of films
	// Calculates the total surcharge based on the current date and the rent date

	@Transactional
	public Double returnMedia(Long customerId, List<Long> physicalMediaIds) {
		Customer customer = getCustomer(customerId);
		Double totalSurcharge = 0.0;
		for (Long copyId : physicalMediaIds) {
			Optional<PhysicalMedia> maybeCopy = physicalMediaRepository.findById(copyId);
			if (!maybeCopy.isPresent())
				throw new ResourceNotFoundException("Tried to return copy with ID " + copyId + " but no such copy exists");
			if (maybeCopy.get().isAvailable())
				throw new ResourceNotFoundException("Tried to return copy with ID " + copyId + " but no active rental record exists for that copy");
			// there should be a single active rental for a given copy
			List<Rental> singleRentalList = rentalRepository.findByPhysicalMediaAndReturnDateIsNull(maybeCopy.get());
			if (singleRentalList.isEmpty()) {
				logger.error("Inconsistent database state detected, a copy with id " + copyId + " appears rented out but no corresponding rental entry exists");
				throw new ResourceNotFoundException("Tried to return copy with ID " + copyId + " but no record of the rental action exists");
			}
			Rental rental = singleRentalList.get(0);
			rental.setReturnDate(LocalDate.now(clock));
			// offloads calculations to the billing strategy inside the film's recency type
			totalSurcharge += rental.getPhysicalMedia().getFilm().getRecencyType().calculateSurcharge(DAYS.between(rental.getRentDate(), rental.getReturnDate()));
			rental.getPhysicalMedia().setAvailable(true);
			rentalRepository.save(rental);
			physicalMediaRepository.save(rental.getPhysicalMedia());
		}
		return totalSurcharge;
	}

	private Customer getCustomer(Long customerId) {
		Optional<Customer> maybeCustomer = customerRepository.findById(customerId);
		if (!maybeCustomer.isPresent())
			throw new ResourceNotFoundException("No customer with id " + customerId);
		return maybeCustomer.get();
	}

}