package com.example.Cambell.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * HU-S21 / HU-S73: cifrado de los archivos sensibles (documentos de identidad
 * y selfies biométricas) antes de su almacenamiento en disco. Solo los procesos
 * y roles autorizados pueden descifrarlos.
 */
@Service
public class CifradoArchivosService {

    private static final int IV_LENGTH = 12;

    @Value("${app.cifrado.clave}")
    private String claveCifrado;

    private SecretKey derivarClave() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha.digest(claveCifrado.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(Arrays.copyOf(digest, 16), "AES");
    }

    // Cifra el archivo: [IV + ciphertext] en Base64
    public String cifrar(byte[] contenido) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new java.security.SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, derivarClave(), new GCMParameterSpec(128, iv));
            byte[] cifrado = cipher.doFinal(contenido);
            byte[] completo = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, completo, 0, iv.length);
            System.arraycopy(cifrado, 0, completo, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(completo);
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar el archivo: " + e.getMessage(), e);
        }
    }

    public byte[] descifrar(String contenidoBase64) {
        try {
            byte[] completo = Base64.getDecoder().decode(contenidoBase64);
            byte[] iv = Arrays.copyOfRange(completo, 0, IV_LENGTH);
            byte[] cifrado = Arrays.copyOfRange(completo, IV_LENGTH, completo.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, derivarClave(), new GCMParameterSpec(128, iv));
            return cipher.doFinal(cifrado);
        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar el archivo: " + e.getMessage(), e);
        }
    }

    // Cifra y guarda en disco con extensión .enc
    public String cifrarYGuardar(byte[] contenido, String carpeta, String nombreBase) {
        try {
            java.io.File dir = new java.io.File(carpeta);
            if (!dir.exists()) dir.mkdirs();
            String nombre = nombreBase + ".enc";
            java.nio.file.Files.write(java.nio.file.Path.of(carpeta, nombre), cifrar(contenido).getBytes(StandardCharsets.UTF_8));
            return java.nio.file.Path.of(carpeta, nombre).toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar archivo cifrado: " + e.getMessage(), e);
        }
    }

    // Descifra un archivo .enc del disco y devuelve su contenido en claro,
    // para uso exclusivo de los procesos autorizados (ej. verificación facial).
    public byte[] descifrarArchivo(String rutaArchivoEnc) {
        try {
            String contenido = new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(rutaArchivoEnc)), StandardCharsets.UTF_8);
            return descifrar(contenido);
        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar archivo del disco: " + e.getMessage(), e);
        }
    }
}