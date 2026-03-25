package com.example.flow.service;

import com.example.flow.dto.*;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlowQueryService {

    private final AkisRepository akisRepository;
    private final AkisAdimRepository adimRepository;
    private final FormRepository formRepository;
    private final FormBileseniRepository bilesenRepository;
    private final BilesenSecenegiRepository secenekRepository;

    public FlowQueryService(
            AkisRepository akisRepository,
            AkisAdimRepository adimRepository,
            FormRepository formRepository,
            FormBileseniRepository bilesenRepository,
            BilesenSecenegiRepository secenekRepository
    ) {
        this.akisRepository = akisRepository;
        this.adimRepository = adimRepository;
        this.formRepository = formRepository;
        this.bilesenRepository = bilesenRepository;
        this.secenekRepository = secenekRepository;
    }

    public FlowDetailResponse getFlowDetail(Long flowId) {

        Akis akis = akisRepository.findById(flowId)
                .orElseThrow();

        FlowDetailResponse response = new FlowDetailResponse();
        response.setFlowId(akis.getAkisId());
        response.setFlowName(akis.getAkisAdi());
        response.setAciklama(akis.getAciklama());

        List<AkisAdim> adimlar =
                adimRepository.findByAkis_AkisIdOrderByAdimSirasi(flowId);

        List<StepDetailResponse> steps = adimlar.stream().map(adim -> {

            StepDetailResponse step = new StepDetailResponse();
            step.setStepId(adim.getAdimId());
            step.setStepName(adim.getAdimAdi());
            step.setStepOrder(adim.getAdimSirasi());

            Form form = formRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getAdim().getAdimId().equals(adim.getAdimId()))
                    .findFirst()
                    .orElse(null);

            if (form == null) return step;

            List<FormBileseni> bilesenler =
                    bilesenRepository.findAll()
                            .stream()
                            .filter(b -> b.getForm().getFormId().equals(form.getFormId()))
                            .toList();

            List<FieldDetailResponse> fields = bilesenler.stream().map(b -> {

                FieldDetailResponse field = new FieldDetailResponse();
                field.setFieldId(b.getBilesenId());
                field.setType(b.getBilesenTipi());
                field.setLabel(b.getLabel());
                field.setPlaceholder(b.getPlaceholder());
                field.setRequired(b.getZorunlu());
                field.setOrderNo(b.getSiraNo());

                if ("COMBOBOX".equalsIgnoreCase(b.getBilesenTipi())) {

                    List<OptionDetailResponse> options =
                            secenekRepository.findAll()
                                    .stream()
                                    .filter(s -> s.getBilesen().getBilesenId().equals(b.getBilesenId()))
                                    .map(s -> {
                                        OptionDetailResponse o = new OptionDetailResponse();
                                        o.setLabel(s.getEtiket());
                                        o.setValue(s.getDeger());
                                        return o;
                                    }).toList();

                    field.setOptions(options);
                }

                return field;

            }).collect(Collectors.toList());

            step.setFields(fields);

            return step;

        }).toList();

        response.setSteps(steps);

        return response;
    }
}