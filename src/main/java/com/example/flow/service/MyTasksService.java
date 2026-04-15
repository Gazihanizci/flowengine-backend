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
                surecAdimRepository.findByAtananKullaniciIdAndDurum(userId, "BEKLIYOR");

        List<TaskResponse> responseList = new ArrayList<>();

        for (SurecAdim task : tasks) {

            TaskResponse tr = new TaskResponse();
            tr.setTaskId(task.getId());
            tr.setSurecId(task.getSurecId());
            tr.setAdimId(task.getAdimId());

            AkisAdim step = akisAdimRepository
                    .findById(task.getAdimId())
                    .orElseThrow();

            tr.setAdimAdi(step.getAdimAdi());

            Form form = formRepository
                    .findByAdimId(task.getAdimId())
                    .orElse(null);

            List<FieldResponse> fields = new ArrayList<>();

            if (form != null) {

                List<FormBileseni> bilesenler =
                        formBilesenRepository.findByForm_FormId(form.getFormId());

                List<FormVeri> veriler =
                        formVeriRepository.findBySurecId(task.getSurecId());

                Map<Long, String> veriMap = new HashMap<>();
                for (FormVeri v : veriler) {
                    veriMap.put(v.getBilesenId(), v.getDeger());
                }

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

                for (FormBileseni b : bilesenler) {

                    boolean canView = hasPermission(userId, b.getBilesenId(), "VIEW");
                    if (!canView) {
                        continue;
                    }

                    boolean canEdit = hasPermission(userId, b.getBilesenId(), "EDIT");

                    FieldResponse fr = new FieldResponse();
                    fr.setFieldId(b.getBilesenId());
                    fr.setType(b.getBilesenTipi());
                    fr.setLabel(b.getLabel());
                    fr.setValue(veriMap.get(b.getBilesenId()));
                    fr.setEditable(canEdit);
                    fr.setOptions(
                            optionMap.getOrDefault(
                                    b.getBilesenId(),
                                    new ArrayList<>()
                            )
                    );

                    fields.add(fr);
                }
            }

            // Kullanıcının görebildiği hiç alan yoksa task'i gösterme
            if (fields.isEmpty()) {
                continue;
            }

            tr.setForm(fields);
            responseList.add(tr);
        }

        return responseList;
    }

    private boolean hasPermission(Long userId, Long bilesenId, String yetkiTipi) {

        List<FormBileseniAtama> atamalar =
                atamaRepository.findByBilesenId(bilesenId);

        // hiç atama yoksa herkes görebilsin/düzenleyebilsin mantığı
        if (atamalar.isEmpty()) {
            return true;
        }

        for (FormBileseniAtama a : atamalar) {

            boolean yetkiUygun =
                    yetkiTipi.equals(a.getYetkiTipi()) ||
                            ("VIEW".equals(yetkiTipi) && "EDIT".equals(a.getYetkiTipi()));

            if (!yetkiUygun) {
                continue;
            }

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