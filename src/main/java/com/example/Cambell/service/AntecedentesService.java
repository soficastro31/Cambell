package com.example.Cambell.service;

import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * HU-S11 / HU-S63: consulta de antecedentes al servicio externo (API). El
 * resultado se asocia al expediente de verificación del trabajador. Si la API
 * no está disponible, no bloquea el flujo: registra el intento como pendiente.
 */
@Service
public class AntecedentesService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${antecedentes.api.url:https://api.antecedentes.local/consulta}")
    private String antecedentesUrl;

    @Value("${antecedentes.api.tiempoLimiteMs:5000}")
    private int tiempoLimiteMs;

    public String consultarYRegistrar(Usuario trabajador) {
        Map<String, String> detalle = java.util.Map.of(
                "documento", trabajador.getNumeroDocumento() != null ? trabajador.getNumeroDocumento() : "",
                "fecha", LocalDateTime.now().toString());
        String resultado;
        try {
            resultado = consultarApi(trabajador.getNumeroDocumento(), trabajador.getNombre());
        } catch (Exception e) {
            System.err.println("Error consultando antecedentes: " + e.getMessage());
            resultado = "{\"estado\":\"PENDIENTE\",\"detalle\":\"API no disponible\",\"apiRespuesta\":"
                    + escapeJson(detalle.toString()) + "}";
        }
        trabajador.setAntecedentes(resultado);
        trabajador.setFechaConsultaAntecedentes(LocalDateTime.now());
        usuarioRepository.save(trabajador);
        return resultado;
    }

    private String consultarApi(String numeroDocumento, String nombre) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
            // Se mantiene el manejo de errores por defecto; los rethrows se capturan abajo
        });

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("documento", numeroDocumento != null ? numeroDocumento : "");
        body.add("nombres", nombre != null ? nombre : "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        Map<?, ?> response = restTemplate.postForObject(antecedentesUrl, request, Map.class);
        if (response == null) {
            throw new RuntimeException("Respuesta vacía de la API de antecedentes");
        }
        return response.toString();
    }

    private String escapeJson(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}