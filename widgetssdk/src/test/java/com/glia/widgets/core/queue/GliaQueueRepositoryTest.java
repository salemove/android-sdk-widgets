package com.glia.widgets.core.queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.glia.androidsdk.internal.queuing.QueueManager;
import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.widgets.core.queue.model.GliaQueueingState;
import com.glia.widgets.di.GliaCore;

import org.junit.After;
import org.junit.Test;

import io.reactivex.CompletableEmitter;
import io.reactivex.disposables.CompositeDisposable;

public class GliaQueueRepositoryTest {

    CompositeDisposable disposable = new CompositeDisposable();

    @After
    public void tearDown() {
        disposable.clear();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_Emits_onQueueTicketStateChangeToEnqueued() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        java.util.function.Consumer<QueueTicket[]> consumer = gliaQueueRepository.getQueueTicketStateChangeToUnstaffedListener(emitter);
        QueueTicket updatedTicket = new QueueTicket("123", QueueTicket.State.ENQUEUED, mock(QueueManager.class));
        QueueTicket[] ticketArray = {updatedTicket};
        when(queueingState.getTicketId()).thenReturn(updatedTicket.getId());

        consumer.accept(ticketArray);

        verify(emitter, never()).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToRequestSent() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        java.util.function.Consumer<QueueTicket[]> consumer = gliaQueueRepository.getQueueTicketStateChangeToUnstaffedListener(emitter);
        QueueTicket updatedTicket = new QueueTicket("123", QueueTicket.State.REQUEST_SENT, mock(QueueManager.class));
        QueueTicket[] ticketArray = {updatedTicket};
        when(queueingState.getTicketId()).thenReturn(updatedTicket.getId());

        consumer.accept(ticketArray);

        verify(emitter, never()).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToCanceled() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        java.util.function.Consumer<QueueTicket[]> consumer = gliaQueueRepository.getQueueTicketStateChangeToUnstaffedListener(emitter);
        QueueTicket updatedTicket = new QueueTicket("123", QueueTicket.State.CANCELED, mock(QueueManager.class));
        QueueTicket[] ticketArray = {updatedTicket};
        when(queueingState.getTicketId()).thenReturn(updatedTicket.getId());

        consumer.accept(ticketArray);

        verify(emitter, never()).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToFinished() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        java.util.function.Consumer<QueueTicket[]> consumer = gliaQueueRepository.getQueueTicketStateChangeToUnstaffedListener(emitter);
        QueueTicket updatedTicket = new QueueTicket("123", QueueTicket.State.FINISHED, mock(QueueManager.class));
        QueueTicket[] ticketArray = {updatedTicket};
        when(queueingState.getTicketId()).thenReturn(updatedTicket.getId());

        consumer.accept(ticketArray);

        verify(emitter, never()).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToFailed() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        java.util.function.Consumer<QueueTicket[]> consumer = gliaQueueRepository.getQueueTicketStateChangeToUnstaffedListener(emitter);
        QueueTicket updatedTicket = new QueueTicket("123", QueueTicket.State.FAILED, mock(QueueManager.class));
        QueueTicket[] ticketArray = {updatedTicket};
        when(queueingState.getTicketId()).thenReturn(updatedTicket.getId());

        consumer.accept(ticketArray);

        verify(emitter, never()).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToUnstafed() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        java.util.function.Consumer<QueueTicket[]> consumer = gliaQueueRepository.getQueueTicketStateChangeToUnstaffedListener(emitter);
        QueueTicket updatedTicket = new QueueTicket("123", QueueTicket.State.UNSTAFFED, mock(QueueManager.class));
        QueueTicket[] ticketArray = {updatedTicket};
        when(queueingState.getTicketId()).thenReturn(updatedTicket.getId());

        consumer.accept(ticketArray);

        verify(emitter, times(1)).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToUnknown() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        java.util.function.Consumer<QueueTicket[]> consumer = gliaQueueRepository.getQueueTicketStateChangeToUnstaffedListener(emitter);
        QueueTicket updatedTicket = new QueueTicket("123", QueueTicket.State.UNKNOWN, mock(QueueManager.class));
        QueueTicket[] ticketArray = {updatedTicket};
        when(queueingState.getTicketId()).thenReturn(updatedTicket.getId());

        consumer.accept(ticketArray);

        verify(emitter, never()).onComplete();
    }
}
