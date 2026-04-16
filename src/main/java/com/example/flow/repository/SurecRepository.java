package com.example.flow.repository;

import com.example.flow.entity.AkisSurec;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SurecRepository extends JpaRepository<AkisSurec, Long> {
    List<AkisSurec> findByBaslamaTarihiBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}