package com.chatserver.service;

import com.chatserver.model.Group;
import com.chatserver.model.GroupMember;
import com.chatserver.repository.GroupRepository;
import com.chatserver.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserService userService;

    public Group createGroup(String name, String createdBy) throws Exception {
        if (!userService.userExists(createdBy)) {
            throw new Exception("User does not exist");
        }

        Group group = new Group(name, createdBy);
        Group savedGroup = groupRepository.save(group);

        // Add creator as first member
        addUserToGroup(savedGroup.getId(), createdBy);
        
        return savedGroup;
    }

    public void addUserToGroup(Long groupId, String username) throws Exception {
        if (!userService.userExists(username)) {
            throw new Exception("User does not exist");
        }

        if (!groupRepository.existsById(groupId)) {
            throw new Exception("Group does not exist");
        }

        if (groupMemberRepository.existsByGroupIdAndUsername(groupId, username)) {
            throw new Exception("User is already a member of this group");
        }

        GroupMember member = new GroupMember(groupId, username);
        groupMemberRepository.save(member);
    }

    public List<GroupMember> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    public Optional<Group> findById(Long groupId) {
        return groupRepository.findById(groupId);
    }

    public boolean isUserInGroup(Long groupId, String username) {
        return groupMemberRepository.existsByGroupIdAndUsername(groupId, username);
    }
}
