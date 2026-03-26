package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kullanicilar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kullaniciId;

    private String adSoyad;

    @Column(unique = true)
    private String email;

    @Column(name = "parola_hash")
    private String parolaHash;
}