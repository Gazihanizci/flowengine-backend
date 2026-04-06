package com.example.flow.service;

import com.example.flow.entity.*;
import com.example.flow.repository.AkisAdimRepository;
import com.example.flow.repository.SurecEventiRepository;
import com.example.flow.repository.SurecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowEngineService {

    private final SurecRepository surecRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final SurecEventiRepository surecEventiRepository;

    @Transactional
    public void startExternalFlow(AkisSurec parentSurec, AkisAdim step, TaskService taskService) {
        parentSurec.setDurum("WAITING_EXTERNAL");
        parentSurec.setMevcutAdimId(step.getAdimId());
        surecRepository.save(parentSurec);

        AkisSurec child = new AkisSurec();
        child.setAkisId(step.getExternalFlowId());
        child.setBaslatanKullaniciId(parentSurec.getBaslatanKullaniciId());
        child.setMevcutAdimId(null);
        child.setDurum("RUNNING");
        child.setBaslamaTarihi(LocalDateTime.now());
        child.setParentSurecId(parentSurec.getSurecId());
        child.setParentAdimId(step.getAdimId());
        child.setResumeAdimSirasi(step.getAdimSirasi() + 1);

        surecRepository.save(child);

        AkisAdim firstChildStep = akisAdimRepository
                .findFirstByAkis_AkisIdOrderByAdimSirasiAsc(step.getExternalFlowId())
                .orElseThrow(() -> new RuntimeException("Child flow ilk adımı bulunamadı"));

        child.setMevcutAdimId(firstChildStep.getAdimId());
        surecRepository.save(child);

        taskService.createTasksForStep(child.getSurecId(), firstChildStep.getAdimId());
    }

    @Transactional
    public void resumeParentIfNeeded(AkisSurec childSurec, TaskService taskService) {
        if (childSurec.getParentSurecId() == null) return;

        String correlationId = "CHILD_COMPLETED_" + childSurec.getSurecId();
        if (surecEventiRepository.existsByCorrelationId(correlationId)) return;

        SurecEventi event = new SurecEventi();
        event.setSurecId(childSurec.getSurecId());
        event.setEventType("CHILD_COMPLETED");
        event.setCorrelationId(correlationId);
        event.setProcessedAt(LocalDateTime.now());
        surecEventiRepository.save(event);

        AkisSurec parent = surecRepository.findById(childSurec.getParentSurecId())
                .orElseThrow(() -> new RuntimeException("Parent süreç bulunamadı"));

        parent.setDurum("RUNNING");

        Optional<AkisAdim> next = akisAdimRepository
                .findFirstByAkis_AkisIdAndAdimSirasiGreaterThanOrderByAdimSirasiAsc(
                        parent.getAkisId(),
                        childSurec.getResumeAdimSirasi() - 1
                );

        if (next.isEmpty()) {
            parent.setDurum("TAMAMLANDI");
            parent.setBitisTarihi(LocalDateTime.now());
            surecRepository.save(parent);
            return;
        }

        parent.setMevcutAdimId(next.get().getAdimId());
        surecRepository.save(parent);
        taskService.createTasksForStep(parent.getSurecId(), next.get().getAdimId());
    }

    @Transactional
    public void handleChildRejected(AkisSurec childSurec) {
        if (childSurec.getParentSurecId() == null) return;

        AkisSurec parent = surecRepository.findById(childSurec.getParentSurecId())
                .orElseThrow(() -> new RuntimeException("Parent süreç bulunamadı"));

        AkisAdim parentStep = akisAdimRepository.findById(childSurec.getParentAdimId())
                .orElseThrow(() -> new RuntimeException("Parent step bulunamadı"));

        String davranis = parentStep.getCancelBehavior();

        if (davranis == null || "PROPAGATE".equalsIgnoreCase(davranis)) {
            parent.setDurum("REDDEDILDI");
            parent.setBitisTarihi(LocalDateTime.now());
            surecRepository.save(parent);
        } else if ("KEEP_WAITING".equalsIgnoreCase(davranis)) {
            parent.setDurum("WAITING_EXTERNAL");
            surecRepository.save(parent);
        }
    }
}