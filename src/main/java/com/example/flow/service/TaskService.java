package com.example.flow.service;

import com.example.flow.entity.Form;
import com.example.flow.entity.FormBileseni;
import com.example.flow.entity.FormBileseniAtama;
import com.example.flow.entity.KullaniciRol;
import com.example.flow.entity.SurecAdim;
import com.example.flow.repository.FormBileseniAtamaRepository;
import com.example.flow.repository.FormBileseniRepository;
import com.example.flow.repository.FormRepository;
import com.example.flow.repository.KullaniciRolRepository;
import com.example.flow.repository.SurecAdimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
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
                .orElseThrow(() -> new RuntimeException("Adım için form bulunamadı. adimId=" + adimId));

        List<FormBileseni> bilesenler =
                formBilesenRepository.findByForm_FormId(form.getFormId());

        Set<Long> users = new HashSet<>();

        for (FormBileseni bilesen : bilesenler) {

            // SADECE EDIT yetkisi olanları task sahibi yap
            List<FormBileseniAtama> editAtamalar =
                    atamaRepository.findByBilesenIdAndYetkiTipi(bilesen.getBilesenId(), "EDIT");

            for (FormBileseniAtama atama : editAtamalar) {

                if ("USER".equalsIgnoreCase(atama.getTip())) {
                    if (atama.getRefId() != null) {
                        users.add(atama.getRefId());
                    }
                } else if ("ROLE".equalsIgnoreCase(atama.getTip())) {
                    List<KullaniciRol> roller =
                            kullaniciRolRepository.findByRolId(atama.getRefId());

                    for (KullaniciRol kr : roller) {
                        if (kr.getKullaniciId() != null) {
                            users.add(kr.getKullaniciId());
                        }
                    }
                }
            }
        }

        if (users.isEmpty()) {
            log.error("Süreç ID {} - Adım ID {} için EDIT yetkisine sahip atanacak kullanıcı bulunamadı!",
                    surecId, adimId);
            throw new RuntimeException("Bu adım için EDIT yetkisine sahip atanacak kullanıcı bulunamadı!");
        }

        // Aynı kullanıcıya aynı step için açık task varsa tekrar oluşturma
        for (Long uid : users) {
            boolean zatenVar = surecAdimRepository
                    .findBySurecIdAndAdimId(surecId, adimId)
                    .stream()
                    .anyMatch(t ->
                            uid.equals(t.getAtananKullaniciId()) &&
                                    !Boolean.TRUE.equals(t.getTamamlandiMi()) &&
                                    !"REDDEDILDI".equalsIgnoreCase(t.getDurum())
                    );

            if (zatenVar) {
                log.warn("Süreç ID {} - Adım ID {} - Kullanıcı ID {} için zaten açık task var, tekrar oluşturulmadı.",
                        surecId, adimId, uid);
                continue;
            }

            SurecAdim task = new SurecAdim();
            task.setSurecId(surecId);
            task.setAdimId(adimId);
            task.setAtananKullaniciId(uid);
            task.setDurum("BEKLIYOR");
            task.setTamamlandiMi(false);
            task.setBaslamaTarihi(LocalDateTime.now());

            surecAdimRepository.save(task);
        }

        log.info("Adım ID {} için EDIT yetkisine göre görevler başarıyla oluşturuldu. Kullanıcı sayısı={}",
                adimId, users.size());
    }

    public boolean isStepFullyCompleted(Long surecId, Long adimId) {

        List<SurecAdim> tasks = surecAdimRepository.findBySurecIdAndAdimId(surecId, adimId);

        if (tasks.isEmpty()) {
            return true;
        }

        return tasks.stream().anyMatch(t -> "TAMAMLANDI".equalsIgnoreCase(t.getDurum()));    }
}