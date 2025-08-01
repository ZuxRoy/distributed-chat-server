package com.chatserver.service;

import com.chatserver.dto.MessageDto;
import com.chatserver.model.Message;
import com.chatserver.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    public Message saveDirectMessage(MessageDto messageDto) throws Exception {
        if (!userService.userExists(messageDto.getSenderUsername())) {
            throw new Exception("Sender does not exist");
        }
        
        if (!userService.userExists(messageDto.getReceiverUsername())) {
            throw new Exception("Receiver does not exist");
        }

        Message message = new Message(
            messageDto.getSenderUsername(),
            messageDto.getReceiverUsername(),
            messageDto.getContent(),
            Message.MessageType.DIRECT
        );

        return messageRepository.save(message);
    }

    public Message saveGroupMessage(MessageDto messageDto) throws Exception {
        if (!userService.userExists(messageDto.getSenderUsername())) {
            throw new Exception("Sender does not exist");
        }

        if (!groupService.findById(messageDto.getGroupId()).isPresent()) {
            throw new Exception("Group does not exist");
        }

        if (!groupService.isUserInGroup(messageDto.getGroupId(), messageDto.getSenderUsername())) {
            throw new Exception("User is not a member of this group");
        }

        Message message = new Message();
        message.setSenderUsername(messageDto.getSenderUsername());
        message.setGroupId(messageDto.getGroupId());
        message.setContent(messageDto.getContent());
        message.setMessageType(Message.MessageType.GROUP);

        return messageRepository.save(message);
    }

    public List<Message> getDirectMessages(String user1, String user2) {
        return messageRepository.findDirectMessagesBetweenUsers(user1, user2);
    }

    public List<Message> getGroupMessages(Long groupId) {
        return messageRepository.findByGroupIdOrderByCreatedAtAsc(groupId);
    }
}
