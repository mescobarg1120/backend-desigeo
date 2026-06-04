package com.backend_desigeo.desigeo_analytics_service.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final FirebaseApp firebaseApp;

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    // ── Dashboard global (SUPER_ADMIN) ──────────────────────────────
    public Map<String, Object> getGlobalDashboard() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = getFirestore()
                .collection("Reports")
                .get().get()
                .getDocuments();

        Map<String, Object> result = new HashMap<>();
        result.put("totalReportes", docs.size());

        Map<String, Long> porEstado = docs.stream()
                .collect(Collectors.groupingBy(
                        d -> Optional.ofNullable(d.getString("status")).orElse("UNKNOWN"),
                        Collectors.counting()
                ));
        result.put("porEstado", porEstado);

        Map<String, Long> porCategoria = docs.stream()
                .collect(Collectors.groupingBy(
                        d -> Optional.ofNullable(d.getString("category")).orElse("OTRO"),
                        Collectors.counting()
                ));
        result.put("porCategoria", porCategoria);

        Map<String, Long> porPrioridad = docs.stream()
                .collect(Collectors.groupingBy(
                        d -> Optional.ofNullable(d.getString("priority")).orElse("MEDIUM"),
                        Collectors.counting()
                ));
        result.put("porPrioridad", porPrioridad);

        Map<String, Long> porComuna = docs.stream()
                .filter(d -> d.get("comunaId") != null)
                .collect(Collectors.groupingBy(
                        d -> String.valueOf(d.get("comunaId")),
                        Collectors.counting()
                ));
        result.put("porComuna", porComuna);

        result.put("timeline", buildTimeline(docs));

        long resueltos = docs.stream()
                .filter(d -> "RESOLVED".equals(d.getString("status")))
                .count();
        double tasaResolucion = docs.isEmpty() ? 0 :
                Math.round((resueltos * 100.0 / docs.size()) * 10.0) / 10.0;
        result.put("tasaResolucion", tasaResolucion);

        return result;
    }

    // ── Dashboard por comuna (ADMIN_MUNICIPAL) ──────────────────────
    public Map<String, Object> getDashboardByComuna(Integer comunaId)
            throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> docs = getFirestore()
                .collection("Reports")
                .whereEqualTo("comunaId", comunaId)
                .get().get()
                .getDocuments();

        Map<String, Object> result = new HashMap<>();
        result.put("totalReportes", docs.size());

        Map<String, Long> porEstado = docs.stream()
                .collect(Collectors.groupingBy(
                        d -> Optional.ofNullable(d.getString("status")).orElse("UNKNOWN"),
                        Collectors.counting()
                ));
        result.put("porEstado", porEstado);

        Map<String, Long> porCategoria = docs.stream()
                .collect(Collectors.groupingBy(
                        d -> Optional.ofNullable(d.getString("category")).orElse("OTRO"),
                        Collectors.counting()
                ));
        result.put("porCategoria", porCategoria);

        result.put("timeline", buildTimeline(docs));

        long resueltos = docs.stream()
                .filter(d -> "RESOLVED".equals(d.getString("status")))
                .count();
        double tasaResolucion = docs.isEmpty() ? 0 :
                Math.round((resueltos * 100.0 / docs.size()) * 10.0) / 10.0;
        result.put("tasaResolucion", tasaResolucion);

        return result;
    }

    // ── Lista de reportes con filtros ───────────────────────────────
    public Map<String, Object> getReportes(Integer comunaId, String status,
                                            String category, String priority,
                                            int page, int size)
            throws ExecutionException, InterruptedException {

        Query query = getFirestore().collection("Reports");

        // Filtros opcionales
        if (comunaId != null) {
            query = query.whereEqualTo("comunaId", comunaId);
        }
        if (status != null && !status.isBlank()) {
            query = query.whereEqualTo("status", status);
        }
        if (category != null && !category.isBlank()) {
            query = query.whereEqualTo("category", category);
        }
        if (priority != null && !priority.isBlank()) {
            query = query.whereEqualTo("priority", priority);
        }

        List<QueryDocumentSnapshot> allDocs = query.get().get().getDocuments();

        // Paginación manual
        int total = allDocs.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex   = Math.min(fromIndex + size, total);
        List<QueryDocumentSnapshot> pageDocs = allDocs.subList(fromIndex, toIndex);

        List<Map<String, Object>> reportes = pageDocs.stream()
                .map(this::docToReporte)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("reportes", reportes);
        result.put("total", total);
        result.put("page", page);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    // ── Categorías disponibles ──────────────────────────────────────
    public List<String> getCategorias() throws ExecutionException, InterruptedException {
        return getFirestore().collection("Reports")
                .get().get()
                .getDocuments()
                .stream()
                .map(d -> d.getString("category"))
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ── Helpers ─────────────────────────────────────────────────────
    private Map<String, Object> docToReporte(QueryDocumentSnapshot doc) {
        Map<String, Object> r = new HashMap<>();
        r.put("reportId",    doc.getString("reportId"));
        r.put("description", doc.getString("description"));
        r.put("category",    doc.getString("category"));
        r.put("priority",    doc.getString("priority"));
        r.put("status",      doc.getString("status"));
        r.put("address",     doc.getString("address"));
        r.put("comunaId",    doc.get("comunaId"));
        r.put("userId",      doc.getString("userId"));
        r.put("latitude",    doc.getDouble("latitude"));
        r.put("longitude",   doc.getDouble("longitude"));
        r.put("createdAt",   doc.getDate("createdAt") != null
                ? doc.getDate("createdAt").toInstant().toString() : null);
        r.put("updatedAt",   doc.getDate("updatedAt") != null
                ? doc.getDate("updatedAt").toInstant().toString() : null);
        return r;
    }

    private List<Map<String, Object>> buildTimeline(List<QueryDocumentSnapshot> docs) {
        Map<String, Long> ingresadosPorMes = new LinkedHashMap<>();
        Map<String, Long> resueltosPorMes  = new LinkedHashMap<>();

        Calendar cal = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -i);
            String key = String.format("%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
            ingresadosPorMes.put(key, 0L);
            resueltosPorMes.put(key, 0L);
        }

        for (QueryDocumentSnapshot doc : docs) {
            Date createdAt = doc.getDate("createdAt");
            if (createdAt != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(createdAt);
                String key = String.format("%d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
                if (ingresadosPorMes.containsKey(key)) {
                    ingresadosPorMes.merge(key, 1L, Long::sum);
                }
            }
            if ("RESOLVED".equals(doc.getString("status"))) {
                Date updatedAt = doc.getDate("updatedAt");
                if (updatedAt != null) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(updatedAt);
                    String key = String.format("%d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
                    if (resueltosPorMes.containsKey(key)) {
                        resueltosPorMes.merge(key, 1L, Long::sum);
                    }
                }
            }
        }

        List<Map<String, Object>> timeline = new ArrayList<>();
        for (String mes : ingresadosPorMes.keySet()) {
            Map<String, Object> punto = new HashMap<>();
            punto.put("mes", mes);
            punto.put("ingresados", ingresadosPorMes.get(mes));
            punto.put("resueltos", resueltosPorMes.getOrDefault(mes, 0L));
            timeline.add(punto);
        }
        return timeline;
    }
}