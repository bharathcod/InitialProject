package com.example.collab.controller;

import com.example.collab.dto.ChangeEvent;
import com.example.collab.service.CollaborationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ModelWebSocketController {

    private final CollaborationService collaborationService;

    public ModelWebSocketController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    @MessageMapping("/update-model") // clients send to /app/update-model
    public void handleModelUpdate(ChangeEvent event) {
        // Optionally validate event fields
        collaborationService.processChange(event);
        // We don't directly send to clients here — publisher -> Redis -> subscriber -> broadcast
    }
}
