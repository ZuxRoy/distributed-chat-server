package com.chatserver.service;

import com.chatserver.dto.MessageDto;
import com.chatserver.model.GroupMember;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KafkaMessageService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private GroupService groupService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendDirectMessageToKafka(MessageDto messageDto) {
        try {
            String messageJson = objectMapper.writeValueAsString(messageDto);
            kafkaTemplate.send("chat-messages", messageDto.getReceiverUsername(), messageJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void sendGroupMessageToKafka(MessageDto messageDto) {
        try {
            String messageJson = objectMapper.writeValueAsString(messageDto);
            kafkaTemplate.send("group-messages", messageDto.getGroupId().toString(), messageJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "chat-messages", groupId = "chat-server-group")
    public void handleDirectMessage(String messageJson) {
        try {
            MessageDto messageDto = objectMapper.readValue(messageJson, MessageDto.class);
            
            simpMessagingTemplate.convertAndSendToUser(
                messageDto.getReceiverUsername(),
                "/queue/messages",
                messageDto
            );
            
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "group-messages", groupId = "chat-server-group")
    public void handleGroupMessage(String messageJson) {
        try {
            MessageDto messageDto = objectMapper.readValue(messageJson, MessageDto.class);
            
            List<GroupMember> members = groupService.getGroupMembers(messageDto.getGroupId());
            
            for (GroupMember member : members) {
                simpMessagingTemplate.convertAndSend(
                    "/topic/group/" + messageDto.getGroupId(),
                    messageDto
                );
            }
            
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
