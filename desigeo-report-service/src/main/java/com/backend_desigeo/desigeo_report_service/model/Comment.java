package com.backend_desigeo.desigeo_report_service.model;

import lombok.*;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private String commentId;
    private String reportId;
    private String userId;
    private String content;
    private String userName;
    private Boolean isInternal;
    private Date createdAt;
}
