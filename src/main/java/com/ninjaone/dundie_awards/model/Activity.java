package com.ninjaone.dundie_awards.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime occuredAt;

    private String event;

    public Activity(LocalDateTime localDateTime, String event) {
        this.occuredAt = localDateTime;
        this.event = event;
    }
}
