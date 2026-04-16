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
    private final AkisRepository akisRepository;
    private final AkisSurecRepository akisSurecRepository;
    public List<TaskResponse> getMyTasks(Long userId) {

        // 1. Kullanıcının aktif task'larını al
        List<SurecAdim> aktifTasklar =
                surecAdimRepository.findByAtananKullaniciIdAndDurum(userId, "BEKLIYOR");

        // 2. Bu task'ların süreçlerini bul
        Set<Long> surecIdSet = new HashSet<>();
        for (SurecAdim t : aktifTasklar) {
            surecIdSet.add(t.getSurecId());
        }

        List<TaskResponse> responseList = new ArrayList<>();

        // 3. Her süreç için tüm step'leri getir
        for (Long surecId : surecIdSet) {

            List<SurecAdim> tumAdimlar =
                    surecAdimRepository.findBySurecId(surecId);

            // 4. Duplicate step'leri temizle (aynı adımId tek olsun)
            Map<Long, SurecAdim> uniqueSteps = new LinkedHashMap<>();

            for (SurecAdim task : tumAdimlar) {

                Long adimId = task.getAdimId();

                if (!uniqueSteps.containsKey(adimId)) {
                    uniqueSteps.put(adimId, task);
                } else {

                    // kullanıcıya ait olanı tercih et
                    SurecAdim mevcut = uniqueSteps.get(adimId);

                    boolean mevcutBenimMi =
                            mevcut.getAtananKullaniciId() != null &&
                                    mevcut.getAtananKullaniciId().equals(userId);

                    boolean yeniBenimMi =
                            task.getAtananKullaniciId() != null &&
                                    task.getAtananKullaniciId().equals(userId);

                    if (!mevcutBenimMi && yeniBenimMi) {
                        uniqueSteps.put(adimId, task);
                    }
                }
            }

            // 5. Step'leri sırala (id ile, varsa adimSirasi kullan)
            List<SurecAdim> siraliAdimlar = new ArrayList<>(uniqueSteps.values());
            siraliAdimlar.sort(Comparator.comparing(SurecAdim::getId));

            // 6. Response oluştur
            for (SurecAdim task : siraliAdimlar) {

                boolean isCurrentUserTask =
                        task.getAtananKullaniciId() != null
                                && task.getAtananKullaniciId().equals(userId)
                                && "BEKLIYOR".equals(task.getDurum());

                TaskResponse tr = new TaskResponse();
                tr.setTaskId(task.getId());
                tr.setSurecId(task.getSurecId());
                tr.setAdimId(task.getAdimId());

                AkisAdim step = akisAdimRepository
                        .findById(task.getAdimId())
                        .orElseThrow();

                tr.setAdimAdi(step.getAdimAdi());
// 🔥 FLOW BİLGİSİ EKLE
                AkisSurec akisSurec = akisSurecRepository
                        .findBySurecId(task.getSurecId())
                        .orElse(null);

                if (akisSurec != null) {
                    Akis akis = akisRepository
                            .findById(akisSurec.getAkisId())
                            .orElse(null);

                    if (akis != null) {
                        tr.setAkisAdi(akis.getAkisAdi());
                        tr.setAkisAciklama(akis.getAciklama());
                    }
                }
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

                    Map<Long, List<OptionResponse>> optionMap = getOptionMap();

                    for (FormBileseni b : bilesenler) {

                        boolean canView = hasPermission(userId, b.getBilesenId(), "VIEW");
                        if (!canView) continue;

                        boolean canEdit = isCurrentUserTask &&
                                hasPermission(userId, b.getBilesenId(), "EDIT");

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

                if (fields.isEmpty()) continue;

                tr.setForm(fields);
                responseList.add(tr);
            }
        }

        return responseList;
    }

    // 🔹 OPTION MAP
    private Map<Long, List<OptionResponse>> getOptionMap() {

        List<BilesenSecenegi> tumSecenekler = bilesenSecenegiRepository.findAll();

        Map<Long, List<OptionResponse>> optionMap = new HashMap<>();

        for (BilesenSecenegi s : tumSecenekler) {

            Long bilesenId = s.getBilesen().getBilesenId();

            optionMap.putIfAbsent(bilesenId, new ArrayList<>());

            OptionResponse op = new OptionResponse();
            op.setLabel(s.getEtiket());
            op.setValue(s.getDeger());

            optionMap.get(bilesenId).add(op);
        }

        return optionMap;
    }

    // 🔹 PERMISSION
    private boolean hasPermission(Long userId, Long bilesenId, String yetkiTipi) {

        List<FormBileseniAtama> atamalar =
                atamaRepository.findByBilesenId(bilesenId);

        if (atamalar.isEmpty()) return true;

        for (FormBileseniAtama a : atamalar) {

            boolean yetkiUygun =
                    yetkiTipi.equals(a.getYetkiTipi()) ||
                            ("VIEW".equals(yetkiTipi) && "EDIT".equals(a.getYetkiTipi()));

            if (!yetkiUygun) continue;

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