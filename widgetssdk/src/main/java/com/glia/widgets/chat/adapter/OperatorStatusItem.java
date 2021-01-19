package com.glia.widgets.chat.adapter;

public class OperatorStatusItem extends ChatItem {
    private final String companyName;
    private final Status status;
    private final String operatorName;

    public OperatorStatusItem(Status status, String companyName, String operatorName) {
        super(ChatAdapter.OPERATOR_STATUS_VIEW_TYPE);
        this.companyName = companyName;
        this.status = status;
        this.operatorName = operatorName;
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

    public enum Status {
        IN_QUEUE, OPERATOR_CONNECTED
    }

    public static OperatorStatusItem QueueingStatusItem(String companyName) {
        return new OperatorStatusItem(OperatorStatusItem.Status.IN_QUEUE, companyName, null);
    }

    public static OperatorStatusItem OperatorFoundStatusItem(String companyName, String operatorName) {
        return new OperatorStatusItem(Status.OPERATOR_CONNECTED, companyName, operatorName);
    }

    @Override
    public String toString() {
        return "OperatorStatusItem{" +
                "companyName='" + companyName + '\'' +
                ", status=" + status +
                ", operatorName='" + operatorName + '\'' +
                '}';
    }
}
