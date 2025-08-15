package com.smartdrive.processingservice.service;

import com.smartdrive.processingservice.model.FileUploadEvent;
import com.smartdrive.processingservice.model.ProcessingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingService {

    private final RestTemplate restTemplate;

    @Value("${app.ai-service.url:http://localhost:8082}")
    private String aiServiceUrl;

    @Value("${app.metadata-service.url:http://localhost:8085}")
    private String metadataServiceUrl;

    @CircuitBreaker(name = "ai-service", fallbackMethod = "processFileFallback")
    public void processFile(FileUploadEvent event) {
        log.info("Processing file: {}", event.getContentId());
        
        try {
            // Step 1: Call AI service for metadata generation
            Map<String, Object> aiRequest = Map.of(
                    "contentId", event.getContentId(),
                    "s3Key", event.getS3Key(),
                    "fileName", event.getFileName(),
                    "contentType", event.getContentType(),
                    "size", event.getSize()
            );
            
            Map<String, Object> aiResponse = restTemplate.postForObject(
                    aiServiceUrl + "/api/v1/metadata/generate",
                    aiRequest,
                    Map.class
            );
            
            if (aiResponse != null) {
                // Step 2: Update metadata in metadata service
                updateMetadataInDynamoDB(event.getContentId(), aiResponse);
                
                // Step 3: Update processing status
                updateProcessingStatus(event.getContentId(), "COMPLETED", null);
                
                log.info("File processing completed successfully: {}", event.getContentId());
            } else {
                throw new RuntimeException("AI service returned null response");
            }
            
        } catch (Exception e) {
            log.error("Error processing file: {}", event.getContentId(), e);
            updateProcessingStatus(event.getContentId(), "FAILED", e.getMessage());
            throw e;
        }
    }

    public void processFileFallback(FileUploadEvent event, Exception e) {
        log.warn("Processing fallback triggered for file: {}", event.getContentId(), e);
        
        try {
            // Create basic metadata without AI enhancement
            Map<String, Object> basicMetadata = Map.of(
                    "contentId", event.getContentId(),
                    "processingStatus", "COMPLETED",
                    "processedAt", Instant.now(),
                    "processingError", "AI service unavailable, using basic metadata",
                    "tags", java.util.List.of("basic"),
                    "categories", java.util.List.of("general"),
                    "summary", "Basic file metadata"
            );
            
            // Update with basic metadata
            updateMetadataInDynamoDB(event.getContentId(), basicMetadata);
            updateProcessingStatus(event.getContentId(), "COMPLETED", "AI service unavailable");
            
            log.info("Basic metadata processing completed for file: {}", event.getContentId());
            
        } catch (Exception fallbackError) {
            log.error("Fallback processing also failed for file: {}", event.getContentId(), fallbackError);
            updateProcessingStatus(event.getContentId(), "FAILED", fallbackError.getMessage());
        }
    }

    private void updateMetadataInDynamoDB(String contentId, Map<String, Object> metadata) {
        log.info("Updating metadata in DynamoDB for contentId: {}", contentId);
        
        try {
            restTemplate.put(
                    metadataServiceUrl + "/api/v1/metadata/files/" + contentId,
                    metadata
            );
            
            log.info("Metadata updated successfully for contentId: {}", contentId);
            
        } catch (Exception e) {
            log.error("Error updating metadata for contentId: {}", contentId, e);
            throw new RuntimeException("Failed to update metadata", e);
        }
    }

    private void updateProcessingStatus(String contentId, String status, String error) {
        log.info("Updating processing status for contentId: {} to {}", contentId, status);
        
        try {
            Map<String, Object> statusUpdate = Map.of(
                    "contentId", contentId,
                    "processingStatus", status,
                    "processedAt", Instant.now(),
                    "processingError", error != null ? error : ""
            );
            
            restTemplate.patchForObject(
                    metadataServiceUrl + "/api/v1/metadata/files/" + contentId + "/status",
                    statusUpdate,
                    Map.class
            );
            
            log.info("Processing status updated successfully for contentId: {}", contentId);
            
        } catch (Exception e) {
            log.error("Error updating processing status for contentId: {}", contentId, e);
            // Don't throw here as this is not critical
        }
    }
}
