package com.apmats.rentflix.repository;

import com.apmats.rentflix.model.Film;
import com.apmats.rentflix.model.PhysicalMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhysicalMediaRepository extends JpaRepository<PhysicalMedia, Long> {
    List<PhysicalMedia> findByAvailableTrueAndFilm(Film film);

    Optional<PhysicalMedia> findById(Long physicalMediaId);
}
