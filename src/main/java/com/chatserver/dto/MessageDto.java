package com.chatserver.dto;

import com.chatserver.model.Message;
import java.time.LocalDateTime;

public class MessageDto {
    private String senderUsername;
    private String receiverUsername;
    private Long groupId;
    private String content;
    private Message.MessageType messageType;
    private LocalDateTime createdAt;

    public MessageDto() {}

    public MessageDto(String senderUsername, String receiverUsername, String content, Message.MessageType messageType) {
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.content = content;
        this.messageType = messageType;
        this.createdAt = LocalDateTime.now();
    }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Message.MessageType getMessageType() { return messageType; }
    public void setMessageType(Message.MessageType messageType) { this.messageType = messageType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
