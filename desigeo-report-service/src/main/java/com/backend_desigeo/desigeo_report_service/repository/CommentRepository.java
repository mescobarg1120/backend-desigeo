package com.backend_desigeo.desigeo_report_service.repository;

import com.backend_desigeo.desigeo_report_service.model.Comment;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final Firestore firestore;
    private static final String COMMENTS_COL = "Comments";

    public Comment save(Comment comment) throws ExecutionException, InterruptedException {
        if (comment.getCommentId() == null) {
            comment.setCommentId(UUID.randomUUID().toString());
        }
        comment.setCreatedAt(new Date());
        firestore.collection(COMMENTS_COL).document(comment.getCommentId()).set(comment).get();
        return comment;
    }

    public List<Comment> findByReportId(String reportId) throws ExecutionException, InterruptedException {
        return firestore.collection(COMMENTS_COL)
                .whereEqualTo("reportId", reportId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get().get().getDocuments().stream()
                .map(doc -> doc.toObject(Comment.class))
                .collect(Collectors.toList());
    }
}
