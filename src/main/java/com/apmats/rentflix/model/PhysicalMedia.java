package com.apmats.rentflix.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

import javax.persistence.*;

@Entity
@Table(name = "physical_media")
public class PhysicalMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "film_id")
    private Film film;

    private String serialNumber;

    private Boolean available = true;

    @Version
    @Column(columnDefinition = "integer DEFAULT 0", nullable = false)
    private Long version = 0L;

    public PhysicalMedia() {
    }

    public PhysicalMedia(Long id, Film film, String serialNumber, Boolean available) {
        this.id = id;
        this.film = film;
        this.serialNumber = serialNumber;
        this.available = available;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Film getFilm() {
        return this.film;
    }

    public void setFilm(Film film) {
        this.film = film;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Boolean isAvailable() {
        return this.available;
    }

    public Boolean getAvailable() {
        return this.available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public PhysicalMedia id(Long id) {
        this.id = id;
        return this;
    }

    public PhysicalMedia film(Film film) {
        this.film = film;
        return this;
    }

    public PhysicalMedia serialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    public PhysicalMedia available(Boolean available) {
        this.available = available;
        return this;
    }

    public PhysicalMedia version(Long version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PhysicalMedia)) {
            return false;
        }
        PhysicalMedia physicalMedia = (PhysicalMedia) o;
        return Objects.equals(id, physicalMedia.id) && Objects.equals(film, physicalMedia.film)
                && Objects.equals(serialNumber, physicalMedia.serialNumber)
                && Objects.equals(available, physicalMedia.available) && Objects.equals(version, physicalMedia.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, film, serialNumber, available, version);
    }

    @Override
    public String toString() {
        return "{" + " id='" + getId() + "'" + ", film='" + getFilm() + "'" + ", serialNumber='" + getSerialNumber()
                + "'" + ", available='" + isAvailable() + "'" + "}";
    }

}
