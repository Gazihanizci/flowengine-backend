package com.example.flow.service;

import com.example.flow.entity.FlowBaslatmaYetki;
import com.example.flow.repository.FlowBaslatmaYetkiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlowBaslatmaYetkiService {

    private final FlowBaslatmaYetkiRepository repository;

    // 🔥 TÜMÜNÜ GETİR
    public List<FlowBaslatmaYetki> getByAkisId(Long akisId) {
        return repository.findByAkisId(akisId);
    }

    // 🔥 EKLE
    public FlowBaslatmaYetki add(Long akisId, String tip, Long refId) {

        FlowBaslatmaYetki yetki = new FlowBaslatmaYetki();
        yetki.setAkisId(akisId);
        yetki.setTip(tip);
        yetki.setRefId(refId);

        return repository.save(yetki);
    }

    // 🔥 SİL
    public void delete(Long id) {
        repository.deleteById(id);
    }

    // 🔥 TÜMÜNÜ GETİR
    public List<FlowBaslatmaYetki> getAll() {
        return repository.findAll();
    }
}