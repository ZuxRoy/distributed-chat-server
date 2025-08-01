package com.chatserver.dto;

public class GroupDto {
    private String name;
    private String createdBy;

    public GroupDto() {}

    public GroupDto(String name, String createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
