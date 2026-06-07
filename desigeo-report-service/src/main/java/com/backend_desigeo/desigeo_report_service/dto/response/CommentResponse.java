package com.backend_desigeo.desigeo_report_service.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String commentId;
    private String reportId;
    private String userId;
    private String userName;
    private String content;
    private String createdAt;
}
