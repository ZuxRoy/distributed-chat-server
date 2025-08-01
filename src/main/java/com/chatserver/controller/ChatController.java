package com.chatserver.controller;

import com.chatserver.dto.MessageDto;
import com.chatserver.model.Message;
import com.chatserver.service.KafkaMessageService;
import com.chatserver.service.MessageService;
import com.chatserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private KafkaMessageService kafkaMessageService;

    @Autowired
    private UserService userService;

    @MessageMapping("/chat.sendMessage")
    public void sendDirectMessage(@Payload MessageDto messageDto) {
        try {
            Message savedMessage = messageService.saveDirectMessage(messageDto);
            
            kafkaMessageService.sendDirectMessageToKafka(messageDto);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.sendGroupMessage")
    public void sendGroupMessage(@Payload MessageDto messageDto) {
        try {
            Message savedMessage = messageService.saveGroupMessage(messageDto);
            
            kafkaMessageService.sendGroupMessageToKafka(messageDto);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload MessageDto messageDto, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", messageDto.getSenderUsername());
        
        userService.updateUserOnlineStatus(messageDto.getSenderUsername(), true);
    }
}
