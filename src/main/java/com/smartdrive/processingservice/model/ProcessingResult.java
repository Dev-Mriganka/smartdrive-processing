package com.smartdrive.processingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResult {
    
    private String contentId;
    private String processingStatus; // PENDING, PROCESSING, COMPLETED, FAILED
    private Instant processedAt;
    private String processingError;
    
    // AI Generated Metadata
    private String extractedText;
    private List<String> detectedLanguage;
    private List<String> tags;
    private List<String> categories;
    private String summary;
    private List<String> keywords;
    private List<String> entities;
    private String documentType;
    
    // Image Analysis Results
    private List<String> imageLabels;
    private List<String> imageObjects;
    private List<String> imageColors;
    private List<String> imageFaces;
    private List<String> imageText;
    private List<Float> imageEmbedding;
    
    // Custom metadata
    private Map<String, Object> customMetadata;
    
    // Processing metadata
    private Long processingTimeMs;
    private String aiProvider; // gemini, openai
    private String processingVersion;
}
