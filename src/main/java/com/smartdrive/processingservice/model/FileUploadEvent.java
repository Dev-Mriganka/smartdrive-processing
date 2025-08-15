package com.smartdrive.processingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadEvent {
    
    private String contentId;
    private String s3Key;
    private String fileName;
    private String contentType;
    private Long size;
    private String userId;
    private String workspaceId;
    private String bucketName;
    private Instant uploadedAt;
    private String eventId;
    private String eventType; // UPLOADED, PROCESSING, COMPLETED, FAILED
}
