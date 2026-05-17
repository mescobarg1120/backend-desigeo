package com.backend_desigeo.desigeo_report_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private String reportId;
    private String userId;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private Double latitude;
    private Double longitude;
    private String address;
    private Integer comunaId;
    private Double aiConfidence;
    private Boolean requiresManualReview;
    private Integer mediaCount;
    private Integer reopenCount;
    private String assignedTo;
    private String channel;
    private String geohash;
    private Date createdAt;
    private Date updatedAt;
}
