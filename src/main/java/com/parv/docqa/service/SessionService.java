package com.parv.docqa.service;



import com.parv.docqa.dto.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
@Service
public class SessionService {
private final HashMap<String, List<ChatMessage>> session= new HashMap<>();
    public String createSession(){
     String sessionId = UUID.randomUUID().toString();
     session.put(sessionId,new ArrayList<>());
     return sessionId;
    }

    public List<ChatMessage> getChatHistory(String sessionId){
       return session.getOrDefault(sessionId,new ArrayList<>());
    }
    public void removeSession(String sessionId){
     session.remove(sessionId);
    }
    public void addMessage(String sessionId,ChatMessage chatMessage){
        session.computeIfAbsent(sessionId,k->new ArrayList<>()).add(chatMessage);
    }
}
