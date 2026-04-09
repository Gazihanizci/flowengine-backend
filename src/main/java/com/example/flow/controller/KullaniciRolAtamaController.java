package com.example.flow.controller;

import com.example.flow.dto.KullaniciRolResponse;
import com.example.flow.service.KullaniciRolAtamaService;
import com.example.flow.service.KullaniciRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rol-atama")
@RequiredArgsConstructor
public class KullaniciRolAtamaController {

    private final KullaniciRolAtamaService kullaniciRolAtamaService;

    // 🔥 ROL ATA
    @PostMapping("/assign")
    public void assignRole(@RequestParam Long kullaniciId,
                           @RequestParam Long rolId) {
        kullaniciRolAtamaService.assignRole(kullaniciId, rolId);
    }

    // 🔥 ROL SİL
    @DeleteMapping("/remove")
    public void removeRole(@RequestParam Long kullaniciId,
                           @RequestParam Long rolId) {
        kullaniciRolAtamaService.removeRole(kullaniciId, rolId);
    }

    // 🔥 ROL GÜNCELLE
    @PutMapping("/update")
    public void updateRole(@RequestParam Long kullaniciId,
                           @RequestParam Long eskiRolId,
                           @RequestParam Long yeniRolId) {
        kullaniciRolAtamaService.updateRole(kullaniciId, eskiRolId, yeniRolId);
    }

    // 🔥 KULLANICIYA AİT ROLLER
    @GetMapping("/{kullaniciId}")
    public List<KullaniciRolResponse> getUserRoles(@PathVariable Long kullaniciId) {
        return kullaniciRolAtamaService.getRoles(kullaniciId);
    }
}