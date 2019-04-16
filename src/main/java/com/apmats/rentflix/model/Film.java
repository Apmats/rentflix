package com.apmats.rentflix.model;

import com.apmats.rentflix.util.RecencyType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;
import java.util.Objects;

import javax.persistence.*;

@Entity
@Table(name = "film")
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(columnDefinition = "text")
    private String title;

    @Enumerated(EnumType.STRING)
    private RecencyType recencyType;

    @OneToMany(mappedBy = "film")
    private List<PhysicalMedia> physicalMedia;

    @Version
    @Column(columnDefinition = "integer DEFAULT 0", nullable = false)
    private Long version = 0L;

    public Film() {
    }

    public Film(Long id, String title, RecencyType recencyType) {
        this.id = id;
        this.title = title;
        this.recencyType = recencyType;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RecencyType getRecencyType() {
        return this.recencyType;
    }

    public void setRecencyType(RecencyType recencyType) {
        this.recencyType = recencyType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Film)) {
            return false;
        }
        Film film = (Film) o;
        return Objects.equals(id, film.id) && Objects.equals(title, film.title)
                && Objects.equals(recencyType, film.recencyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, recencyType);
    }

    @Override
    public String toString() {
        return "{" + " id='" + getId() + "'" + ", title='" + getTitle() + "'" + ", recency='" + getRecencyType() + "'"
                + "}";
    }

}
