package com.apmats.rentflix.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

@Entity
public class Rental implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne
    // @JoinColumn
    private Customer customer;

    @ManyToOne
    // @JoinColumn
    private PhysicalMedia physicalMedia;

    private LocalDate rentDate;
    private LocalDate returnDate;

    @Version
    @Column(columnDefinition = "integer DEFAULT 0", nullable = false)
    private Long version = 0L;

    public Rental() {
    }

    public Rental(Customer customer, PhysicalMedia physicalMedia, LocalDate rentDate) {
        this.customer = customer;
        this.physicalMedia = physicalMedia;
        this.rentDate = rentDate;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public PhysicalMedia getPhysicalMedia() {
        return this.physicalMedia;
    }

    public LocalDate getRentDate() {
        return this.rentDate;
    }

    public LocalDate getReturnDate() {
        return this.returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Rental)) {
            return false;
        }
        Rental rental = (Rental) o;
        return Objects.equals(customer, rental.customer) && Objects.equals(physicalMedia, rental.physicalMedia)
                && Objects.equals(rentDate, rental.rentDate) && Objects.equals(returnDate, rental.returnDate)
                && Objects.equals(version, rental.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, physicalMedia, rentDate, returnDate, version);
    }

    @Override
    public String toString() {
        return "{" + " customer='" + getCustomer() + "'" + ", physicalMedia='" + getPhysicalMedia() + "'"
                + ", rentDate='" + getRentDate() + "'" + ", returnDate='" + getReturnDate() + "'" + "}";
    }
}
