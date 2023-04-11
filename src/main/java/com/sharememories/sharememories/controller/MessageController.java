package com.sharememories.sharememories.controller;

import com.sharememories.sharememories.domain.Message;
import com.sharememories.sharememories.domain.User;
import com.sharememories.sharememories.service.MessageService;
import com.sharememories.sharememories.service.SecurityUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/messages", produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageController {

    private MessageService messageService;
    private SecurityUserDetailsService userDetailsService;

    @Autowired
    public MessageController(MessageService messageService, SecurityUserDetailsService userDetailsService) {
        this.messageService = messageService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMessage(@PathVariable long id) {
        Optional<Message> message = messageService.getMessage(id);
        if (message.isPresent()) {
            return ResponseEntity.ok(message.get());
        } else {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("status", HttpStatus.NOT_FOUND.value());
            map.put("message", "Message not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        }
    }

    @GetMapping("/user/{contactId}")
    public ResponseEntity<?> getAllMessagesWithUser(@PathVariable long contactId) {
        Optional<User> loggedUser = userDetailsService.getUserByUsername(SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName());
        Optional<User> contact = userDetailsService.getUserById(contactId);
        if (!contact.isPresent() && loggedUser.isPresent()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("status", HttpStatus.NOT_FOUND.value());
            map.put("message", "Contact not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        } else {
            List<Message> messages = messageService.getAllMessagesBySenderAndReceiver(loggedUser.get(), contact.get());
            if (messages.isEmpty())
                return ResponseEntity.noContent().build();
            else
                return ResponseEntity.ok(messages);
        }
    }

    //TODO including image to message
    @PostMapping("/user/{contactId}")
    public ResponseEntity<?> sendMessageToUser(@PathVariable long contactId,
                                               @RequestPart String content,
                                               @RequestPart(value = "image", required = false) MultipartFile file) {
        User loggedUser = userDetailsService.getUserByUsername(SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName())
                .get();
        Optional<User> contact = userDetailsService.getUserById(contactId);
        if (!contact.isPresent()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("status", HttpStatus.NOT_FOUND.value());
            map.put("message", "Contact not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        } else {
            Message message = messageService.createMessage(new Message(loggedUser, contact.get(), content));
            return ResponseEntity.ok(message);
        }
    }
}