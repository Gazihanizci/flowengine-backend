package com.example.flow.service;

import com.example.flow.dto.FieldSaveRequest;
import com.example.flow.dto.FlowSaveRequest;
import com.example.flow.dto.FlowSaveResponse;
import com.example.flow.dto.OptionSaveRequest;
import com.example.flow.dto.StepSaveRequest;
import com.example.flow.entity.Akis;
import com.example.flow.entity.AkisAdim;
import com.example.flow.entity.BilesenSecenegi;
import com.example.flow.entity.Form;
import com.example.flow.entity.FormBileseni;
import com.example.flow.repository.AkisAdimRepository;
import com.example.flow.repository.AkisRepository;
import com.example.flow.repository.BilesenSecenegiRepository;
import com.example.flow.repository.FormBileseniRepository;
import com.example.flow.repository.FormRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FlowDesignerService {

    private final AkisRepository akisRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final FormRepository formRepository;
    private final FormBileseniRepository formBileseniRepository;
    private final BilesenSecenegiRepository bilesenSecenegiRepository;

    public FlowDesignerService(
            AkisRepository akisRepository,
            AkisAdimRepository akisAdimRepository,
            FormRepository formRepository,
            FormBileseniRepository formBileseniRepository,
            BilesenSecenegiRepository bilesenSecenegiRepository
    ) {
        this.akisRepository = akisRepository;
        this.akisAdimRepository = akisAdimRepository;
        this.formRepository = formRepository;
        this.formBileseniRepository = formBileseniRepository;
        this.bilesenSecenegiRepository = bilesenSecenegiRepository;
    }

    @Transactional
    public FlowSaveResponse saveFlow(FlowSaveRequest request) {
        Akis akis = new Akis();
        akis.setAkisAdi(request.getFlowName());
        akis.setAciklama(request.getAciklama());
        akis.setAktif(true);

        akis = akisRepository.save(akis);

        List<StepSaveRequest> sortedSteps = new ArrayList<>(request.getSteps());
        sortedSteps.sort(Comparator.comparing(StepSaveRequest::getStepOrder));

        for (StepSaveRequest stepRequest : sortedSteps) {
            AkisAdim adim = new AkisAdim();
            adim.setAkis(akis);
            adim.setAdimAdi(stepRequest.getStepName());
            adim.setAdimSirasi(stepRequest.getStepOrder());
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
                bilesen.setSiraNo(fieldRequest.getOrderNo() != null ? fieldRequest.getOrderNo() : autoOrder++);
                bilesen = formBileseniRepository.save(bilesen);

                if ("COMBOBOX".equalsIgnoreCase(fieldRequest.getType())
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

        return new FlowSaveResponse(akis.getAkisId(), "Flow başarıyla kaydedildi");
    }
}