package com.example.flow.repository;

import com.example.flow.entity.Bildirim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BildirimRepository extends JpaRepository<Bildirim, Long> {

    List<Bildirim> findByKullaniciIdOrderByOlusturmaTarihiDesc(Long kullaniciId);

    long countByKullaniciIdAndOkunduFalse(Long kullaniciId);
}