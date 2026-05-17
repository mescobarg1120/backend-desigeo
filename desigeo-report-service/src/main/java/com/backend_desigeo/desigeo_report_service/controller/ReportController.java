package com.backend_desigeo.desigeo_report_service.controller;

import com.backend_desigeo.desigeo_report_service.dto.request.*;
import com.backend_desigeo.desigeo_report_service.dto.response.*;
import com.backend_desigeo.desigeo_report_service.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<CreateReportResponse> createReport(
            @Valid @RequestBody CreateReportRequest request,
            Authentication auth) {
        String userId = (String) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createReport(request, userId));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportDetailResponse> getReport(@PathVariable String reportId) {
        return ResponseEntity.ok(reportService.getReport(reportId));
    }

    @GetMapping
    public ResponseEntity<ReportListResponse> getReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radius,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.getReports(
                status, category, priority, userId, startDate, endDate, lat, lng, radius, page, size));
    }

    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ReportDetailResponse> updateStatus(
            @PathVariable String reportId,
            @Valid @RequestBody UpdateStatusRequest request,
            Authentication auth) {
        String userId = (String) auth.getPrincipal();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");
        return ResponseEntity.ok(reportService.updateStatus(reportId, request, userId, role));
    }

    @PostMapping("/{reportId}/reopen")
    public ResponseEntity<ReportDetailResponse> reopenReport(
            @PathVariable String reportId,
            @Valid @RequestBody ReopenReportRequest request,
            Authentication auth) {
        String userId = (String) auth.getPrincipal();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");
        return ResponseEntity.ok(reportService.reopenReport(reportId, request, userId, role));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ReportListResponse> getReportsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(reportService.getReportsByUser(userId));
    }
}
