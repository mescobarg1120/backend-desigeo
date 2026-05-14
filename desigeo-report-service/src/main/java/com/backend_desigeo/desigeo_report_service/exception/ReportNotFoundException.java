package com.backend_desigeo.desigeo_report_service.exception;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(String reportId) {
        super("Reporte no encontrado: " + reportId);
    }
}
