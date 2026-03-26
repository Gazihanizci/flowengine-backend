package com.example.flow.service;

import com.example.flow.dto.KullaniciMeResponse;
import com.example.flow.repository.KullaniciRepository;
import com.example.flow.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;
    private final CurrentUser currentUser;

    public List<KullaniciMeResponse> getMyInfo() {

        Long userId = currentUser.id();

        if (userId == null) {
            throw new RuntimeException("Token yok veya geçersiz");
        }

        return kullaniciRepository.findMyRoles(userId);
    }
}