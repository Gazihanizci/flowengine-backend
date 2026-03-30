package com.example.flow.repository;

import com.example.flow.dto.KullaniciRolResponse;
import com.example.flow.entity.KullaniciRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface KullaniciRolRepository extends JpaRepository<KullaniciRol, Long> {

    @Query(value = """
        SELECT 
            k.kullanici_id,
            k.ad_soyad,
            k.email,
            r.rol_id,
            r.rol_adi
        FROM kullanici_rolleri kr
        JOIN kullanicilar k ON k.kullanici_id = kr.kullanici_id
        JOIN roller r ON r.rol_id = kr.rol_id
    """, nativeQuery = true)
    List<Object[]> findAllUsersWithRoles();
    List<KullaniciRol> findByRolId(Long rolId);

}