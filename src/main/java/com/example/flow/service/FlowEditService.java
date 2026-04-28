package com.example.flow.service;

import com.example.flow.dto.*;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class FlowEditService {

    private final AkisRepository akisRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final FormRepository formRepository;
    private final FormBileseniRepository bilesenRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final BilesenSecenegiRepository secenekRepository;
    private final FlowBaslatmaYetkiRepository yetkiRepository;

    // =====================================================
    // 🔥 1. FLOW UPDATE
    // =====================================================
    public void updateFlow(Long flowId, FlowUpdateRequest request) {

        Akis akis = akisRepository.findById(flowId)
                .orElseThrow(() -> new RuntimeException("Flow bulunamadı"));

        if (request.getFlowName() != null)
            akis.setAkisAdi(request.getFlowName());

        if (request.getAciklama() != null)
            akis.setAciklama(request.getAciklama());

        akisRepository.save(akis);

        // 🔥 YETKİLER
        if (request.getBaslatmaYetkileri() != null) {

            yetkiRepository.deleteByAkisId(flowId);

            for (BaslatmaYetkiDto y : request.getBaslatmaYetkileri()) {

                FlowBaslatmaYetki yetki = new FlowBaslatmaYetki();
                yetki.setAkisId(flowId);
                yetki.setTip(y.getTip());
                yetki.setRefId(y.getRefId());

                yetkiRepository.save(yetki);
            }
        }
    }

    // =====================================================
    // 🔥 2. STEP UPDATE
    // =====================================================
    public void updateStep(Long stepId, StepUpdateRequest request) {

        AkisAdim step = akisAdimRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step bulunamadı"));

        if (request.getStepName() != null)
            step.setAdimAdi(request.getStepName());

        if (request.getStepOrder() != null)
            step.setAdimSirasi(request.getStepOrder());

        if (request.getRequiredApprovalCount() != null)
            step.setGerekliOnaySayisi(request.getRequiredApprovalCount());

        if (request.getExternalFlowEnabled() != null)
            step.setExternalFlowEnabled(request.getExternalFlowEnabled());

        if (request.getExternalFlowId() != null)
            step.setExternalFlowId(request.getExternalFlowId());

        if (request.getWaitForExternalFlowCompletion() != null)
            step.setWaitForExternal(request.getWaitForExternalFlowCompletion());

        if (request.getResumeParentAfterSubFlow() != null)
            step.setResumeParent(request.getResumeParentAfterSubFlow());

        if (request.getCancelBehavior() != null)
            step.setCancelBehavior(request.getCancelBehavior());

        akisAdimRepository.save(step);
    }

    // =====================================================
    // 🔥 3. FIELD UPDATE
    // =====================================================
    public void updateField(Long fieldId, FieldUpdateRequest request) {

        FormBileseni field = bilesenRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field bulunamadı"));

        if (request.getLabel() != null)
            field.setLabel(request.getLabel());

        if (request.getPlaceholder() != null)
            field.setPlaceholder(request.getPlaceholder());

        if (request.getRequired() != null)
            field.setZorunlu(request.getRequired());

        bilesenRepository.save(field);

        // 🔥 PERMISSION RESET
        if (request.getPermissions() != null) {

            atamaRepository.deleteAll(
                    atamaRepository.findByBilesenId(fieldId)
            );

            for (PermissionDto p : request.getPermissions()) {

                FormBileseniAtama atama = new FormBileseniAtama();
                atama.setBilesenId(fieldId);
                atama.setTip(p.getTip());
                atama.setRefId(p.getRefId());
                atama.setYetkiTipi(p.getYetkiTipi());

                atamaRepository.save(atama);
            }
        }

        // 🔥 OPTION RESET
        if (request.getOptions() != null) {

            secenekRepository.deleteAll(
                    secenekRepository.findByBilesen_BilesenId(fieldId)
            );

            for (OptionSaveRequest o : request.getOptions()) {

                BilesenSecenegi secenek = new BilesenSecenegi();
                secenek.setBilesen(field);
                secenek.setEtiket(o.getLabel());
                secenek.setDeger(o.getValue());

                secenekRepository.save(secenek);
            }
        }
    }

    // =====================================================
    // 🔥 4. STEP CREATE
    // =====================================================
    public Long createStep(Long flowId, StepUpdateRequest request) {

        Akis akis = akisRepository.findById(flowId)
                .orElseThrow(() -> new RuntimeException("Flow bulunamadı"));

        AkisAdim step = new AkisAdim();

        step.setAkis(akis);
        step.setAdimAdi(request.getStepName());
        step.setAdimSirasi(request.getStepOrder());
        step.setGerekliOnaySayisi(request.getRequiredApprovalCount());

        step.setExternalFlowEnabled(request.getExternalFlowEnabled());
        step.setExternalFlowId(request.getExternalFlowId());
        step.setWaitForExternal(request.getWaitForExternalFlowCompletion());
        step.setResumeParent(request.getResumeParentAfterSubFlow());
        step.setCancelBehavior(request.getCancelBehavior());

        akisAdimRepository.save(step);

        return step.getAdimId();
    }

    // =====================================================
    // 🔥 5. FIELD CREATE (FIXED)
    // =====================================================
    public Long createField(Long stepId, FieldCreateRequest request) {

        // 🔥 FORM'u doğru yerden çek
        Form form = formRepository.findByAdimId(stepId)
                .orElseThrow(() -> new RuntimeException("Form bulunamadı"));

        FormBileseni field = new FormBileseni();
        field.setForm(form);
        field.setLabel(request.getLabel());
        field.setPlaceholder(request.getPlaceholder());
        field.setZorunlu(request.getRequired());
        field.setBilesenTipi(request.getType()); // TEXT, FILE, SELECT

        bilesenRepository.save(field);

        Long fieldId = field.getBilesenId();

        // 🔥 PERMISSIONS
        if (request.getPermissions() != null) {
            for (PermissionDto p : request.getPermissions()) {

                FormBileseniAtama atama = new FormBileseniAtama();
                atama.setBilesenId(fieldId);
                atama.setTip(p.getTip());
                atama.setRefId(p.getRefId());
                atama.setYetkiTipi(p.getYetkiTipi());

                atamaRepository.save(atama);
            }
        }

        // 🔥 OPTIONS
        if (request.getOptions() != null) {
            for (OptionSaveRequest o : request.getOptions()) {

                BilesenSecenegi secenek = new BilesenSecenegi();
                secenek.setBilesen(field);
                secenek.setEtiket(o.getLabel());
                secenek.setDeger(o.getValue());

                secenekRepository.save(secenek);
            }
        }

        return fieldId;
    }
}