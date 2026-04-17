package com.example.flow.service;

import com.example.flow.dto.*;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FlowDetailService {
    private final KullaniciRepository kullaniciRepository;
    private final AkisRepository akisRepository;
    private final SurecRepository surecRepository;
    private final SurecAdimRepository surecAdimRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final FormRepository formRepository;
    private final FormBileseniRepository formBilesenRepository;
    private final FormVeriRepository formVeriRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final KullaniciRolRepository kullaniciRolRepository;
    private final BilesenSecenegiRepository bilesenSecenegiRepository;

    public List<TaskResponse> getFlowDetail(Long surecId, Long userId) {

        // 🔥 TÜM STEP’LER
        List<AkisAdim> steps =
                akisAdimRepository.findByAkis_AkisIdOrderByAdimSirasiAsc(
                        surecRepository.findById(surecId).orElseThrow().getAkisId()
                );

        // 🔥 TÜM TASK’LER (geçmiş + aktif)
        List<SurecAdim> tasks = surecAdimRepository.findBySurecId(surecId);

        Map<Long, SurecAdim> taskMap = new HashMap<>();
        for (SurecAdim t : tasks) {
            taskMap.put(t.getAdimId(), t);
        }

        List<TaskResponse> responseList = new ArrayList<>();

        for (AkisAdim step : steps) {

            TaskResponse tr = new TaskResponse();
            tr.setAdimId(step.getAdimId());
            tr.setAdimAdi(step.getAdimAdi());
            tr.setSurecId(surecId);

            SurecAdim relatedTask = taskMap.get(step.getAdimId());

            boolean isActive =
                    relatedTask != null &&
                            "BEKLIYOR".equals(relatedTask.getDurum()) &&
                            userId.equals(relatedTask.getAtananKullaniciId());

            Form form = formRepository.findByAdimId(step.getAdimId()).orElse(null);

            List<FieldResponse> fields = new ArrayList<>();

            if (form != null) {

                List<FormBileseni> bilesenler =
                        formBilesenRepository.findByForm_FormId(form.getFormId());

                List<FormVeri> veriler =
                        formVeriRepository.findBySurecId(surecId);

                Map<Long, String> veriMap = new HashMap<>();
                for (FormVeri v : veriler) {
                    veriMap.put(v.getBilesenId(), v.getDeger());
                }

                for (FormBileseni b : bilesenler) {

                    boolean canView = hasPermission(userId, b.getBilesenId(), "VIEW");
                    if (!canView) continue;

                    boolean canEdit = isActive &&
                            hasPermission(userId, b.getBilesenId(), "EDIT");

                    FieldResponse fr = new FieldResponse();
                    fr.setFieldId(b.getBilesenId());
                    fr.setLabel(b.getLabel());
                    fr.setType(b.getBilesenTipi());
                    fr.setValue(veriMap.get(b.getBilesenId()));
                    fr.setEditable(canEdit);

                    fields.add(fr);
                }
            }

            tr.setForm(fields);
            responseList.add(tr);
        }

        return responseList;
    }

    private boolean hasPermission(Long userId, Long bilesenId, String yetkiTipi) {

        List<FormBileseniAtama> atamalar =
                atamaRepository.findByBilesenId(bilesenId);

        if (atamalar.isEmpty()) return true;

        for (FormBileseniAtama a : atamalar) {

            boolean yetkiUygun =
                    yetkiTipi.equalsIgnoreCase(a.getYetkiTipi()) ||
                            ("VIEW".equalsIgnoreCase(yetkiTipi)
                                    && "EDIT".equalsIgnoreCase(a.getYetkiTipi()));

            if (!yetkiUygun) continue;

            if ("USER".equalsIgnoreCase(a.getTip())
                    && a.getRefId().equals(userId)) {
                return true;
            }

            if ("ROLE".equalsIgnoreCase(a.getTip())) {

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