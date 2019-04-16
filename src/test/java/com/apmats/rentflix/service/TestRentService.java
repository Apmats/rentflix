package com.apmats.rentflix.service;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.apmats.rentflix.exception.NoCopyAvailableException;
import com.apmats.rentflix.exception.ResourceNotFoundException;
import com.apmats.rentflix.model.Customer;
import com.apmats.rentflix.model.Film;
import com.apmats.rentflix.model.PhysicalMedia;
import com.apmats.rentflix.model.Rental;
import com.apmats.rentflix.repository.CustomerRepository;
import com.apmats.rentflix.repository.FilmRepository;
import com.apmats.rentflix.repository.PhysicalMediaRepository;
import com.apmats.rentflix.repository.RentalRepository;
import com.apmats.rentflix.util.RecencyType;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRentService {

    @Autowired
    private RentService rentService;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private RentalRepository rentalRepository;

    @MockBean
    private PhysicalMediaRepository physicalMediaRepository;

    @MockBean
    private FilmRepository filmRepository;

    @MockBean
    private Clock clock;

    private Customer customer;
    private Film film1, film2;
    private PhysicalMedia copyOfFilm1, copyOfFilm2;

    @Before
    public void setupTest() {
        customer = new Customer(1L, "Test", "Test", "Testing", 10L);
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        film1 = new Film(1L, "Test title 1", RecencyType.RECENT);
        when(filmRepository.findById(film1.getId())).thenReturn(Optional.of(film1));
        film2 = new Film(2L, "Test title 2", RecencyType.REGULAR);
        when(filmRepository.findById(film2.getId())).thenReturn(Optional.of(film2));

        copyOfFilm1 = new PhysicalMedia(1L, film1, "1", true);
        when(physicalMediaRepository.findByAvailableTrueAndFilm(film1)).thenReturn(Arrays.asList(copyOfFilm1));
        when(physicalMediaRepository.findById(copyOfFilm1.getId())).thenReturn(Optional.of(copyOfFilm1));
        copyOfFilm2 = new PhysicalMedia(2L, film2, "2", true);
        when(physicalMediaRepository.findByAvailableTrueAndFilm(film2)).thenReturn(Arrays.asList(copyOfFilm2));
        when(physicalMediaRepository.findById(copyOfFilm2.getId())).thenReturn(Optional.of(copyOfFilm2));

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    public void rentingFilmsShouldReturnTheTotalPrice() {
        assertEquals("Expected calculated total price to be equal to the sum of the initial charges",
                (Double) (film1.getRecencyType().getInitialCharge() + film2.getRecencyType().getInitialCharge()),
                rentService.rentFilms(customer.getId(), Arrays.asList(film1.getId(), film2.getId())).getKey());
    }

    @Test
    public void rentingFilmsShouldReturnIdsOfRentedCopies() {
        assertEquals("Expected list of returned IDs to match the IDs of the copies that should be rented",
                Arrays.asList(copyOfFilm1.getId(), copyOfFilm2.getId()),
                rentService.rentFilms(customer.getId(), Arrays.asList(film1.getId(), film2.getId())).getValue());
    }

    @Test
    public void rentingFilmsShouldAddBonusPoints() {
        Long initialBonus = 10L;
        customer.setBonusPoints(initialBonus);
        rentService.rentFilms(customer.getId(), Arrays.asList(film1.getId()));

        assertEquals("Expected the appropriate amount of bonus points to be added to the customer's account",
                (Long) (initialBonus + film1.getRecencyType().getBonus()), customer.getBonusPoints());
    }

    @Test
    public void rentingAFilmShouldReserveACopy() {
        rentService.rentFilms(customer.getId(), Arrays.asList(film1.getId()));

        assertTrue("Expected copy of film to be marked as not available", !copyOfFilm1.getAvailable());
    }

    @Test
    public void rentingAFilmShouldGenerateARentalEntry() {
        rentService.rentFilms(customer.getId(), Arrays.asList(film1.getId()));

        verify(rentalRepository).save(any(Rental.class));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void rentingOnANonExistentUserShouldThrow() {
        Long invalidCustomerId = 100L;
        when(customerRepository.findById(invalidCustomerId)).thenReturn(Optional.empty());

        rentService.rentFilms(invalidCustomerId, Arrays.asList(film1.getId()));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void rentingANonExistentFilmShouldThrow() {
        Long invalidFilmId = 100L;
        when(filmRepository.findById(invalidFilmId)).thenReturn(Optional.empty());

        rentService.rentFilms(customer.getId(), Arrays.asList(invalidFilmId));
    }

    @Test(expected = NoCopyAvailableException.class)
    public void rentingAFilmWithNoCopiesAvailableShouldThrow() {
        when(physicalMediaRepository.findByAvailableTrueAndFilm(film1)).thenReturn(new ArrayList<>());

        rentService.rentFilms(customer.getId(), Arrays.asList(film1.getId()));
    }

    @Test
    public void returningACopyLateShouldIncurSurcharge() {
        Rental rental = new Rental(customer, copyOfFilm1, LocalDate.now());
        when(rentalRepository.findByPhysicalMediaAndReturnDateIsNull(copyOfFilm1)).thenReturn(Arrays.asList(rental));
        copyOfFilm1.setAvailable(false);

        when(clock.instant())
                .thenReturn(Instant.now().plus(film1.getRecencyType().getInitialPeriodInDays() + 1, ChronoUnit.DAYS));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        assertEquals("Expected surcharge to be equal to the surcharge for the movie type",
                (Double) (film1.getRecencyType()
                        .calculateSurcharge(film1.getRecencyType().getInitialPeriodInDays() + 1)),
                rentService.returnMedia(customer.getId(), Arrays.asList(copyOfFilm1.getId())));
    }

    @Test
    public void returningACopyShouldRenderItAvailable() {
        Rental rental = new Rental(customer, copyOfFilm1, LocalDate.now());
        when(rentalRepository.findByPhysicalMediaAndReturnDateIsNull(copyOfFilm1)).thenReturn(Arrays.asList(rental));
        copyOfFilm1.setAvailable(false);

        rentService.returnMedia(customer.getId(), Arrays.asList(copyOfFilm1.getId()));
        verify(physicalMediaRepository).save(copyOfFilm1);
        assertTrue("Expected copy of film to be marked as available", copyOfFilm1.isAvailable());
    }

    @Test
    public void returningACopyShouldUpdateRentalRecords() {
        Rental rental = new Rental(customer, copyOfFilm1, LocalDate.now());
        when(rentalRepository.findByPhysicalMediaAndReturnDateIsNull(copyOfFilm1)).thenReturn(Arrays.asList(rental));
        copyOfFilm1.setAvailable(false);

        rentService.returnMedia(customer.getId(), Arrays.asList(copyOfFilm1.getId()));
        verify(rentalRepository).save(rental);
        assertEquals("Expected return date for rental record to be set correctly", rental.getReturnDate(),
                LocalDate.now(clock));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void returningOnAnNonExistentUserShouldThrow() {
        Rental rental = new Rental(customer, copyOfFilm1, LocalDate.now());
        when(rentalRepository.findByPhysicalMediaAndReturnDateIsNull(copyOfFilm1)).thenReturn(Arrays.asList(rental));
        copyOfFilm1.setAvailable(false);

        Long invalidCustomerId = 100L;
        when(customerRepository.findById(invalidCustomerId)).thenReturn(Optional.empty());
        rentService.returnMedia(invalidCustomerId, Arrays.asList(copyOfFilm1.getId()));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void returningANonExistentCopyShouldThrow() {
        Rental rental = new Rental(customer, copyOfFilm1, LocalDate.now());
        when(rentalRepository.findByPhysicalMediaAndReturnDateIsNull(copyOfFilm1)).thenReturn(Arrays.asList(rental));
        copyOfFilm1.setAvailable(false);

        Long invalidCopyId = 100L;
        when(physicalMediaRepository.findById(invalidCopyId)).thenReturn(Optional.empty());
        rentService.returnMedia(customer.getId(), Arrays.asList(invalidCopyId));
    }
}
