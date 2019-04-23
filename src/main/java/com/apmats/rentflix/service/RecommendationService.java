package com.apmats.rentflix.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.apmats.rentflix.exception.NoCustomerInteractionsException;
import com.apmats.rentflix.exception.RecommendationComputationException;
import com.apmats.rentflix.exception.ResourceNotFoundException;
import com.apmats.rentflix.model.Customer;
import com.apmats.rentflix.model.Film;
import com.apmats.rentflix.model.Rental;
import com.apmats.rentflix.repository.CustomerRepository;
import com.apmats.rentflix.repository.FilmRepository;
import com.apmats.rentflix.repository.RentalRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smile.imputation.KNNImputation;

@Service
public class RecommendationService {

	private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

	private CustomerRepository customerRepository;
	private RentalRepository rentalRepository;
	private FilmRepository filmRepository;

	@Autowired
	public RecommendationService(CustomerRepository customerRepository, RentalRepository rentalRepository,
			FilmRepository filmRepository) {
		this.customerRepository = customerRepository;
		this.rentalRepository = rentalRepository;
		this.filmRepository = filmRepository;
	}

	// A toy implementation of a recommender feature
	// Inefficient as it runs the recommendation algorithm for every such request
	// In production, we'd automate this recommendation generation process, only
	// running it
	// pccasionally to refresh with new data,
	// and we would persist the recommendations in a data store to have available
	// for recommendations

	// However, the basic functionality is regardless provided here.
	// Recommendations are generated through a k nearest neighboors algorithm.
	// This means, for a given item or user, find the k most similar ones to it.
	// Calculate the expected value of the missing values based on the values that
	// exist on
	// the retrieved similar items.

	// Arbitrarily we decide to use 10 nearest neighbours for each user,
	// but in an actual usecase for this we'd use our data and compute some metrics
	// to decide what the best value for this would be.

	// This family of collaborative filtering algorithms are somewhat outdated
	// but it should be faster than something involving matrix factorization
	// (which is state of the art) and thus more suitable to run on the fly

	// Our implementation simply gathers the appropriate interaction data, forms the
	// matrix
	// and offloads the actual computation to a library.

	// We return a maximum of maxSuggestions. More relevant suggestions are higher
	// in the
	// result list, but potentially resorts to returning movies with even a single
	// rental.

	public List<Long> getSuggestedMoviesWithTitles(Long customerId, int maxSuggestions) {
		Optional<Customer> maybeCustomer = this.customerRepository.findById(customerId);
		if (!maybeCustomer.isPresent())
			throw new ResourceNotFoundException("Customer with id " + customerId + " not found");
		List<Rental> customerInteractions = rentalRepository.findByCustomer(maybeCustomer.get());
		if (customerInteractions.isEmpty())
			throw new NoCustomerInteractionsException(
					"Cannot recommend for user that has no history of preferred movies!");

		// Customers and films with no interaction can't be included for the algorithm
		// to work
		List<Customer> allCustomers = new ArrayList<>(customerRepository.findRentedAtLeastOnce());
		List<Film> allFilms = new ArrayList<>(filmRepository.findInteractedWith());
		List<Rental> allRentals = new ArrayList<>(rentalRepository.findAll());

		double[][] interactions = new double[allCustomers.size()][allFilms.size()];
		Map<Long, Integer> customerIdToIndex = new HashMap<>();
		Map<Long, Integer> filmIdToIndex = new HashMap<>();

		for (int i = 0; i < allCustomers.size(); i++)
			customerIdToIndex.put(allCustomers.get(i).getId(), i);

		for (int j = 0; j < allFilms.size(); j++)
			filmIdToIndex.put(allFilms.get(j).getId(), j);

		for (int i = 0; i < allCustomers.size(); i++)
			for (int j = 0; j < allFilms.size(); j++)
				interactions[i][j] = Double.NaN;

		Set<Long> moviesToNotRecommend = new HashSet<>();
		for (Rental rental : allRentals) {
			Integer x = customerIdToIndex.get(rental.getCustomer().getId());
			Integer y = filmIdToIndex.get(rental.getPhysicalMedia().getFilm().getId());
			// note the movies that the customer has already rented
			// in order to avoid recommending them down the line
			if (rental.getCustomer().getId().equals(customerId))
				moviesToNotRecommend.add(rental.getPhysicalMedia().getFilm().getId());
			interactions[x][y] = 1.0;
		}
		// Hard coded use of 10 neighbours, should instead be the result of
		// experimentation, and could probably be configurable then
		Integer k = Math.max(1, allCustomers.size() / 10);
		KNNImputation knn = new KNNImputation(k);
		try {
			knn.impute(interactions);
		} catch (Exception ex) {
			logger.error("Failed to predict film ratings", ex);
			throw new RecommendationComputationException(
					"Failed to compute recommended films due to an internal error. Our engineers will be notified with the relevant logs");
		}

		Map<Film, Double> filmToScore = new LinkedHashMap<>();
		for (int j = 0; j < allFilms.size(); j++) {
			filmToScore.put(allFilms.get(j), interactions[customerIdToIndex.get(customerId)][j]);
		}

		List<Film> filmSuggestions = filmToScore.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.filter(e -> !moviesToNotRecommend.contains(e.getKey().getId())).limit(maxSuggestions)
				.map(e -> e.getKey()).collect(Collectors.toList());

		return filmSuggestions.stream().map(f -> f.getId()).collect(Collectors.toList());
	}

}