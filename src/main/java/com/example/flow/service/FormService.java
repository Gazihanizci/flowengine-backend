package com.example.flow.service;

import com.example.flow.entity.Form;
import com.example.flow.entity.FormBileseni;
import com.example.flow.entity.FormVeri;
import com.example.flow.repository.FormBileseniRepository;
import com.example.flow.repository.FormRepository;
import com.example.flow.repository.FormVeriRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormRepository formRepository;
    private final FormBileseniRepository formBilesenRepository;
    private final FormVeriRepository formVeriRepository;
    private final FieldPermissionService fieldPermissionService;
    @Transactional
    public void saveFormDraft(Long surecId, Long userId, Map<Long, String> formData) {

        if (formData == null) return;

        for (Map.Entry<Long, String> entry : formData.entrySet()) {

            fieldPermissionService.validate(userId, entry.getKey());

            Optional<FormVeri> existing =
                    formVeriRepository.findBySurecIdAndBilesenId(surecId, entry.getKey());

            FormVeri fv = existing.orElse(new FormVeri());
            fv.setSurecId(surecId);
            fv.setBilesenId(entry.getKey());
            fv.setDeger(entry.getValue());
            fv.setKaydedenKullaniciId(userId);
            fv.setKayitTarihi(LocalDateTime.now());

            formVeriRepository.save(fv);
        }
    }
    @Transactional
    public void validateAndSaveFormData(Long surecId, Long adimId, Long userId, Map<Long, String> formData) {
        Form form = formRepository.findByAdimId(adimId).orElse(null);

        if (form != null) {
            List<FormBileseni> bilesenler = formBilesenRepository.findByForm_FormId(form.getFormId());
            for (FormBileseni b : bilesenler) {
                if (Boolean.TRUE.equals(b.getZorunlu())) {
                    if (formData == null || !formData.containsKey(b.getBilesenId())) {
                        throw new RuntimeException("Zorunlu alan boş: " + b.getLabel());
                    }
                }
            }
        }

        if (formData != null) {
            for (Map.Entry<Long, String> entry : formData.entrySet()) {
                fieldPermissionService.validate(userId, entry.getKey());

                Optional<FormVeri> existing = formVeriRepository.findBySurecIdAndBilesenId(surecId, entry.getKey());

                FormVeri fv = existing.orElse(new FormVeri());
                fv.setSurecId(surecId);
                fv.setBilesenId(entry.getKey());
                fv.setDeger(entry.getValue());
                fv.setKaydedenKullaniciId(userId);
                fv.setKayitTarihi(LocalDateTime.now());

                formVeriRepository.save(fv);
            }
        }
    }
}