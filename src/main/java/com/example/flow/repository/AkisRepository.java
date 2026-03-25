package com.example.flow.repository;

import com.example.flow.dto.FlowFieldFlatResponse;
import com.example.flow.dto.FlowListResponse;
import com.example.flow.entity.Akis;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AkisRepository extends JpaRepository<Akis, Long> {

    @Query(value = """
    SELECT 
        a.akis_id as akisId,
        a.akis_adi as akisAdi,
        a.aciklama as aciklama,  -- 🔥

        ad.adim_id as adimId,
        ad.adim_adi as adimAdi,

        b.bilesen_id as bilesenId,
        b.bilesen_tipi as bilesenTipi,
        b.label as label,
        b.placeholder as placeholder,
        b.zorunlu as zorunlu,
        b.sira_no as siraNo,

        s.etiket as optionLabel,
        s.deger as optionValue

    FROM akislar a
    JOIN akis_adimlari ad ON ad.akis_id = a.akis_id
    JOIN formlar f ON f.adim_id = ad.adim_id
    JOIN form_bilesenleri b ON b.form_id = f.form_id
    LEFT JOIN bilesen_secenekleri s ON s.bilesen_id = b.bilesen_id

    WHERE a.akis_id = :flowId
    ORDER BY ad.adim_sirasi, b.sira_no
""", nativeQuery = true)
    List<FlowFieldFlatResponse> getFlowFields(@Param("flowId") Long flowId);
    @Query(value = """
    SELECT 
        akis_id as akisId,
        akis_adi as akisAdi,
        aciklama as aciklama
    FROM akislar
    ORDER BY akis_id DESC
""", nativeQuery = true)
    List<FlowListResponse> getAllFlows();
}