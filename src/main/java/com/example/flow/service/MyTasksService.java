package com.example.flow.service;

import com.example.flow.dto.*;
import com.example.flow.entity.*;
import com.example.flow.entity.Dosya;
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
    private final DosyaRepository dosyaRepository;
    private final SurecRepository surecRepository;
    private final AkisRepository akisRepository;

    public List<TaskResponse> getMyTasks(Long userId) {

        List<SurecAdim> tasks =
                surecAdimRepository.findByAtananKullaniciIdAndDurum(userId, "BEKLIYOR");

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

            // AKIŞ
            AkisSurec surec = surecRepository
                    .findById(task.getSurecId())
                    .orElseThrow();

            Akis akis = akisRepository
                    .findById(surec.getAkisId())
                    .orElseThrow();

            tr.setAkisAdi(akis.getAkisAdi());
            tr.setAkisAciklama(akis.getAciklama());

            // FORM
            Form form = formRepository
                    .findByAdimId(task.getAdimId())
                    .orElse(null);

            List<FieldResponse> fields = new ArrayList<>();

            if (form != null) {

                List<FormBileseni> bilesenler =
                        formBilesenRepository.findByForm_FormId(form.getFormId());

                // 🔥 SADECE KENDİ VERİSİ
                List<FormVeri> veriler =
                        formVeriRepository.findBySurecId(task.getSurecId());

                Map<Long, String> veriMap = new HashMap<>();

                for (FormVeri v : veriler) {
                    veriMap.put(v.getBilesenId(), v.getDeger());
                }

                // OPTIONS
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

                // 🔥 FIELD LOOP (FIX BURADA)
                for (FormBileseni b : bilesenler) {

                    boolean canEdit = hasPermission(userId, b.getBilesenId(), "EDIT");
                    boolean canView = hasPermission(userId, b.getBilesenId(), "VIEW");

                    // 🔥 KRİTİK FIX
                    if (!canEdit && !canView) {
                        continue;
                    }

                    FieldResponse fr = new FieldResponse();
                    fr.setFieldId(b.getBilesenId());
                    fr.setType(b.getBilesenTipi());
                    fr.setLabel(b.getLabel());
                    String rawValue = veriMap.get(b.getBilesenId());

                    if ("FILE".equalsIgnoreCase(b.getBilesenTipi()) && rawValue != null) {

                        try {
                            Long fileId = Long.valueOf(rawValue);

                            Dosya dosya = dosyaRepository.findById(fileId).orElse(null);

                            if (dosya != null) {
                                fr.setValue(dosya.getDosyaAdi()); // görünen isim
                                fr.setFileId(dosya.getDosyaId()); // download için
                            }

                        } catch (Exception e) {
                            fr.setValue(null);
                        }

                    } else {
                        fr.setValue(rawValue);
                    }                    fr.setEditable(canEdit);

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

    // PERMISSION
    private boolean hasPermission(Long userId, Long bilesenId, String yetkiTipi) {

        List<FormBileseniAtama> atamalar =
                atamaRepository.findByBilesenId(bilesenId);

        if (atamalar.isEmpty()) {
            return true;
        }

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