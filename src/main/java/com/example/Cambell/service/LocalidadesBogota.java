package com.example.Cambell.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catálogo de localidades y barrios de Bogotá con sus coordenadas aproximadas
 * para poblar los filtros multicriterio y el mapa Leaflet.
 */
public class LocalidadesBogota {

    private LocalidadesBogota() {
    }

    /**
     * Localidad -> [Barrio, (lat, lon)]
     */
    public static final Map<String, List<Object[]>> LOCALIDADES = new LinkedHashMap<>();

    static {
        LOCALIDADES.put("Chapinero",
                List.of(
                        new Object[]{"Chapinero Alto", 4.6533, -74.0513},
                        new Object[]{"Chapinero Central", 4.6482, -74.0636},
                        new Object[]{"El Refugio", 4.6667, -74.0536},
                        new Object[]{"Marly", 4.6455, -74.0594},
                        new Object[]{"San Isidro Patios", 4.6481, -74.0447}));

        LOCALIDADES.put("Suba",
                List.of(
                        new Object[]{"Suba Centro", 4.7485, -74.0831},
                        new Object[]{"El Rincón", 4.7627, -74.1009},
                        new Object[]{"Niza", 4.7210, -74.0800},
                        new Object[]{"La Alhambra", 4.7300, -74.0656},
                        new Object[]{"San José de Bavaria", 4.6950, -74.0722}));

        LOCALIDADES.put("Engativá",
                List.of(
                        new Object[]{"Engativá Pueblo", 4.7075, -74.0967},
                        new Object[]{"Villa Country", 4.6894, -74.0867},
                        new Object[]{"Las Ferias", 4.6928, -74.0969},
                        new Object[]{"El Dorado", 4.7013, -74.1039},
                        new Object[]{"Minuto de Dios", 4.7203, -74.1039}));

        LOCALIDADES.put("Kennedy",
                List.of(
                        new Object[]{"Kennedy Central", 4.6333, -74.1525},
                        new Object[]{"Bosa", 4.6172, -74.1817},
                        new Object[]{"Tintal", 4.6440, -74.1660},
                        new Object[]{"Alamos", 4.6577, -74.1625},
                        new Object[]{"Carvajal", 4.6278, -74.1567}));

        LOCALIDADES.put("Usaquén",
                List.of(
                        new Object[]{"Usaquén Centro", 4.6986, -74.0328},
                        new Object[]{"Santa Bárbara", 4.6970, -74.0450},
                        new Object[]{"Cedritos", 4.7160, -74.0330},
                        new Object[]{"La Carolina", 4.6889, -74.0456},
                        new Object[]{"Santa Ana", 4.7075, -74.0417}));

        LOCALIDADES.put("Fontibón",
                List.of(
                        new Object[]{"Fontibón Pueblo", 4.6719, -74.1442},
                        new Object[]{"Modelia", 4.6728, -74.1197},
                        new Object[]{"El Tintal", 4.6440, -74.1660},
                        new Object[]{"Villemar", 4.6869, -74.1256},
                        new Object[]{"La Capuchina", 4.6781, -74.1425}));

        LOCALIDADES.put("Teusaquillo",
                List.of(
                        new Object[]{"Galerías", 4.6522, -74.0922},
                        new Object[]{"Campín", 4.6511, -74.0864},
                        new Object[]{"Quinta Paredes", 4.6489, -74.0789},
                        new Object[]{"La Soledad", 4.6233, -74.0786},
                        new Object[]{"Palermo", 4.6408, -74.0686}));

        LOCALIDADES.put("Puente Aranda",
                List.of(
                        new Object[]{"Muzú", 4.6289, -74.1239},
                        new Object[]{"San Rafael", 4.6233, -74.1139},
                        new Object[]{"Ciudad Montes", 4.6167, -74.1189},
                        new Object[]{"Veraguas", 4.6331, -74.1169},
                        new Object[]{"Pensilvania", 4.6422, -74.1275}));

        LOCALIDADES.put("Barrios Unidos",
                List.of(
                        new Object[]{"Doce de Octubre", 4.6744, -74.0781},
                        new Object[]{"La Paz", 4.6781, -74.0861},
                        new Object[]{"Alcázares", 4.6817, -74.0792},
                        new Object[]{"Los Andes", 4.6692, -74.0744},
                        new Object[]{"San Fernando", 4.6722, -74.0892}));

        LOCALIDADES.put("Antonio Nariño",
                List.of(
                        new Object[]{"Restrepo", 4.6058, -74.1011},
                        new Object[]{"Ciudad Jardín", 4.6089, -74.1017},
                        new Object[]{"Santander", 4.6189, -74.1097},
                        new Object[]{"San Antonio", 4.5847, -74.1014},
                        new Object[]{"La Fraguita", 4.6111, -74.0967}));

        LOCALIDADES.put("Santa Fe",
                List.of(
                        new Object[]{"La Candelaria", 4.6019, -74.0717},
                        new Object[]{"Las Aguas", 4.6083, -74.0706},
                        new Object[]{"Santa Fe", 4.6050, -74.0792},
                        new Object[]{"Los Mártires", 4.6067, -74.0833},
                        new Object[]{"La Macarena", 4.6125, -74.0756}));

        LOCALIDADES.put("Los Mártires",
                List.of(
                        new Object[]{"La Estanzuela", 4.6058, -74.0989},
                        new Object[]{"La Pepita", 4.6086, -74.0936},
                        new Object[]{"Voto Nacional", 4.6133, -74.0889},
                        new Object[]{"Paloquemao", 4.6186, -74.0947},
                        new Object[]{"La Sabana", 4.6106, -74.0847}));
    }

    public static Map<String, List<Object[]>> obtenerLocalidades() {
        return LOCALIDADES;
    }

    /**
     * Devuelve el catálogo en formato JSON para poblar los selects y el mapa Leaflet.
     */
    public static String obtenerLocalidadesJson() {
        StringBuilder sb = new StringBuilder("[");
        boolean primera = true;
        for (Map.Entry<String, List<Object[]>> e : LOCALIDADES.entrySet()) {
            if (!primera) sb.append(",");
            primera = false;
            sb.append("{\"localidad\":\"").append(escapar(e.getKey())).append("\",\"barrios\":[");
            boolean primeroB = true;
            for (Object[] b : e.getValue()) {
                if (!primeroB) sb.append(",");
                primeroB = false;
                sb.append("{\"nombre\":\"").append(escapar((String) b[0]))
                  .append("\",\"lat\":").append(b[1])
                  .append(",\"lon\":").append(b[2]).append("}");
            }
            sb.append("]}");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
