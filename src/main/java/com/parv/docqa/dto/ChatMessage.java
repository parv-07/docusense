package com.parv.docqa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ChatMessage {

    private String role;    // "user" or "model"

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String content;

    public ChatMessage(String role) {
        this.role = role;
    }

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
