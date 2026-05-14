package com.backend_desigeo.desigeo_report_service.repository;

import com.backend_desigeo.desigeo_report_service.model.Report;
import com.backend_desigeo.desigeo_report_service.model.ReportHistory;
import com.backend_desigeo.desigeo_report_service.model.ReportMedia;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final Firestore firestore;

    private static final String REPORTS_COL = "Reports";
    private static final String MEDIA_COL    = "ReportMedia";
    private static final String HISTORY_COL  = "ReportHistory";

    public Report save(Report report) throws ExecutionException, InterruptedException {
        if (report.getReportId() == null) {
            report.setReportId(UUID.randomUUID().toString());
        }
        Date now = new Date();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        firestore.collection(REPORTS_COL).document(report.getReportId()).set(toMap(report)).get();
        return report;
    }

    public Optional<Report> findById(String reportId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(REPORTS_COL).document(reportId).get().get();
        if (!doc.exists()) return Optional.empty();
        return Optional.of(fromDocument(doc));
    }

    public Report update(Report report) throws ExecutionException, InterruptedException {
        report.setUpdatedAt(new Date());
        firestore.collection(REPORTS_COL).document(report.getReportId()).set(toMap(report)).get();
        return report;
    }

    public List<Report> findWithFilters(String status, String category, String priority,
                                        String userId, String startDate, String endDate,
                                        int page, int size) throws ExecutionException, InterruptedException {
        Query query = firestore.collection(REPORTS_COL);

        if (status   != null) query = query.whereEqualTo("status",   status);
        if (category != null) query = query.whereEqualTo("category", category);
        if (priority != null) query = query.whereEqualTo("priority", priority);
        if (userId   != null) query = query.whereEqualTo("userId",   userId);

        query = query.orderBy("createdAt", Query.Direction.DESCENDING)
                     .offset(page * size)
                     .limit(size);

        return query.get().get().getDocuments().stream()
                .map(this::fromDocument)
                .collect(Collectors.toList());
    }

    public List<Report> findByUserId(String userId) throws ExecutionException, InterruptedException {
        return firestore.collection(REPORTS_COL)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().get().getDocuments().stream()
                .map(this::fromDocument)
                .collect(Collectors.toList());
    }

    public void saveMedia(ReportMedia media) throws ExecutionException, InterruptedException {
        if (media.getMediaId() == null) media.setMediaId(UUID.randomUUID().toString());
        media.setUploadedAt(new Date());
        firestore.collection(MEDIA_COL).document(media.getMediaId()).set(media).get();
    }

    public List<ReportMedia> findMediaByReportId(String reportId) throws ExecutionException, InterruptedException {
        return firestore.collection(MEDIA_COL)
                .whereEqualTo("reportId", reportId)
                .get().get().getDocuments().stream()
                .map(doc -> doc.toObject(ReportMedia.class))
                .collect(Collectors.toList());
    }

    public void saveHistory(ReportHistory history) throws ExecutionException, InterruptedException {
        if (history.getHistoryId() == null) history.setHistoryId(UUID.randomUUID().toString());
        firestore.collection(HISTORY_COL).document(history.getHistoryId()).set(history).get();
    }

    public List<ReportHistory> findHistoryByReportId(String reportId) throws ExecutionException, InterruptedException {
        return firestore.collection(HISTORY_COL)
                .whereEqualTo("reportId", reportId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get().get().getDocuments().stream()
                .map(doc -> doc.toObject(ReportHistory.class))
                .collect(Collectors.toList());
    }

    private Map<String, Object> toMap(Report r) {
        Map<String, Object> m = new HashMap<>();
        m.put("reportId",             r.getReportId());
        m.put("userId",               r.getUserId());
        m.put("title",                r.getTitle());
        m.put("description",          r.getDescription());
        m.put("category",             r.getCategory());
        m.put("priority",             r.getPriority());
        m.put("status",               r.getStatus());
        m.put("latitude",             r.getLatitude());
        m.put("longitude",            r.getLongitude());
        m.put("address",              r.getAddress());
        m.put("comunaId",             r.getComunaId());
        m.put("aiConfidence",         r.getAiConfidence());
        m.put("requiresManualReview", r.getRequiresManualReview());
        m.put("mediaCount",           r.getMediaCount());
        m.put("reopenCount",          r.getReopenCount());
        m.put("assignedTo",           r.getAssignedTo());
        m.put("channel",              r.getChannel());
        m.put("geohash",              r.getGeohash());
        m.put("createdAt",            r.getCreatedAt());
        m.put("updatedAt",            r.getUpdatedAt());
        return m;
    }

    private Report fromDocument(DocumentSnapshot doc) {
        return Report.builder()
                .reportId(doc.getString("reportId"))
                .userId(doc.getString("userId"))
                .title(doc.getString("title"))
                .description(doc.getString("description"))
                .category(doc.getString("category"))
                .priority(doc.getString("priority"))
                .status(doc.getString("status"))
                .latitude(doc.getDouble("latitude"))
                .longitude(doc.getDouble("longitude"))
                .address(doc.getString("address"))
                .comunaId(doc.getLong("comunaId") != null ? doc.getLong("comunaId").intValue() : null)
                .aiConfidence(doc.getDouble("aiConfidence"))
                .requiresManualReview(doc.getBoolean("requiresManualReview"))
                .mediaCount(doc.getLong("mediaCount") != null ? doc.getLong("mediaCount").intValue() : 0)
                .reopenCount(doc.getLong("reopenCount") != null ? doc.getLong("reopenCount").intValue() : 0)
                .assignedTo(doc.getString("assignedTo"))
                .channel(doc.getString("channel"))
                .geohash(doc.getString("geohash"))
                .createdAt(doc.getDate("createdAt"))
                .updatedAt(doc.getDate("updatedAt"))
                .build();
    }
}
