package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "surec_eventleri")
@Getter
@Setter
public class SurecEventi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "surec_id", nullable = false)
    private Long surecId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "correlation_id", nullable = false, unique = true)
    private String correlationId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}