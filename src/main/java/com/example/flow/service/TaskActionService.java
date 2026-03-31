package com.example.flow.service;

import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskActionService {

    private final SurecAdimRepository surecAdimRepository;
    private final SurecRepository surecRepository;
    private final AdimGecisKuralRepository gecisRepository;
    private final FormVeriRepository formVeriRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final FieldPermissionService fieldPermissionService;

    private final FormRepository formRepository;
    private final FormBileseniRepository formBilesenRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final KullaniciRolRepository kullaniciRolRepository;

    @Transactional
    public void handleAction(Long taskId, Long aksiyonId, Map<Long, String> formData) {

        // 1️⃣ MEVCUT TASK'I BUL VE KULLANICIYI AL
        SurecAdim currentTask = surecAdimRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task bulunamadı: " + taskId));

        Long userId = currentTask.getAtananKullaniciId();

        // 2️⃣ FORM VERİLERİNİ DOĞRULA VE KAYDET
        for (Map.Entry<Long, String> entry : formData.entrySet()) {
            fieldPermissionService.validate(userId, entry.getKey());

            FormVeri fv = new FormVeri();
            fv.setSurecId(currentTask.getSurecId());
            fv.setBilesenId(entry.getKey());
            fv.setDeger(entry.getValue());
            fv.setKaydedenKullaniciId(userId);
            fv.setKayitTarihi(LocalDateTime.now());

            formVeriRepository.save(fv);
        }

        // 3️⃣ MEVCUT TASK'I TAMAMLA
        currentTask.setDurum("TAMAMLANDI");
        currentTask.setTamamlandiMi(true);
        currentTask.setBitisTarihi(LocalDateTime.now()); // Opsiyonel: Bitiş tarihi eklemek iyidir
        surecAdimRepository.save(currentTask);

        // 4️⃣ PARALEL ONAY KONTROLÜ
        // Bu adımda birden fazla kişiye task atanmış olabilir. Hepsinin bitmesi beklenir.
        long tamamlananSayisi = surecAdimRepository.countBySurecIdAndAdimIdAndDurum(
                currentTask.getSurecId(),
                currentTask.getAdimId(),
                "TAMAMLANDI"
        );

        long toplamTaskSayisi = surecAdimRepository.findBySurecIdAndAdimId(
                currentTask.getSurecId(),
                currentTask.getAdimId()
        ).size();

        // Eğer henüz herkes tamamlamadıysa metodu burada bitir, sonraki adıma geçme
        if (tamamlananSayisi < toplamTaskSayisi) {
            return;
        }

        // 5️⃣ AKIŞ ADIMI VE SÜREÇ BİLGİLERİNİ GETİR
        AkisAdim step = akisAdimRepository.findById(currentTask.getAdimId())
                .orElseThrow(() -> new RuntimeException("Akış adımı bulunamadı"));

        AkisSurec surec = surecRepository.findById(currentTask.getSurecId())
                .orElseThrow(() -> new RuntimeException("Süreç bulunamadı"));

        // 6️⃣ SONRAKI ADIMI BELİRLE (KURAL VEYA VARSAYILAN MANTIK)
        Long nextStepId = null;

        // DB'deki Özel Geçiş Kuralları (Örn: Redde basınca 5. adıma git gibi)
        List<AdimGecisKural> rules = gecisRepository.findByAdimIdAndAksiyonId(
                currentTask.getAdimId(),
                aksiyonId
        );

        if (!rules.isEmpty()) {
            nextStepId = rules.get(0).getSonrakiAdimId();
        }

        // Eğer kural yoksa varsayılan logic (1=Onay, 2=Red)
        if (nextStepId == null) {
            if (aksiyonId == 1) { // ONAY
                Optional<AkisAdim> nextStepOpt = akisAdimRepository
                        .findFirstByAkis_AkisIdAndAdimSirasiGreaterThanOrderByAdimSirasiAsc(
                                surec.getAkisId(),
                                step.getAdimSirasi()
                        );

                if (nextStepOpt.isEmpty()) {
                    surec.setDurum("TAMAMLANDI");
                    surecRepository.save(surec);
                    return;
                }
                nextStepId = nextStepOpt.get().getAdimId();
            } else if (aksiyonId == 2) { // RED
                surec.setDurum("REDDEDILDI");
                surecRepository.save(surec);
                return;
            }
        }

        // 7️⃣ SÜRECİ GÜNCELLE VE YENİ TASKLARI OLUŞTUR
        if (nextStepId != null) {
            surec.setMevcutAdimId(nextStepId);
            surecRepository.save(surec);
            createTasksForStep(surec.getSurecId(), nextStepId);
        }
    }

    private void createTasksForStep(Long surecId, Long adimId) {
        Form form = formRepository.findByAdimId(adimId)
                .orElseThrow(() -> new RuntimeException("Bu adım için form tanımlanmamış: " + adimId));

        List<FormBileseni> bilesenler = formBilesenRepository.findByForm_FormId(form.getFormId());
        Set<Long> atanacakKullanicilar = new HashSet<>();

        for (FormBileseni b : bilesenler) {
            List<FormBileseniAtama> atamalar = atamaRepository.findByBilesenId(b.getBilesenId());

            for (FormBileseniAtama a : atamalar) {
                if ("USER".equals(a.getTip())) {
                    atanacakKullanicilar.add(a.getRefId());
                } else if ("ROLE".equals(a.getTip())) {
                    List<KullaniciRol> roller = kullaniciRolRepository.findByRolId(a.getRefId());
                    for (KullaniciRol kr : roller) {
                        atanacakKullanicilar.add(kr.getKullaniciId());
                    }
                }
            }
        }

        // Eğer hiç kullanıcı bulunamadıysa (Sistem hatası veya eksik tanım)
        if (atanacakKullanicilar.isEmpty()) {
            throw new RuntimeException("Yeni adım için atanacak kullanıcı bulunamadı!");
        }

        for (Long kId : atanacakKullanicilar) {
            SurecAdim newTask = new SurecAdim();
            newTask.setSurecId(surecId);
            newTask.setAdimId(adimId);
            newTask.setAtananKullaniciId(kId);
            newTask.setDurum("BEKLIYOR");
            newTask.setTamamlandiMi(false);
            newTask.setBaslamaTarihi(LocalDateTime.now());
            surecAdimRepository.save(newTask);
        }
    }
}