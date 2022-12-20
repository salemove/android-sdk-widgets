package com.glia.widgets.core.queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.glia.androidsdk.RequestCallback;
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
        RequestCallback<QueueTicket> callback = gliaQueueRepository.emitOnQueueTicketChangeToUnstaffed(emitter);
        QueueTicket updatedTicket = mock(QueueTicket.class);
        when(updatedTicket.getState()).thenReturn(QueueTicket.State.ENQUEUED);
        when(queueingState.getTicketId()).thenReturn("123");

        callback.onResult(updatedTicket, null);

        verify(emitter, never()).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToRequestSent() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        RequestCallback<QueueTicket> callback = gliaQueueRepository.emitOnQueueTicketChangeToUnstaffed(emitter);
        QueueTicket updatedTicket = mock(QueueTicket.class);
        when(updatedTicket.getState()).thenReturn(QueueTicket.State.REQUEST_SENT);
        when(queueingState.getTicketId()).thenReturn("123");

        callback.onResult(updatedTicket, null);

        verify(emitter, never()).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToCanceled() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        RequestCallback<QueueTicket> callback = gliaQueueRepository.emitOnQueueTicketChangeToUnstaffed(emitter);
        QueueTicket updatedTicket = mock(QueueTicket.class);
        when(updatedTicket.getState()).thenReturn(QueueTicket.State.CANCELED);
        when(queueingState.getTicketId()).thenReturn("123");

        callback.onResult(updatedTicket, null);

        verify(emitter, never()).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToFinished() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        RequestCallback<QueueTicket> callback = gliaQueueRepository.emitOnQueueTicketChangeToUnstaffed(emitter);
        QueueTicket updatedTicket = mock(QueueTicket.class);
        when(updatedTicket.getState()).thenReturn(QueueTicket.State.FINISHED);
        when(queueingState.getTicketId()).thenReturn("123");

        callback.onResult(updatedTicket, null);

        verify(emitter, never()).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToFailed() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        RequestCallback<QueueTicket> callback = gliaQueueRepository.emitOnQueueTicketChangeToUnstaffed(emitter);
        QueueTicket updatedTicket = mock(QueueTicket.class);
        when(updatedTicket.getState()).thenReturn(QueueTicket.State.FAILED);
        when(queueingState.getTicketId()).thenReturn("123");

        callback.onResult(updatedTicket, null);

        verify(emitter, never()).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToUnstafed() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        RequestCallback<QueueTicket> callback = gliaQueueRepository.emitOnQueueTicketChangeToUnstaffed(emitter);
        QueueTicket updatedTicket = mock(QueueTicket.class);
        when(updatedTicket.getState()).thenReturn(QueueTicket.State.UNSTAFFED);
        when(queueingState.getTicketId()).thenReturn("123");

        callback.onResult(updatedTicket, null);

        verify(emitter, times(1)).onComplete();
    }

    @Test
    public void observeQueueTicketStateChangeToUnstaffed_DoesNotEmit_onQueueTicketStateChangeToUnknown() {
        GliaQueueingState queueingState = mock(GliaQueueingState.class);
        GliaQueueRepository gliaQueueRepository = new GliaQueueRepository(mock(GliaCore.class), queueingState);
        CompletableEmitter emitter = mock(CompletableEmitter.class);
        RequestCallback<QueueTicket> callback = gliaQueueRepository.emitOnQueueTicketChangeToUnstaffed(emitter);
        QueueTicket updatedTicket = mock(QueueTicket.class);
        when(updatedTicket.getState()).thenReturn(QueueTicket.State.UNKNOWN);
        when(queueingState.getTicketId()).thenReturn("123");

        callback.onResult(updatedTicket, null);

        verify(emitter, never()).onComplete();
    }
}
