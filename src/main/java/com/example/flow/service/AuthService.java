package com.example.flow.service;

import com.example.flow.dto.AuthResponse;
import com.example.flow.dto.LoginRequest;
import com.example.flow.dto.RegisterRequest;
import com.example.flow.entity.Kullanici;
import com.example.flow.repository.KullaniciRepository;
import com.example.flow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // =========================
    // ✅ REGISTER
    // =========================
    public AuthResponse register(RegisterRequest request) {

        // email kontrol
        if (kullaniciRepository.findByEmail(request.getEmail()).isPresent()) {
            return new AuthResponse("Bu email zaten kayıtlı", false, null);
        }

        // kullanıcı oluştur
        Kullanici user = Kullanici.builder()
                .adSoyad(request.getAdSoyad())
                .email(request.getEmail())
                .parolaHash(passwordEncoder.encode(request.getPassword()))
                .build();

        kullaniciRepository.save(user);

        return new AuthResponse("Kayıt başarılı", true, null);
    }

    // =========================
    // ✅ LOGIN (JWT üretir)
    // =========================
    public AuthResponse login(LoginRequest request) {

        Kullanici user = kullaniciRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return new AuthResponse("Kullanıcı bulunamadı", false, null);
        }

        // şifre kontrol
        if (!passwordEncoder.matches(request.getPassword(), user.getParolaHash())) {
            return new AuthResponse("Şifre yanlış", false, null);
        }

        // 🔥 JWT üret
        String token = jwtService.generateToken(
                user.getKullaniciId(),
                user.getEmail()
        );

        return new AuthResponse("Giriş başarılı", true, token);
    }
}