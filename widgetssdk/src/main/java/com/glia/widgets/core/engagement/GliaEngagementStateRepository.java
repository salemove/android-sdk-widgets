package com.glia.widgets.core.engagement;

import androidx.annotation.Nullable;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Operator;
import com.glia.androidsdk.engagement.EngagementState;
import com.glia.widgets.core.engagement.domain.model.EngagementStateEvent;
import com.glia.widgets.core.engagement.domain.model.EngagementStateEventVisitor;

import java.util.Optional;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;

public class GliaEngagementStateRepository {
    private final EngagementStateEventVisitor<Operator> visitor = new EngagementStateEventVisitor.OperatorVisitor();
    private final BehaviorProcessor<Optional<Operator>> operatorProcessor =
            BehaviorProcessor.createDefault(Optional.empty());
    private final Flowable<Operator> operatorFlowable =
            operatorProcessor
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .onBackpressureLatest();

    private final BehaviorProcessor<Optional<EngagementState>> engagementStateProcessor = BehaviorProcessor.createDefault(Optional.empty());

    private final BehaviorProcessor<EngagementStateEvent> engagementStateEventProcessor = BehaviorProcessor.createDefault(
            new EngagementStateEvent.EngagementEndedEvent()
    );
    private final Flowable<EngagementStateEvent> engagementStateEventFlowable = engagementStateEventProcessor.onBackpressureLatest();

    private CompositeDisposable disposable = new CompositeDisposable();

    public void onEngagementStarted(Engagement engagement) {
        disposable = new CompositeDisposable();
        disposable.add(
                engagementStateProcessor
                        .onBackpressureLatest()
                        .map(state -> mapToEngagementStateChangeEvent(state.orElse(null), getOperator()))
                        .doOnNext(this::notifyEngagementStateEventUpdate)
                        .doOnNext(this::updateOperatorOnEngagementStateChanged)
                        .subscribe()
        );
        engagement.on(Engagement.Events.STATE_UPDATE, this::notifyEngagementStateUpdate);
        engagement.on(Engagement.Events.END, () -> onEngagementEnded(engagement));
    }

    public void onEngagementEnded(Engagement engagement) {
        engagement.off(Engagement.Events.STATE_UPDATE, this::notifyEngagementStateUpdate);
        engagementStateProcessor.onNext(Optional.empty());
        operatorProcessor.onNext(Optional.empty());
        engagementStateEventProcessor.onNext(new EngagementStateEvent.EngagementEndedEvent());
        disposable.dispose();
    }

    public Flowable<Operator> operatorFlowable() {
        return operatorFlowable;
    }

    public Flowable<EngagementStateEvent> engagementStateEventFlowable() {
        return engagementStateEventFlowable;
    }

    public boolean isOperatorPresent() {
        return operatorProcessor.getValue().isPresent();
    }

    private void notifyOperatorUpdate(Operator operator) {
        operatorProcessor.onNext(Optional.ofNullable(operator));
    }

    private void notifyEngagementStateUpdate(EngagementState engagementState) {
        engagementStateProcessor.onNext(Optional.of(engagementState));
    }

    private void notifyEngagementStateEventUpdate(EngagementStateEvent engagementStateEvent) {
        engagementStateEventProcessor.onNext(engagementStateEvent);
    }

    private @Nullable
    Operator getOperator() {
        return operatorProcessor
                .getValue()
                .orElse(null);
    }

    private EngagementStateEvent mapToEngagementStateChangeEvent(
            EngagementState engagementState,
            @Nullable Operator operator
    ) {
        if (engagementState == null) {
            return new EngagementStateEvent.EngagementEndedEvent();
        } else if (engagementState.getVisitorStatus() == EngagementState.VisitorStatus.TRANSFERRING) {
            return new EngagementStateEvent.EngagementTransferringEvent();
        } else {
            if (operator == null) {
                return new EngagementStateEvent.EngagementOperatorConnectedEvent(engagementState.getOperator());
            } else {
                if (!engagementState.getOperator().getId().equals(operator.getId())) {
                    return new EngagementStateEvent.EngagementOperatorChangedEvent(engagementState.getOperator());
                } else {
                    return new EngagementStateEvent.EngagementOngoingEvent(engagementState.getOperator());
                }
            }
        }
    }

    private void updateOperatorOnEngagementStateChanged(EngagementStateEvent engagementStateEvent) {
        switch (engagementStateEvent.getType()) {
            case ENGAGEMENT_OPERATOR_CONNECTED:
            case ENGAGEMENT_OPERATOR_CHANGED:
            case ENGAGEMENT_ONGOING:
                notifyOperatorUpdate(visitor.visit(engagementStateEvent));
                break;
            case ENGAGEMENT_TRANSFERRING:
            case ENGAGEMENT_ENDED:
                notifyOperatorUpdate(null);
                break;
        }
    }
}
