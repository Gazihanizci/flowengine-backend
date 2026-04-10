package com.example.flow.repository;

import com.example.flow.dto.KullaniciMeResponse;
import com.example.flow.entity.Kullanici;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    @Query(value = """
    SELECT 
        k.kullanici_id AS kullaniciId,
        k.ad_soyad AS adSoyad,
        r.rol_adi AS rolAdi,
        r.rol_id AS rolId
    FROM kullanicilar k
    JOIN kullanici_rolleri kr ON k.kullanici_id = kr.kullanici_id
    JOIN roller r ON kr.rol_id = r.rol_id
    WHERE k.kullanici_id = :kullaniciId
    """, nativeQuery = true)
    List<KullaniciMeResponse> findMyRoles(@Param("kullaniciId") Long kullaniciId);

    Optional<Kullanici> findByEmail(String email);

    @Query("""
    SELECT kr.kullaniciId
    FROM KullaniciRol kr
    WHERE kr.rolId = :rolId
    """)
    List<Long> findKullaniciIdsByRolId(@Param("rolId") Long rolId);
}