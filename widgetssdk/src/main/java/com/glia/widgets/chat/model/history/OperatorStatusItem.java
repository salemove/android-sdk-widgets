package com.glia.widgets.chat.model.history;

import com.glia.widgets.chat.adapter.ChatAdapter;

import java.util.Objects;

public class OperatorStatusItem extends ChatItem {
    public static final String ID = "operator_status_item";
    private final String companyName;
    private final Status status;
    private final String operatorName;
    private final String profileImgUrl;

    public OperatorStatusItem(Status status, String companyName, String operatorName, String profileImgUrl) {
        super(ID, ChatAdapter.OPERATOR_STATUS_VIEW_TYPE);
        this.companyName = companyName;
        this.status = status;
        this.operatorName = operatorName;
        this.profileImgUrl = profileImgUrl;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Status getStatus() {
        return status;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public enum Status {
        IN_QUEUE, OPERATOR_CONNECTED
    }

    public static OperatorStatusItem QueueingStatusItem(String companyName) {
        return new OperatorStatusItem(OperatorStatusItem.Status.IN_QUEUE, companyName, null, null);
    }

    public static OperatorStatusItem OperatorFoundStatusItem(String companyName, String operatorName, String profileImgUrl) {
        return new OperatorStatusItem(Status.OPERATOR_CONNECTED, companyName, operatorName, profileImgUrl);
    }

    @Override
    public String toString() {
        return "OperatorStatusItem{" +
                "companyName='" + companyName + '\'' +
                ", status=" + status +
                ", operatorName='" + operatorName + '\'' +
                ", profileImgUrl='" + profileImgUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OperatorStatusItem that = (OperatorStatusItem) o;
        return Objects.equals(companyName, that.companyName) &&
                status == that.status &&
                Objects.equals(operatorName, that.operatorName) &&
                Objects.equals(profileImgUrl, that.profileImgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), companyName, status, operatorName, profileImgUrl);
    }
}
