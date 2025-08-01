package com.chatserver.repository;

import com.chatserver.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.senderUsername = :user1 AND m.receiverUsername = :user2) OR " +
           "(m.senderUsername = :user2 AND m.receiverUsername = :user1) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findDirectMessagesBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);
    
    List<Message> findByGroupIdOrderByCreatedAtAsc(Long groupId);
}
