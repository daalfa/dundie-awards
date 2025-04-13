package com.ninjaone.dundie_awards.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private Integer dundieAwards;

    @ManyToOne
    private Organization organization;

    public Employee(String firstName, String lastName, Organization organization) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
    }
}