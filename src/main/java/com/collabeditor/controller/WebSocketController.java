package com.collabeditor.controller;

import com.collabeditor.model.CodeUpdate;
import com.collabeditor.model.Cursor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    private final Map<String, String> activeDocuments = new ConcurrentHashMap<>();
    private final Map<String, String> documentLanguages = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/code")
    public void handleCodeUpdate(CodeUpdate update) {
        logger.info("Received code update from {} for document {}: {}", 
            update.getSender(), 
            update.getDocumentId(),
            update.getContent().substring(0, Math.min(50, update.getContent().length())) + "...");
        
        if (update.getDocumentId() != null) {
            activeDocuments.put(update.getDocumentId(), update.getContent());
            
            // Update language if provided
            if (update.getLanguage() != null) {
                String oldLanguage = documentLanguages.get(update.getDocumentId());
                documentLanguages.put(update.getDocumentId(), update.getLanguage());
                logger.info("Updated language for document {} from {} to {}", 
                    update.getDocumentId(), 
                    oldLanguage != null ? oldLanguage : "none",
                    update.getLanguage());
            }
            
            // Broadcast to all clients
            logger.info("Broadcasting update to /topic/code");
            messagingTemplate.convertAndSend("/topic/code", update);
            
            // Also send to document-specific topic
            logger.info("Broadcasting update to /topic/document/{}", update.getDocumentId());
            messagingTemplate.convertAndSend("/topic/document/" + update.getDocumentId(), update);
        }
    }

    @MessageMapping("/cursor")
    public void handleCursorUpdate(Cursor cursor) {
        logger.info("Received cursor update from {} for document {}: line {}, column {}", 
            cursor.getSender(), 
            cursor.getDocumentId(),
            cursor.getLine(),
            cursor.getColumn());
            
        // Broadcast to all clients
        messagingTemplate.convertAndSend("/topic/cursor", cursor);
    }

    @SubscribeMapping("/document/{documentId}")
    public Map<String, Object> getDocumentContent(String documentId) {
        logger.info("Document content requested for {}", documentId);
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("content", activeDocuments.getOrDefault(documentId, ""));
        response.put("language", documentLanguages.getOrDefault(documentId, "javascript"));
        logger.info("Returning content length: {} and language: {}", 
            response.get("content").toString().length(),
            response.get("language"));
        return response;
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        logger.error("WebSocket error occurred", exception);
        return "Error: " + exception.getMessage();
    }
}
