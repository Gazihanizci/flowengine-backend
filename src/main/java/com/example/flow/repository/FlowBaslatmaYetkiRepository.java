package com.example.flow.repository;

import com.example.flow.entity.FlowBaslatmaYetki;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlowBaslatmaYetkiRepository
        extends JpaRepository<FlowBaslatmaYetki, Long> {

    // 🔥 Mevcut
    List<FlowBaslatmaYetki> findByAkisId(Long akisId);

    boolean existsByAkisIdAndTipAndRefId(Long akisId, String tip, Long refId);

    // ============================
    // 🔥 EKLENMESİ GEREKENLER
    // ============================

    // 🔥 Flow’a ait TÜM yetkileri silmek için (update sırasında)
    void deleteByAkisId(Long akisId);

    // 🔥 Belirli tipteki yetkileri çekmek (USER / ROLE ayrımı için)
    List<FlowBaslatmaYetki> findByAkisIdAndTip(Long akisId, String tip);

    // 🔥 Tekil kayıt bulmak (güncelleme için)
    Optional<FlowBaslatmaYetki> findByAkisIdAndTipAndRefId(
            Long akisId,
            String tip,
            Long refId
    );

    // 🔥 Belirli bir yetkiyi silmek (granular edit için)
    void deleteByAkisIdAndTipAndRefId(
            Long akisId,
            String tip,
            Long refId
    );
}