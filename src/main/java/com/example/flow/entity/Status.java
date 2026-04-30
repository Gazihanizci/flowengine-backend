package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "statuses")
@Getter
@Setter
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO, IN_PROGRESS, REVIEW, DONE, REJECTED
    @Column(nullable = false, unique = true)
    private String name;

    private String color;

    @Column(name = "order_no")
    private Integer orderNo;
}