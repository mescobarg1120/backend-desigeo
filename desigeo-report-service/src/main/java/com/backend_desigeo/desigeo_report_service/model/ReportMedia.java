package com.backend_desigeo.desigeo_report_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMedia {
    private String mediaId;
    private String reportId;
    private String s3Url;
    private String s3Key;
    private String fileType;
    private Long fileSize;
    private Long compressedSize;
    private Boolean rekognitionProcessed;
    private List<String> rekognitionLabels;
    private String uploadedBy;
    private Date uploadedAt;
}
