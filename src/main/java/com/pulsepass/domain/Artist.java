package com.pulsepass.domain;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "artists")
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="stage_name", nullable=false, unique=true, length=120)
    private String stageName;

    @Column(nullable=false, length=80)
    private String country;

    @Column(nullable=false, length=80)
    private String genre;

    @Column(nullable=false)
    private boolean active;

    @ManyToMany(mappedBy="artists")
    private Set<Event> events = new LinkedHashSet<>();

    public Artist() {}

    public Long getId() {
        return id;
    }
    public void setId(Long v){
        id=v;
    }
    public String getStageName(){
        return stageName;
    }
    public void setStageName(String v){
        stageName=v;
    }
    public String getCountry(){
        return country;
    }
    public void setCountry(String v){
        country=v;
    }
    public String getGenre(){
        return genre;
    }
    public void setGenre(String v){
        genre=v;
    }
    public boolean isActive(){
        return active;
    }
    public void setActive(boolean v){
        active=v;
    }
    public Set<Event> getEvents(){
        return events;
    }
    public void setEvents(Set<Event> v){
        events=v;
    }
}
