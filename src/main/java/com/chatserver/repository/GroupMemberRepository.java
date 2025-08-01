package com.chatserver.repository;

import com.chatserver.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByUsername(String username);
    boolean existsByGroupIdAndUsername(Long groupId, String username);
}
