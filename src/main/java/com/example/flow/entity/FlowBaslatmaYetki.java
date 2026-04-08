package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "flow_baslatma_yetkileri")
@Getter
@Setter
public class FlowBaslatmaYetki {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long akisId;

    private String tip; // USER / ROLE

    private Long refId;
}