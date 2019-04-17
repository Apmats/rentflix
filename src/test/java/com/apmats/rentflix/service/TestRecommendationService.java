package com.apmats.rentflix.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.apmats.rentflix.model.Customer;
import com.apmats.rentflix.model.Film;
import com.apmats.rentflix.model.PhysicalMedia;
import com.apmats.rentflix.model.Rental;
import com.apmats.rentflix.repository.CustomerRepository;
import com.apmats.rentflix.repository.FilmRepository;
import com.apmats.rentflix.repository.RentalRepository;
import com.apmats.rentflix.util.RecencyType;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRecommendationService {

    @Autowired
    private RecommendationService recommendationService;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private RentalRepository rentalRepository;

    @MockBean
    private FilmRepository filmRepository;

    @Test
    public void shouldRecommendAFilmRentedBySimilarCustomers() {
        Customer customer1 = new Customer(1L, "Test", "Test", "Testing", 0L);
        when(customerRepository.findById(customer1.getId())).thenReturn(Optional.of(customer1));
        Customer customer2 = new Customer(2L, "Test", "Test", "Testing", 0L);
        when(customerRepository.findById(customer2.getId())).thenReturn(Optional.of(customer2));
        Customer customer3 = new Customer(3L, "Test", "Test", "Testing", 0L);

        when(customerRepository.findRentedAtLeastOnce()).thenReturn(Arrays.asList(customer1, customer2, customer3));

        Film film1 = new Film(1L, "Test title 1", RecencyType.RECENT);
        when(filmRepository.findById(film1.getId())).thenReturn(Optional.of(film1));
        Film film2 = new Film(2L, "Test title 2", RecencyType.REGULAR);
        when(filmRepository.findById(film2.getId())).thenReturn(Optional.of(film2));
        Film film3 = new Film(3L, "Test title 3", RecencyType.REGULAR);
        when(filmRepository.findById(film3.getId())).thenReturn(Optional.of(film3));
        Film film4 = new Film(4L, "Test title 4", RecencyType.OLD);
        when(filmRepository.findById(film4.getId())).thenReturn(Optional.of(film4));

        PhysicalMedia copyOfFilm1 = new PhysicalMedia(1L, film1, "1", true);
        PhysicalMedia copyOfFilm2 = new PhysicalMedia(2L, film2, "2", true);
        PhysicalMedia copyOfFilm3 = new PhysicalMedia(3L, film3, "3", true);
        PhysicalMedia copyOfFilm4 = new PhysicalMedia(4L, film4, "4", true);

        Rental rental1 = new Rental(customer1, copyOfFilm1, LocalDate.now());
        Rental rental2 = new Rental(customer1, copyOfFilm2, LocalDate.now());

        Rental rental3 = new Rental(customer2, copyOfFilm1, LocalDate.now());
        Rental rental4 = new Rental(customer2, copyOfFilm2, LocalDate.now());

        Rental rental5 = new Rental(customer3, copyOfFilm3, LocalDate.now());
        Rental rental6 = new Rental(customer1, copyOfFilm4, LocalDate.now());

        when(rentalRepository.findByCustomer(customer2)).thenReturn(Arrays.asList(rental3));
        when(rentalRepository.findAll()).thenReturn(Arrays.asList(rental1, rental2, rental3, rental4, rental5, rental6));

        when(filmRepository.findInteractedWith()).thenReturn(Arrays.asList(film1, film2, film3, film4));

        List<Long> recommendations = recommendationService.getSuggestedMoviesWithTitles(customer2.getId(), 1);
        assertEquals("Expected 1 recommendation to be returned", recommendations.size(), 1);
        assertEquals("Expected recommendation for movie with the right id", recommendations.get(0), film4.getId());
    }
}