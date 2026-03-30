package com.example.flow.service;

import com.example.flow.dto.KullaniciRolResponse;
import com.example.flow.repository.KullaniciRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KullaniciRolService {

    private final KullaniciRolRepository repository;

    public List<KullaniciRolResponse> getAll() {

        return repository.findAllUsersWithRoles()
                .stream()
                .map(row -> new KullaniciRolResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue(),
                        (String) row[4]
                ))
                .collect(Collectors.toList());
    }
}