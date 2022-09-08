package com.glia.widgets.core.engagement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import androidx.core.util.Consumer;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.Operator;
import com.glia.androidsdk.RequestCallback;
import com.glia.widgets.core.model.TestOperator;
import com.glia.widgets.di.GliaCore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GliaOperatorRepositoryTest {
    GliaOperatorRepository repository;
    GliaCore core;
    Operator operator;

    @Before
    public void setUp() {
        core = mock(GliaCore.class);
        repository = new GliaOperatorRepository(core);
        operator = TestOperator.DEFAULT;
    }

    @Test
    public void getOperatorById_returnsCachedOperator_whenCachedOperatorExists() {
        repository.addOrUpdateOperator(operator);

        Consumer<Operator> callback = mock(Consumer.class);

        repository.getOperatorById(operator.getId(), callback);

        verify(callback).accept(operator);
    }

    @Test
    public void getOperatorById_returnsOperatorFromApiCall_whenCachedOperatorNotExists() {
        stubGetOperatorResponse(operator, null);

        Consumer<Operator> callback = mock(Consumer.class);

        repository.getOperatorById(operator.getId(), callback);

        verify(callback).accept(operator);
    }

    @Test
    public void getOperatorById_returnsNull_whenCachedOperatorNotExistsAndApiReturnsError() {
        stubGetOperatorResponse(null, new GliaException("", GliaException.Cause.INVALID_INPUT));

        Consumer<Operator> callback = mock(Consumer.class);

        repository.getOperatorById(operator.getId(), callback);

        verify(callback).accept(null);
    }

    void stubGetOperatorResponse(Operator operator, GliaException exception) {
        Mockito.doAnswer(invocation -> {
            RequestCallback<Operator> callback = invocation.getArgument(1);
            callback.onResult(operator, exception);
            return callback;
        }).when(core).getOperator(anyString(), any());
    }
}