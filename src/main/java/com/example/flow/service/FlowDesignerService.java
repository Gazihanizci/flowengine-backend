package com.example.flow.service;

import com.example.flow.dto.*;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlowDesignerService {

    private final AkisRepository akisRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final FormRepository formRepository;
    private final FormBileseniRepository formBileseniRepository;
    private final BilesenSecenegiRepository bilesenSecenegiRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final FlowBaslatmaYetkiRepository yetkiRepository;

    @Transactional
    public FlowSaveResponse saveFlow(FlowSaveRequest request) {

        Akis akis = new Akis();
        akis.setAkisAdi(request.getFlowName());
        akis.setAciklama(request.getAciklama());
        akis.setAktif(true);
        akis = akisRepository.save(akis);

        if (request.getBaslatmaYetkileri() != null) {
            for (BaslatmaYetkiDto y : request.getBaslatmaYetkileri()) {
                FlowBaslatmaYetki yetki = new FlowBaslatmaYetki();
                yetki.setAkisId(akis.getAkisId());
                yetki.setTip(y.getTip());
                yetki.setRefId(y.getRefId());
                yetkiRepository.save(yetki);
            }
        }

        if (request.getSteps() == null || request.getSteps().isEmpty()) {
            return new FlowSaveResponse(
                    akis.getAkisId(),
                    "Flow başarıyla kaydedildi"
            );
        }

        List<StepSaveRequest> sortedSteps = new ArrayList<>(request.getSteps());
        sortedSteps.sort(Comparator.comparing(StepSaveRequest::getStepOrder));

        for (StepSaveRequest stepRequest : sortedSteps) {

            AkisAdim adim = new AkisAdim();
            adim.setAkis(akis);
            adim.setAdimAdi(stepRequest.getStepName());
            adim.setAdimSirasi(stepRequest.getStepOrder());

            // 🔥 YENİ EKLENEN ALAN (ÇOKLU ONAY)
            adim.setGerekliOnaySayisi(
                    stepRequest.getRequiredApprovalCount() != null
                            ? stepRequest.getRequiredApprovalCount()
                            : 1
            );

            adim.setExternalFlowEnabled(
                    Boolean.TRUE.equals(stepRequest.getExternalFlowEnabled())
            );

            adim.setExternalFlowId(resolveExternalFlowId(stepRequest));

            adim.setWaitForExternal(
                    stepRequest.getWaitForExternalFlowCompletion() != null
                            ? stepRequest.getWaitForExternalFlowCompletion()
                            : true
            );

            adim.setResumeParent(
                    stepRequest.getResumeParentAfterSubFlow() != null
                            ? stepRequest.getResumeParentAfterSubFlow()
                            : true
            );

            adim.setCancelBehavior(
                    stepRequest.getCancelBehavior() != null
                            ? stepRequest.getCancelBehavior()
                            : "PROPAGATE"
            );

            adim = akisAdimRepository.save(adim);

            Form form = new Form();
            form.setAdim(adim);
            form.setFormAdi(stepRequest.getStepName() + " Formu");
            form = formRepository.save(form);

            if (stepRequest.getFields() == null || stepRequest.getFields().isEmpty()) {
                continue;
            }

            List<FieldSaveRequest> sortedFields = new ArrayList<>(stepRequest.getFields());
            sortedFields.sort(Comparator.comparing(f ->
                    f.getOrderNo() == null ? Integer.MAX_VALUE : f.getOrderNo()
            ));

            int autoOrder = 1;

            for (FieldSaveRequest fieldRequest : sortedFields) {

                FormBileseni bilesen = new FormBileseni();
                bilesen.setForm(form);
                bilesen.setBilesenTipi(fieldRequest.getType());
                bilesen.setLabel(fieldRequest.getLabel());
                bilesen.setPlaceholder(fieldRequest.getPlaceholder());
                bilesen.setZorunlu(Boolean.TRUE.equals(fieldRequest.getRequired()));
                bilesen.setSiraNo(
                        fieldRequest.getOrderNo() != null ? fieldRequest.getOrderNo() : autoOrder++
                );

                bilesen = formBileseniRepository.save(bilesen);

                if (fieldRequest.getPermissions() != null && !fieldRequest.getPermissions().isEmpty()) {
                    for (PermissionDto p : fieldRequest.getPermissions()) {
                        FormBileseniAtama atama = new FormBileseniAtama();
                        atama.setBilesenId(bilesen.getBilesenId());
                        atama.setTip(p.getTip());
                        atama.setRefId(p.getRefId());
                        atama.setYetkiTipi(p.getYetkiTipi());
                        atamaRepository.save(atama);
                    }
                }

                if (("COMBOBOX".equalsIgnoreCase(fieldRequest.getType())
                        || "RADIO".equalsIgnoreCase(fieldRequest.getType()))
                        && fieldRequest.getOptions() != null
                        && !fieldRequest.getOptions().isEmpty()) {

                    for (OptionSaveRequest optionRequest : fieldRequest.getOptions()) {
                        BilesenSecenegi secenek = new BilesenSecenegi();
                        secenek.setBilesen(bilesen);
                        secenek.setEtiket(optionRequest.getLabel());
                        secenek.setDeger(optionRequest.getValue());
                        bilesenSecenegiRepository.save(secenek);
                    }
                }
            }
        }

        return new FlowSaveResponse(
                akis.getAkisId(),
                "Flow başarıyla kaydedildi"
        );
    }

    private Long resolveExternalFlowId(StepSaveRequest step) {
        if (step.getExternalFlowId() != null) {
            return step.getExternalFlowId();
        }

        if (step.getSubFlowId() != null) {
            return step.getSubFlowId();
        }

        return step.getNextFlowId();
    }
}