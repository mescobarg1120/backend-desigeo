package com.backend_desigeo.desigeo_report_service.service;

import com.backend_desigeo.desigeo_report_service.client.NotificationClient;
import com.backend_desigeo.desigeo_report_service.dto.request.*;
import com.backend_desigeo.desigeo_report_service.dto.response.*;
import com.backend_desigeo.desigeo_report_service.enums.ReportPriority;
import com.backend_desigeo.desigeo_report_service.enums.ReportStatus;
import com.backend_desigeo.desigeo_report_service.exception.ReportNotFoundException;
import com.backend_desigeo.desigeo_report_service.model.Comment;
import com.backend_desigeo.desigeo_report_service.model.Report;
import com.backend_desigeo.desigeo_report_service.model.ReportHistory;
import com.backend_desigeo.desigeo_report_service.model.ReportMedia;
import com.backend_desigeo.desigeo_report_service.repository.CommentRepository;
import com.backend_desigeo.desigeo_report_service.repository.ComunaRepository;
import com.backend_desigeo.desigeo_report_service.repository.ReportRepository;
import com.backend_desigeo.desigeo_report_service.util.GeohashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final CommentRepository commentRepository;
    private final ComunaRepository comunaRepository;
    private final ImageService imageService;
    private final NotificationClient notificationClient;

    public CreateReportResponse createReport(CreateReportRequest request, String userId) {
        try {
            Integer comunaId = null;
            if (request.getComunaNombre() != null && !request.getComunaNombre().isBlank()) {
                comunaId = comunaRepository.findByNombreIgnoreCase(request.getComunaNombre())
                        .map(c -> c.getComunaId())
                        .orElse(null);
                if (comunaId == null) {
                    log.warn("No se encontró la comuna '{}' en la BD", request.getComunaNombre());
                }
            }

            Report report = Report.builder()
                    .userId(userId)
                    .description(request.getDescription())
                    .category(request.getCategory())
                    .priority(ReportPriority.MEDIUM.name())
                    .status(ReportStatus.PENDING.name())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .address(request.getAddress())
                    .comunaId(comunaId)
                    .geohash(GeohashUtil.encode(request.getLatitude(), request.getLongitude()))
                    .mediaCount(0)
                    .reopenCount(0)
                    .requiresManualReview(false)
                    .aiConfidence(0.0)
                    .channel("app")
                    .build();

            Report saved = reportRepository.save(report);

            if (request.getImages() != null && !request.getImages().isEmpty()) {
                int count = 0;
                for (String base64Image : request.getImages()) {
                    try {
                        String url = imageService.uploadBase64Image(base64Image, saved.getReportId());
                        ReportMedia media = ReportMedia.builder()
                                .reportId(saved.getReportId())
                                .s3Url(url)
                                .s3Key("reports/" + saved.getReportId())
                                .fileType("image/jpeg")
                                .rekognitionProcessed(false)
                                .rekognitionLabels(new ArrayList<>())
                                .uploadedBy(userId)
                                .build();
                        reportRepository.saveMedia(media);
                        count++;
                    } catch (Exception e) {
                        log.error("Error subiendo imagen para reporte {}: {}", saved.getReportId(), e.getMessage());
                    }
                }
                saved.setMediaCount(count);
                reportRepository.update(saved);
            }

            saveHistory(saved.getReportId(), null, ReportStatus.PENDING.name(),
                    "Reporte creado", userId, "CITIZEN", null);

            notificationClient.notifyReportCreated(
                    saved.getReportId(), userId, saved.getCategory(), saved.getAddress());

            return CreateReportResponse.builder()
                    .reportId(saved.getReportId())
                    .status(saved.getStatus())
                    .createdAt(saved.getCreatedAt().toInstant().toString())
                    .build();

        } catch (Exception e) {
            log.error("Error creando reporte: {}", e.getMessage());
            throw new RuntimeException("Error creando reporte: " + e.getMessage());
        }
    }

    public ReportDetailResponse getReport(String reportId) {
        try {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportNotFoundException(reportId));

            List<String> imageUrls = reportRepository.findMediaByReportId(reportId).stream()
                    .map(ReportMedia::getS3Url)
                    .collect(Collectors.toList());

            List<ReportDetailResponse.HistoryItem> historyItems = reportRepository.findHistoryByReportId(reportId).stream()
                    .map(h -> ReportDetailResponse.HistoryItem.builder()
                            .previousStatus(h.getPreviousStatus())
                            .newStatus(h.getNewStatus())
                            .comment(h.getComment())
                            .changedBy(h.getChangedBy())
                            .changedByRole(h.getChangedByRole())
                            .timestamp(h.getTimestamp())
                            .reopenReason(h.getReopenReason())
                            .build())
                    .collect(Collectors.toList());

            return ReportDetailResponse.builder()
                    .reportId(report.getReportId())
                    .userId(report.getUserId())
                    .description(report.getDescription())
                    .category(report.getCategory())
                    .priority(report.getPriority())
                    .status(report.getStatus())
                    .latitude(report.getLatitude())
                    .longitude(report.getLongitude())
                    .address(report.getAddress())
                    .comunaId(report.getComunaId())
                    .images(imageUrls)
                    .createdAt(report.getCreatedAt() != null ? report.getCreatedAt().toInstant().toString() : null)
                    .updatedAt(report.getUpdatedAt() != null ? report.getUpdatedAt().toInstant().toString() : null)
                    .history(historyItems)
                    .build();

        } catch (ReportNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error obteniendo reporte {}: {}", reportId, e.getMessage());
            throw new RuntimeException("Error obteniendo reporte: " + e.getMessage());
        }
    }

    public ReportListResponse getReports(String status, String category, String priority,
                                         String userId, String startDate, String endDate,
                                         Double lat, Double lng, Double radius,
                                         int page, int size) {
        try {
            List<Report> reports = reportRepository.findWithFilters(
                    status, category, priority, userId, startDate, endDate, page, size);

            List<ReportSummaryResponse> summaries = reports.stream()
                    .map(this::toSummary)
                    .collect(Collectors.toList());

            return ReportListResponse.builder()
                    .reports(summaries)
                    .total(summaries.size())
                    .page(page)
                    .build();

        } catch (Exception e) {
            log.error("Error listando reportes: {}", e.getMessage());
            throw new RuntimeException("Error listando reportes: " + e.getMessage());
        }
    }

    public ReportDetailResponse updatePriority(String reportId, UpdatePriorityRequest request) {
        try {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportNotFoundException(reportId));

            report.setPriority(request.getPriority().name());
            reportRepository.update(report);

            return getReport(reportId);

        } catch (ReportNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error actualizando prioridad del reporte {}: {}", reportId, e.getMessage());
            throw new RuntimeException("Error actualizando prioridad: " + e.getMessage());
        }
    }

    public ReportDetailResponse updateStatus(String reportId, UpdateStatusRequest request,
                                             String userId, String userRole) {
        try {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportNotFoundException(reportId));

            String previousStatus = report.getStatus();
            report.setStatus(request.getStatus().name());
            reportRepository.update(report);

            saveHistory(reportId, previousStatus, request.getStatus().name(),
                    request.getComment(), userId, userRole, null);

            notificationClient.notifyStatusChanged(
                    reportId, report.getUserId(), previousStatus, request.getStatus().name(),
                    report.getCategory(), report.getAddress());

            return getReport(reportId);

        } catch (ReportNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error actualizando estado del reporte {}: {}", reportId, e.getMessage());
            throw new RuntimeException("Error actualizando estado: " + e.getMessage());
        }
    }

    public ReportDetailResponse reopenReport(String reportId, ReopenReportRequest request,
                                             String userId, String userRole) {
        try {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportNotFoundException(reportId));

            String previousStatus = report.getStatus();
            report.setStatus(ReportStatus.REOPEN_REQUESTED.name());
            report.setReopenCount(report.getReopenCount() != null ? report.getReopenCount() + 1 : 1);
            reportRepository.update(report);

            saveHistory(reportId, previousStatus, ReportStatus.REOPENED.name(),
                    request.getReason(), userId, userRole, request.getReason());

            notificationClient.notifyReopenRequested(
                    reportId, report.getUserId(), request.getReason());

            return getReport(reportId);

        } catch (ReportNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error reabriendo reporte {}: {}", reportId, e.getMessage());
            throw new RuntimeException("Error reabriendo reporte: " + e.getMessage());
        }
    }

    public ReportListResponse getReportsByUser(String targetUserId) {
        try {
            List<Report> reports = reportRepository.findByUserId(targetUserId);
            List<ReportSummaryResponse> summaries = reports.stream()
                    .map(this::toSummary)
                    .collect(Collectors.toList());

            return ReportListResponse.builder()
                    .reports(summaries)
                    .total(summaries.size())
                    .page(0)
                    .build();

        } catch (Exception e) {
            log.error("Error obteniendo reportes del usuario {}: {}", targetUserId, e.getMessage());
            throw new RuntimeException("Error obteniendo reportes: " + e.getMessage());
        }
    }

    public List<CommentResponse> getComments(String reportId) {
        try {
            return commentRepository.findByReportId(reportId).stream()
                    .map(c -> CommentResponse.builder()
                            .commentId(c.getCommentId())
                            .reportId(c.getReportId())
                            .userId(c.getUserId())
                            .userName(c.getUserName())
                            .content(c.getContent())
                            .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().toInstant().toString() : null)
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo comentarios del reporte {}: {}", reportId, e.getMessage());
            throw new RuntimeException("Error obteniendo comentarios: " + e.getMessage());
        }
    }

    public CommentResponse addComment(String reportId, AddCommentRequest request, String userId) {
        try {
            reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportNotFoundException(reportId));

            Comment comment = Comment.builder()
                    .reportId(reportId)
                    .userId(userId)
                    .content(request.getContent())
                    .userName(request.getUserName())
                    .isInternal(false)
                    .build();

            Comment saved = commentRepository.save(comment);

            return CommentResponse.builder()
                    .commentId(saved.getCommentId())
                    .reportId(saved.getReportId())
                    .userId(saved.getUserId())
                    .userName(saved.getUserName())
                    .content(saved.getContent())
                    .createdAt(saved.getCreatedAt() != null ? saved.getCreatedAt().toInstant().toString() : null)
                    .build();

        } catch (ReportNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error agregando comentario al reporte {}: {}", reportId, e.getMessage());
            throw new RuntimeException("Error agregando comentario: " + e.getMessage());
        }
    }

    private void saveHistory(String reportId, String previousStatus, String newStatus,
                             String comment, String changedBy, String changedByRole, String reopenReason) {
        try {
            reportRepository.saveHistory(ReportHistory.builder()
                    .reportId(reportId)
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .comment(comment)
                    .changedBy(changedBy)
                    .changedByRole(changedByRole)
                    .reopenReason(reopenReason)
                    .timestamp(Instant.now().toString())
                    .build());
        } catch (Exception e) {
            log.error("Error guardando historial del reporte {}: {}", reportId, e.getMessage());
        }
    }

    private ReportSummaryResponse toSummary(Report report) {
        return ReportSummaryResponse.builder()
                .reportId(report.getReportId())
                .description(report.getDescription())
                .category(report.getCategory())
                .priority(report.getPriority())
                .status(report.getStatus())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .address(report.getAddress())
                .createdAt(report.getCreatedAt() != null ? report.getCreatedAt().toInstant().toString() : null)
                .updatedAt(report.getUpdatedAt() != null ? report.getUpdatedAt().toInstant().toString() : null)
                .build();
    }
}
