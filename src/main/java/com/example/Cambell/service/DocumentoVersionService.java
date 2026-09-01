package com.example.Cambell.service;

import com.example.Cambell.model.DocumentoVersion;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.DocumentoVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * HU-S21/22/23 (S73/S74/S75): gestión segura de los documentos de los
 * trabajadores.
 *  - S73/S21: cifra el archivo antes de guardarlo en disco.
 *  - S75/S23: al reemplazar un documento crea una nueva versión vigente y
 *    conserva la anterior en el historial.
 *  - S74/S22: elimina físicamente las versiones obsoletas tras el periodo de
 *    retención configurado, conservando el registro.
 */
@Service
public class DocumentoVersionService {

    @Autowired
    private DocumentoVersionRepository documentoRepository;

    @Autowired
    private CifradoArchivosService cifradoService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${cambell.archivos.retencion-dias:365}")
    private int diasRetencion;

    @Value("${cambell.archivos.cron-limpieza:0 0 4 * * *}")
    private String cronLimpieza;

    // Guarda (cifrado) un documento y lo versiona. Devuelve la ruta física.
    public String guardarDocumentoCifrado(Usuario trabajador, DocumentoVersion.TipoDocumento tipo, MultipartFile archivo) {
        try {
            String ruta = cifradoService.cifrarYGuardar(archivo.getBytes(), uploadDir, UUID.randomUUID() + "_" + sanitizar(archivo.getOriginalFilename()));
            guardarVersion(trabajador, tipo, ruta);
            return ruta;
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo: " + e.getMessage(), e);
        }
    }

    private String sanitizar(String nombre) {
        if (nombre == null) return "archivo";
        return nombre.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    // HU-S23/S75: crea la nueva versión vigente y desmarca la anterior
    private void guardarVersion(Usuario trabajador, DocumentoVersion.TipoDocumento tipo, String ruta) {
        Optional<DocumentoVersion> vigente = documentoRepository
                .findByTrabajadorAndTipoAndVigenteTrue(trabajador, tipo);
        int siguienteVersion = 1;
        if (vigente.isPresent()) {
            DocumentoVersion anterior = vigente.get();
            anterior.setVigente(false);
            documentoRepository.save(anterior);
            siguienteVersion = anterior.getVersion() + 1;
        }
        DocumentoVersion nuevo = new DocumentoVersion();
        nuevo.setTrabajador(trabajador);
        nuevo.setTipo(tipo);
        nuevo.setRutaArchivo(ruta);
        nuevo.setVersion(siguienteVersion);
        nuevo.setVigente(true);
        documentoRepository.save(nuevo);
    }

    // HU-S22/S74: eliminar físicamente los archivos obsoletos que superen el
    // periodo de retención configurado, conservando el registro del historial.
    public void limpiarArchivosObsoletos() {
        LocalDateTime limite = LocalDateTime.now().minusDays(diasRetencion);
        for (DocumentoVersion v : documentoRepository.findAll()) {
            // Se elimina la versión si fue reemplazada o si es muy antigua
            if ((!v.isVigente() || v.getFecha().isBefore(limite))) {
                borrarFisico(v);
            }
        }
    }

    private void borrarFisico(DocumentoVersion v) {
        try {
            java.io.File f = new java.io.File(v.getRutaArchivo());
            if (f.exists()) f.delete();
        } catch (Exception e) {
            System.err.println("Error al eliminar archivo obsoleto: " + e.getMessage());
        }
    }

    public List<DocumentoVersion> historialDe(Usuario trabajador) {
        return documentoRepository.findByTrabajadorOrderByVersionDesc(trabajador);
    }
}