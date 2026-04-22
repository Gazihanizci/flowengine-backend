package com.example.flow.service;

import com.example.flow.dto.FieldResponse;
import com.example.flow.dto.OptionResponse;
import com.example.flow.dto.TaskResponse;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final SurecAdimRepository surecAdimRepository;
    private final FormRepository formRepository;
    private final FormBileseniRepository formBilesenRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final KullaniciRolRepository kullaniciRolRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final FormVeriRepository formVeriRepository;
    private final BilesenSecenegiRepository secenekRepository;
    private final FieldPermissionService fieldPermissionService;

    // 🔥 TASK OLUŞTURMA (AYNI)
    @Transactional
    public void createTasksForStep(Long surecId, Long adimId) {

        Form form = formRepository.findByAdimId(adimId)
                .orElseThrow(() -> new RuntimeException("Adım için form bulunamadı"));

        List<FormBileseni> bilesenler =
                formBilesenRepository.findByForm_FormId(form.getFormId());

        Set<Long> users = new HashSet<>();

        for (FormBileseni b : bilesenler) {

            List<FormBileseniAtama> editAtamalar =
                    atamaRepository.findByBilesenIdAndYetkiTipi(b.getBilesenId(), "EDIT");

            for (FormBileseniAtama a : editAtamalar) {

                if ("USER".equalsIgnoreCase(a.getTip())) {
                    users.add(a.getRefId());
                } else if ("ROLE".equalsIgnoreCase(a.getTip())) {
                    List<KullaniciRol> roller =
                            kullaniciRolRepository.findByRolId(a.getRefId());

                    for (KullaniciRol kr : roller) {
                        users.add(kr.getKullaniciId());
                    }
                }
            }
        }

        for (Long uid : users) {

            boolean varMi = surecAdimRepository
                    .findBySurecIdAndAdimId(surecId, adimId)
                    .stream()
                    .anyMatch(t ->
                            uid.equals(t.getAtananKullaniciId()) &&
                                    !Boolean.TRUE.equals(t.getTamamlandiMi())
                    );

            if (varMi) continue;

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

    // 🔥 TASK LİSTELEME (EN KRİTİK)
    public List<TaskResponse> getMyTasks(Long userId) {

        List<SurecAdim> tasks =
                surecAdimRepository.findByAtananKullaniciIdAndDurum(userId, "BEKLIYOR");

        List<TaskResponse> responseList = new ArrayList<>();

        for (SurecAdim task : tasks) {

            TaskResponse res = new TaskResponse();

            res.setTaskId(task.getId());
            res.setSurecId(task.getSurecId());
            res.setAdimId(task.getAdimId());

            AkisAdim adim = akisAdimRepository.findById(task.getAdimId()).orElse(null);
            if (adim != null) {
                res.setAdimAdi(adim.getAdimAdi());
            }

            // 🔥 FORM GETİR
            Optional<Form> formOpt = formRepository.findByAdimId(task.getAdimId());

            if (formOpt.isPresent()) {

                List<FormBileseni> bilesenler =
                        formBilesenRepository.findByForm_FormId(formOpt.get().getFormId());

                // 🔥 SADECE KENDİ VERİSİ
                List<FormVeri> veriler =
                        formVeriRepository.findBySurecIdAndKaydedenKullaniciId(
                                task.getSurecId(),
                                userId
                        );

                List<FieldResponse> fields = new ArrayList<>();

                for (FormBileseni b : bilesenler) {

                    if (!fieldPermissionService.canView(userId, b.getBilesenId())) {
                        continue;
                    }

                    FieldResponse fr = new FieldResponse();
                    fr.setFieldId(b.getBilesenId());
                    fr.setType(b.getBilesenTipi());
                    fr.setLabel(b.getLabel());

                    boolean editable =
                            fieldPermissionService.canEdit(userId, b.getBilesenId());

                    fr.setEditable(editable);

                    // 🔥 VALUE (SADECE KENDİSİ)
                    String value = veriler.stream()
                            .filter(v -> v.getBilesenId().equals(b.getBilesenId()))
                            .map(FormVeri::getDeger)
                            .findFirst()
                            .orElse(null);

                    fr.setValue(value);

                    // 🔥 OPTIONS
                    List<OptionResponse> options =
                            secenekRepository.findByBilesen_BilesenId(b.getBilesenId())
                                    .stream()
                                    .map(o -> {
                                        OptionResponse op = new OptionResponse();
                                        op.setLabel(o.getEtiket());
                                        op.setValue(o.getDeger());
                                        return op;
                                    })
                                    .collect(Collectors.toList());

                    fr.setOptions(options);

                    fields.add(fr);
                }

                res.setForm(fields);
            }

            responseList.add(res);
        }

        return responseList;
    }

    // 🔥 ÇOKLU ONAY
    public boolean isStepFullyCompleted(Long surecId, Long adimId) {

        List<SurecAdim> tasks =
                surecAdimRepository.findBySurecIdAndAdimId(surecId, adimId);

        if (tasks.isEmpty()) return true;

        AkisAdim step = akisAdimRepository.findById(adimId).orElseThrow();

        int gerekli = step.getGerekliOnaySayisi() != null
                ? step.getGerekliOnaySayisi()
                : 1;

        long tamamlanan = tasks.stream()
                .filter(t -> "TAMAMLANDI".equalsIgnoreCase(t.getDurum()))
                .count();

        return tamamlanan >= gerekli;
    }
}