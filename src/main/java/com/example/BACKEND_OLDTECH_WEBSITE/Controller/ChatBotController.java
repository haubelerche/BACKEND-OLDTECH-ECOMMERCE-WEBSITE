package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
public class ChatBotController {

    // Ask something to the chatbot
    @PostMapping("/ask")
    public ResponseEntity<String> askSomething(@RequestBody String question) {
        // notice: Integrate with a real chatbot service or AI model. i'll do this laterrr
        String answer = "abcd " + question;
        return ResponseEntity.ok(answer);
    }
}