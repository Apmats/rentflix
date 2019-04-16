package com.apmats.rentflix.repository;

import com.apmats.rentflix.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilmRepository extends JpaRepository<Film, Long> {
    Optional<Film> findById(Long filmId);

    // A natural join to get movies that have at least one interaction from users
    @Query(value = "SELECT DISTINCT f FROM Film f, Rental r, PhysicalMedia pm WHERE f = pm.film AND r.physicalMedia = pm")
    List<Film> findInteractedWith();
}
