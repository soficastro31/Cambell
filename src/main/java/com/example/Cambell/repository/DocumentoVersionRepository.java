package com.example.Cambell.repository;

import com.example.Cambell.model.DocumentoVersion;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoVersionRepository extends JpaRepository<DocumentoVersion, Long> {
    List<DocumentoVersion> findByTrabajadorOrderByVersionDesc(Usuario trabajador);
    Optional<DocumentoVersion> findByTrabajadorAndTipoAndVigenteTrue(Usuario trabajador, DocumentoVersion.TipoDocumento tipo);
    List<DocumentoVersion> findByVigenteTrue();
}