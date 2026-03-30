package com.example.flow.service;

import com.example.flow.dto.FlowFieldFlatResponse;
import com.example.flow.dto.FlowListResponse;
import com.example.flow.entity.FormBileseniAtama;
import com.example.flow.repository.AkisRepository;
import com.example.flow.repository.FormBileseniAtamaRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FlowQueryService {

    private final AkisRepository akisRepository;
    private final FormBileseniAtamaRepository atamaRepository; // 🔥 EKLENDİ

    public FlowQueryService(AkisRepository akisRepository,
                            FormBileseniAtamaRepository atamaRepository) {
        this.akisRepository = akisRepository;
        this.atamaRepository = atamaRepository;
    }

    public List<FlowListResponse> getFlows() {
        return akisRepository.getAllFlows();
    }

    public Map<String, Object> getFlowFull(Long flowId) {

        List<FlowFieldFlatResponse> rows =
                akisRepository.getFlowFields(flowId);

        Map<Long, Map<String, Object>> stepMap = new LinkedHashMap<>();

        for (FlowFieldFlatResponse row : rows) {

            stepMap.putIfAbsent(row.getAdimId(), new LinkedHashMap<>());

            Map<String, Object> step = stepMap.get(row.getAdimId());

            step.putIfAbsent("stepId", row.getAdimId());
            step.putIfAbsent("stepName", row.getAdimAdi());
            step.putIfAbsent("fields", new ArrayList<>());

            List<Map<String, Object>> fields =
                    (List<Map<String, Object>>) step.get("fields");

            Map<String, Object> field = fields.stream()
                    .filter(f -> f.get("fieldId").equals(row.getBilesenId()))
                    .findFirst()
                    .orElse(null);

            if (field == null) {

                // 🔥 ATAMA ÇEK
                List<FormBileseniAtama> atamalar =
                        atamaRepository.findByBilesenId(row.getBilesenId());

                List<Long> userIds = new ArrayList<>();
                List<Long> roleIds = new ArrayList<>();

                for (FormBileseniAtama a : atamalar) {
                    if ("USER".equals(a.getTip())) {
                        userIds.add(a.getRefId());
                    }
                    if ("ROLE".equals(a.getTip())) {
                        roleIds.add(a.getRefId());
                    }
                }

                field = new LinkedHashMap<>();
                field.put("fieldId", row.getBilesenId());
                field.put("type", row.getBilesenTipi());
                field.put("label", row.getLabel());
                field.put("placeholder", row.getPlaceholder());
                field.put("required", row.getZorunlu());
                field.put("orderNo", row.getSiraNo());
                field.put("options", new ArrayList<>());

                // 🔥 YENİ EKLENENLER
                field.put("userIds", userIds);
                field.put("roleIds", roleIds);

                fields.add(field);
            }

            if (row.getOptionLabel() != null) {
                List<Map<String, String>> options =
                        (List<Map<String, String>>) field.get("options");

                Map<String, String> opt = new HashMap<>();
                opt.put("label", row.getOptionLabel());
                opt.put("value", row.getOptionValue());

                options.add(opt);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();

        if (!rows.isEmpty()) {
            response.put("flowId", rows.get(0).getAkisId());
            response.put("flowName", rows.get(0).getAkisAdi());
            response.put("aciklama", rows.get(0).getAciklama());
        }

        response.put("steps", new ArrayList<>(stepMap.values()));

        return response;
    }
}