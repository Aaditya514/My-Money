package com.financetracker.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Request payload for AI-assisted bill scanning and itemized group splitting.
 */
public class AiGroupBillScanRequest {

    private String statement;
    private String orderNotes;
    private Long defaultPayerMemberId;
    private List<GroupMemberContext> groupMembers = new ArrayList<>();

    public static class GroupMemberContext {
        private Long id;
        private String name;

        public GroupMemberContext() {}

        public GroupMemberContext(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public AiGroupBillScanRequest() {}

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getOrderNotes() {
        return orderNotes;
    }

    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }

    public Long getDefaultPayerMemberId() {
        return defaultPayerMemberId;
    }

    public void setDefaultPayerMemberId(Long defaultPayerMemberId) {
        this.defaultPayerMemberId = defaultPayerMemberId;
    }

    public List<GroupMemberContext> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<GroupMemberContext> groupMembers) {
        this.groupMembers = groupMembers;
    }
}
