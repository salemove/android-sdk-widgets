package com.glia.widgets.core.engagement.domain.model;

import com.glia.androidsdk.Operator;

public interface EngagementStateEvent {
    enum Type {
        ENGAGEMENT_ENDED,
        ENGAGEMENT_TRANSFERRING,
        ENGAGEMENT_ONGOING,
        ENGAGEMENT_OPERATOR_CONNECTED,
        ENGAGEMENT_OPERATOR_CHANGED,
        NO_ENGAGEMENT
    }

    Type getType();

    <T> T accept(EngagementStateEventVisitor<T> visitor);

    class EngagementOperatorChangedEvent implements EngagementStateEvent {
        private final Operator operator;

        public EngagementOperatorChangedEvent(Operator newOperator) {
            operator = newOperator;
        }

        public Operator getOperator() {
            return operator;
        }

        @Override
        public Type getType() {
            return Type.ENGAGEMENT_OPERATOR_CHANGED;
        }

        @Override
        public <T> T accept(EngagementStateEventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    class EngagementOperatorConnectedEvent implements EngagementStateEvent {
        private final Operator operator;

        public EngagementOperatorConnectedEvent(Operator newOperator) {
            operator = newOperator;
        }

        public Operator getOperator() {
            return operator;
        }

        @Override
        public Type getType() {
            return Type.ENGAGEMENT_OPERATOR_CONNECTED;
        }

        @Override
        public <T> T accept(EngagementStateEventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    class EngagementOngoingEvent implements EngagementStateEvent {
        private final Operator operator;

        public EngagementOngoingEvent(Operator currentOperator) {
            operator = currentOperator;
        }

        public Operator getOperator() {
            return operator;
        }

        @Override
        public Type getType() {
            return Type.ENGAGEMENT_ONGOING;
        }

        @Override
        public <T> T accept(EngagementStateEventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    class EngagementTransferringEvent implements EngagementStateEvent {
        @Override
        public Type getType() {
            return Type.ENGAGEMENT_TRANSFERRING;
        }

        @Override
        public <T> T accept(EngagementStateEventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    class EngagementEndedEvent implements EngagementStateEvent {

        @Override
        public Type getType() {
            return Type.ENGAGEMENT_ENDED;
        }

        @Override
        public <T> T accept(EngagementStateEventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    class NoEngagementEvent implements EngagementStateEvent {

        @Override
        public Type getType() {
            return Type.NO_ENGAGEMENT;
        }

        @Override
        public <T> T accept(EngagementStateEventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}


