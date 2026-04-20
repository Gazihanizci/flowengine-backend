package com.example.flow.repository;

import com.example.flow.entity.FormBileseniAtama;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormBileseniAtamaRepository extends JpaRepository<FormBileseniAtama, Long> {

    List<FormBileseniAtama> findByBilesenId(Long bilesenId);
    List<FormBileseniAtama> findByBilesenIdAndYetkiTipi(Long bilesenId, String yetkiTipi);

}