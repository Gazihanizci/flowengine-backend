package com.example.flow.service;

import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final SurecAdimRepository surecAdimRepository;
    private final FormRepository formRepository;
    private final FormBileseniRepository formBilesenRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final KullaniciRolRepository kullaniciRolRepository;

    @Transactional
    public void createTasksForStep(Long surecId, Long adimId) {
        Form form = formRepository.findByAdimId(adimId)
                .orElseThrow(() -> new RuntimeException("Form bulunamadı"));

        List<FormBileseni> bilesenler = formBilesenRepository.findByForm_FormId(form.getFormId());
        Set<Long> users = new HashSet<>();

        for (FormBileseni b : bilesenler) {
            List<FormBileseniAtama> atamalar = atamaRepository.findByBilesenId(b.getBilesenId());
            for (FormBileseniAtama a : atamalar) {
                if ("USER".equals(a.getTip())) {
                    users.add(a.getRefId());
                } else if ("ROLE".equals(a.getTip())) {
                    List<KullaniciRol> roller = kullaniciRolRepository.findByRolId(a.getRefId());
                    for (KullaniciRol kr : roller) {
                        users.add(kr.getKullaniciId());
                    }
                }
            }
        }

        if (users.isEmpty()) {
            throw new RuntimeException("Atanacak kullanıcı bulunamadı!");
        }

        for (Long uid : users) {
            SurecAdim task = new SurecAdim();
            task.setSurecId(surecId);
            task.setAdimId(adimId);
            task.setAtananKullaniciId(uid);
            task.setDurum("BEKLIYOR");
            task.setTamamlandiMi(false);
            task.setBaslamaTarihi(LocalDateTime.now());
            surecAdimRepository.save(task);
        }
    }

    public boolean isStepFullyCompleted(Long surecId, Long adimId) {
        long tamamlanan = surecAdimRepository.countBySurecIdAndAdimIdAndDurum(surecId, adimId, "TAMAMLANDI");
        long toplam = surecAdimRepository.findBySurecIdAndAdimId(surecId, adimId).size();
        return tamamlanan >= toplam;
    }
}