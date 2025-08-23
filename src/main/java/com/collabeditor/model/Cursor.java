package com.collabeditor.model;

import java.time.LocalDateTime;

public class Cursor {
    private int line;
    private int column;
    private String sender;
    private LocalDateTime timestamp;
    private String documentId;
    private String color; // For visual distinction between users
    
    public Cursor() {
        this.timestamp = LocalDateTime.now();
    }
    
    public Cursor(int line, int column, String sender) {
        this.line = line;
        this.column = column;
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
        this.color = generateColor(sender);
    }
    
    private String generateColor(String sender) {
        // Generate a consistent color based on the sender's name
        int hash = sender.hashCode();
        return String.format("#%06x", Math.abs(hash) % 0xFFFFFF);
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
