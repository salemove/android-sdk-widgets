package com.glia.widgets.core.engagement.domain.model;

import com.glia.androidsdk.Operator;

public abstract class EngagementStateEventVisitor<T> {
    protected abstract T visit(EngagementStateEvent.EngagementOperatorChangedEvent event);

    protected abstract T visit(EngagementStateEvent.EngagementOperatorConnectedEvent event);

    protected abstract T visit(EngagementStateEvent.EngagementOngoingEvent event);

    protected abstract T visit(EngagementStateEvent.EngagementEndedEvent event);

    protected abstract T visit(EngagementStateEvent.EngagementTransferringEvent event);

    public T visit(EngagementStateEvent event) {
        return event.accept(this);
    }

    public static class OperatorVisitor extends EngagementStateEventVisitor<Operator> {
        @Override
        protected Operator visit(EngagementStateEvent.EngagementOperatorChangedEvent event) {
            return event.getOperator();
        }

        @Override
        protected Operator visit(EngagementStateEvent.EngagementOperatorConnectedEvent event) {
            return event.getOperator();
        }

        @Override
        protected Operator visit(EngagementStateEvent.EngagementOngoingEvent event) {
            return event.getOperator();
        }

        @Override
        protected Operator visit(EngagementStateEvent.EngagementEndedEvent event) {
            return null;
        }

        @Override
        protected Operator visit(EngagementStateEvent.EngagementTransferringEvent event) {
            return null;
        }
    }
}
