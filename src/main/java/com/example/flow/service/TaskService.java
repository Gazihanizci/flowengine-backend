    package com.example.flow.service;

    import com.example.flow.entity.*;
    import com.example.flow.repository.*;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Optional;
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
            // 1. Formu opsiyonel yapalım. Form yoksa hata fırlatmak yerine log basalım.
            Optional<Form> formOpt = formRepository.findByAdimId(adimId);
            if (formOpt.isEmpty()) {
                log.warn("Adım ID {} için form bulunamadı, görev oluşturulamıyor!", adimId);
                return;
            }

            Form form = formOpt.get();
            List<FormBileseni> bilesenler = formBilesenRepository.findByForm_FormId(form.getFormId());
            Set<Long> users = new HashSet<>();

            // 2. Kullanıcıları topla
            for (FormBileseni b : bilesenler) {
                List<FormBileseniAtama> atamalar = atamaRepository.findByBilesenId(b.getBilesenId());
                for (FormBileseniAtama a : atamalar) {
                    if ("USER".equalsIgnoreCase(a.getTip())) {
                        users.add(a.getRefId());
                    } else if ("ROLE".equalsIgnoreCase(a.getTip())) {
                        List<KullaniciRol> roller = kullaniciRolRepository.findByRolId(a.getRefId());
                        for (KullaniciRol kr : roller) {
                            users.add(kr.getKullaniciId());
                        }
                    }
                }
            }

            // 3. Kullanıcı yoksa patlamak yerine uyarı verelim
            if (users.isEmpty()) {
                log.error("Süreç ID {} - Adım ID {} için atanacak kullanıcı bulunamadı!", surecId, adimId);
                return;
            }

            // 4. Taskları oluştur
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
            log.info("Adım ID {} için {} adet görev başarıyla oluşturuldu.", adimId, users.size());
        }

        public boolean isStepFullyCompleted(Long surecId, Long adimId) {
            // Bu adım için oluşturulmuş tüm görevleri getir
            List<SurecAdim> tasks = surecAdimRepository.findBySurecIdAndAdimId(surecId, adimId);

            if (tasks.isEmpty()) return true; // Hiç task yoksa tamamlanmış sayalım (veya hata yönetimine göre değişir)

            // Hepsinin durumunu kontrol et
            return tasks.stream().allMatch(t -> "TAMAMLANDI".equals(t.getDurum()));
        }
    }