package com.financetracker.dto;

public class SplitContactDTO {

    private String name;
    private String phoneNumber;
    private String email;
    private String nickname;
    private Long registeredUserId;
    private int groupsCount;

    public SplitContactDTO() {
    }

    public SplitContactDTO(String name, String phoneNumber, String nickname, Long registeredUserId, int groupsCount) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.registeredUserId = registeredUserId;
        this.groupsCount = groupsCount;
    }

    public SplitContactDTO(String name, String phoneNumber, String email, String nickname, Long registeredUserId, int groupsCount) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.nickname = nickname;
        this.registeredUserId = registeredUserId;
        this.groupsCount = groupsCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getRegisteredUserId() {
        return registeredUserId;
    }

    public void setRegisteredUserId(Long registeredUserId) {
        this.registeredUserId = registeredUserId;
    }

    public int getGroupsCount() {
        return groupsCount;
    }

    public void setGroupsCount(int groupsCount) {
        this.groupsCount = groupsCount;
    }
}
