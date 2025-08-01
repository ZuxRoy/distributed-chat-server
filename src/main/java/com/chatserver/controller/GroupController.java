package com.chatserver.controller;

import com.chatserver.dto.GroupDto;
import com.chatserver.dto.UserDto;
import com.chatserver.model.Group;
import com.chatserver.model.GroupMember;
import com.chatserver.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody GroupDto groupDto) {
        try {
            Group group = groupService.createGroup(groupDto.getName(), groupDto.getCreatedBy());
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addUserToGroup(@PathVariable Long groupId, @RequestBody UserDto userDto) {
        try {
            groupService.addUserToGroup(groupId, userDto.getUsername());
            return ResponseEntity.ok("User added to group successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMember> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }
}
