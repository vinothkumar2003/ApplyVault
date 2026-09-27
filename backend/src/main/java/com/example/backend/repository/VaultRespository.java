package com.example.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.model.Vault;
import com.example.backend.model.VaultStatus;


public interface VaultRespository extends JpaRepository<Vault, Long> {
    List<Vault> findByUserId(Long id);
    List<Vault> findByStatus(String status);
    List<Vault> findByUserIdAndStatus(Long userId, String status);

}
