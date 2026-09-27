package com.example.backend.controller;

import com.example.backend.model.Vault;
import com.example.backend.model.VaultStatus;
import com.example.backend.service.VaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vaults")
@CrossOrigin(origins = "http://localhost:3000")
public class VaultController {

    @Autowired
    private VaultService vaultService;

    // CREATE
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createVault(@PathVariable Long userId,@RequestBody Vault vault) {
            return vaultService.createVault(userId, vault);
    }
    @GetMapping
     public ResponseEntity<?> getAllVaults() {
            return vaultService.getAllVaults();
    }

    // GET all vaults for user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserVaults(@PathVariable Long userId) {
        return vaultService.getUserVaults(userId);
    }

    // GET vaults by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getVaultsByStatus(@PathVariable String status) {
       return vaultService.getVaultsByStatus(status);
    }

    // GET vault by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getVault(@PathVariable Long id) {
        return vaultService.getVaultById(id);
    }

    // UPDATE vault
    @PatchMapping("/{id}/user/{userId}")
    public ResponseEntity<?> updateVault( @PathVariable Long id, @PathVariable Long userId, @RequestBody Vault vaultDetails) {
        return vaultService.updateVault(id, userId, vaultDetails);
    }

    // UPDATE status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus( @PathVariable Long id, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        return vaultService.updateStatus(id, status);
    }

    // DELETE vault
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVault(@PathVariable Long id,@RequestBody String token) {
        try {
            return vaultService.deleteVault(id,token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}