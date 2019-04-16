package com.apmats.rentflix.model;

import java.util.Objects;

import javax.persistence.*;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String firstName;

    private String lastName;

    private String fullDisplayName;

    @Column(columnDefinition = "integer DEFAULT 0")
    private Long bonusPoints;

    @Version
    @Column(columnDefinition = "integer DEFAULT 0", nullable = false)
    private Long version = 0L;

    public Customer() {
    }

    public Customer(Long id, String firstName, String lastName, String fullDisplayName, Long bonusPoints) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullDisplayName = fullDisplayName;
        this.bonusPoints = bonusPoints;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullDisplayName() {
        return this.fullDisplayName;
    }

    public void setFullDisplayName(String fullDisplayName) {
        this.fullDisplayName = fullDisplayName;
    }

    public Long getBonusPoints() {
        return this.bonusPoints;
    }

    public void setBonusPoints(Long bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public Customer id(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Customer)) {
            return false;
        }
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) && Objects.equals(firstName, customer.firstName)
                && Objects.equals(lastName, customer.lastName)
                && Objects.equals(fullDisplayName, customer.fullDisplayName)
                && Objects.equals(bonusPoints, customer.bonusPoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, fullDisplayName, bonusPoints);
    }

    @Override
    public String toString() {
        return "{" + " id='" + getId() + "'" + ", firstName='" + getFirstName() + "'" + ", lastName='" + getLastName()
                + "'" + ", fullDisplayName='" + getFullDisplayName() + "'" + ", bonusPoints='" + getBonusPoints() + "'"
                + "}";
    }
}
