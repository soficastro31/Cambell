package com.example.Cambell.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Map;

@Service
public class VerificacionFacialService {

    @Value("${faceplusplus.api.key}")
    private String apiKey;

    @Value("${faceplusplus.api.secret}")
    private String apiSecret;

    @Value("${faceplusplus.api.url}")
    private String apiUrl;

    @Value("${faceplusplus.ocr.url:https://api-us.faceplusplus.com/imagepp/v1/recognizetext}")
    private String ocrUrl;

    // HU-S21: los archivos se guardan cifrados (.enc); la API solo recibe el
    // contenido descifrado de forma temporal.
    @Autowired
    private CifradoArchivosService cifradoArchivosService;

    public double compararRostros(String rutaImagen1, String rutaImagen2) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("api_key", apiKey);
            body.add("api_secret", apiSecret);
            body.add("image_file1", recursoTemporal(rutaImagen1));
            body.add("image_file2", recursoTemporal(rutaImagen2));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(apiUrl, requestEntity, Map.class);

            if (response != null && response.containsKey("confidence")) {
                return ((Number) response.get("confidence")).doubleValue();
            }
            return -1;
        } catch (Exception e) {
            System.err.println("Error al comparar rostros con Face++: " + e.getMessage());
            return -1;
        } finally {
            limpiarTemporales();
        }
    }

    /**
     * Usa el OCR universal de Face++ (recognizetext) para leer el texto de la foto del documento
     * y verifica si contiene el número de documento ingresado.
     */
    public boolean verificarNumeroDocumento(String rutaDocumento, String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            return false;
        }
        String textoExtraido;
        try {
            textoExtraido = extraerTexto(rutaDocumento);
        } catch (Exception e) {
            System.err.println("Error OCR al leer documento: " + e.getMessage());
            return false;
        } finally {
            limpiarTemporales();
        }
        if (textoExtraido == null || textoExtraido.isBlank()) {
            return false;
        }
        String numeroNormalizado = numeroDocumento.replaceAll("\\D", "");
        String textoNormalizado = textoExtraido.replaceAll("\\s+", "");
        return numeroNormalizado.length() > 5 && textoNormalizado.contains(numeroNormalizado);
    }

    private String extraerTexto(String rutaImagen) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("api_key", apiKey);
        body.add("api_secret", apiSecret);
        body.add("image_file", recursoTemporal(rutaImagen));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(ocrUrl, requestEntity, Map.class);

        if (response == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Object result = response.get("result");
        if (result instanceof java.util.List<?> lineas) {
            for (Object item : lineas) {
                if (item instanceof Map<?, ?> lineaMap && lineaMap.get("text") != null) {
                    sb.append(lineaMap.get("text")).append(" ");
                }
            }
        }
        return sb.toString();
    }

    // Devuelve un archivo en claro (temporal) a partir de la ruta guardada;
    // si el archivo está cifrado (.enc) lo descifra primero.
    private File recursoTemporal(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            throw new RuntimeException("Ruta de archivo vacía");
        }
        File original = new File(ruta);
        if (!ruta.endsWith(".enc") || !original.exists()) {
            return original;
        }
        byte[] claro = cifradoArchivosService.descifrarArchivo(ruta);
        File temporal = null;
        try {
            temporal = File.createTempFile("cambell_", ".tmp");
            java.nio.file.Files.write(temporal.toPath(), claro);
            temporales.add(temporal);
            return temporal;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo descifrar la imagen: " + e.getMessage(), e);
        }
    }

    private final java.util.List<File> temporales = new java.util.ArrayList<>();

    private void limpiarTemporales() {
        for (File f : temporales) {
            if (f != null && f.exists()) f.delete();
        }
        temporales.clear();
    }
}