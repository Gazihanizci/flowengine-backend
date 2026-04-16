package com.example.flow.repository;

import com.example.flow.entity.KullaniciRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KullaniciRolRepository extends JpaRepository<KullaniciRol, Long> {

    // 🔥 ROL OLSA DA OLMASA DA TÜM KULLANICILAR
    @Query(value = """
        SELECT 
            k.kullanici_id,
            k.ad_soyad,
            k.email,
            r.rol_id,
            r.rol_adi
        FROM kullanicilar k
        LEFT JOIN kullanici_rolleri kr ON k.kullanici_id = kr.kullanici_id
        LEFT JOIN roller r ON r.rol_id = kr.rol_id
    """, nativeQuery = true)
    List<Object[]> findAllUsersWithRoles();

    List<KullaniciRol> findByRolId(Long rolId);

    List<KullaniciRol> findByKullaniciId(Long kullaniciId);

    Optional<KullaniciRol> findByKullaniciIdAndRolId(Long kullaniciId, Long rolId);

    void deleteByKullaniciIdAndRolId(Long kullaniciId, Long rolId);
}