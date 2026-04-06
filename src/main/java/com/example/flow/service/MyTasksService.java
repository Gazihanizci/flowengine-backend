package com.example.flow.service;

import com.example.flow.dto.*;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MyTasksService {

    private final FormVeriRepository formVeriRepository;
    private final SurecAdimRepository surecAdimRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final FormRepository formRepository;
    private final FormBileseniRepository formBilesenRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final KullaniciRolRepository kullaniciRolRepository;
    private final BilesenSecenegiRepository bilesenSecenegiRepository;

    public List<TaskResponse> getMyTasks(Long userId) {

        List<SurecAdim> tasks =
                surecAdimRepository
                        .findByAtananKullaniciIdAndDurum(userId, "BEKLIYOR");

        List<TaskResponse> responseList = new ArrayList<>();

        for (SurecAdim task : tasks) {

            TaskResponse tr = new TaskResponse();
            tr.setTaskId(task.getId());
            tr.setSurecId(task.getSurecId());
            tr.setAdimId(task.getAdimId());

            // STEP
            AkisAdim step = akisAdimRepository
                    .findById(task.getAdimId())
                    .orElseThrow();

            tr.setAdimAdi(step.getAdimAdi());

            // 🔥 FORM
            Form form = formRepository
                    .findByAdimId(task.getAdimId())
                    .orElse(null);

            List<FieldResponse> fields = new ArrayList<>();

            if (form != null) {

                // 🔥 TÜM BİLEŞENLER
                List<FormBileseni> bilesenler =
                        formBilesenRepository
                                .findByForm_FormId(form.getFormId());

                // 🔥 VERİLER (Sadece bu süreç)
                List<FormVeri> veriler =
                        formVeriRepository.findBySurecId(task.getSurecId());

                // 🔥 PERFORMANCE: MAP
                Map<Long, String> veriMap = new HashMap<>();
                for (FormVeri v : veriler) {
                    veriMap.put(v.getBilesenId(), v.getDeger());
                }

                // 🔥 OPTIONS TEK SEFERDE ÇEK
                List<BilesenSecenegi> tumSecenekler =
                        bilesenSecenegiRepository.findAll();

                Map<Long, List<OptionResponse>> optionMap = new HashMap<>();

                for (BilesenSecenegi s : tumSecenekler) {

                    Long bilesenId = s.getBilesen().getBilesenId();

                    optionMap.putIfAbsent(bilesenId, new ArrayList<>());

                    OptionResponse op = new OptionResponse();
                    op.setLabel(s.getEtiket());
                    op.setValue(s.getDeger());

                    optionMap.get(bilesenId).add(op);
                }

                // 🔥 FIELD LOOP
                for (FormBileseni b : bilesenler) {

                    FieldResponse fr = new FieldResponse();

                    fr.setFieldId(b.getBilesenId());
                    fr.setType(b.getBilesenTipi());
                    fr.setLabel(b.getLabel());

                    // 🔥 VALUE (DB’den gelen)
                    fr.setValue(veriMap.get(b.getBilesenId()));

                    // 🔥 YETKİ
                    fr.setEditable(checkPermission(userId, b.getBilesenId()));

                    // 🔥 OPTIONS
                    fr.setOptions(
                            optionMap.getOrDefault(
                                    b.getBilesenId(),
                                    new ArrayList<>()
                            )
                    );

                    fields.add(fr);
                }
            }

            tr.setForm(fields);
            responseList.add(tr);
        }

        return responseList;
    }

    // 🔥 YETKİ KONTROL
    private boolean checkPermission(Long userId, Long bilesenId) {

        List<FormBileseniAtama> atamalar =
                atamaRepository.findByBilesenId(bilesenId);

        if (atamalar.isEmpty()) return true;

        for (FormBileseniAtama a : atamalar) {

            if ("USER".equals(a.getTip())
                    && a.getRefId().equals(userId)) {
                return true;
            }

            if ("ROLE".equals(a.getTip())) {

                List<KullaniciRol> roller =
                        kullaniciRolRepository.findByRolId(a.getRefId());

                for (KullaniciRol kr : roller) {
                    if (kr.getKullaniciId().equals(userId)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}