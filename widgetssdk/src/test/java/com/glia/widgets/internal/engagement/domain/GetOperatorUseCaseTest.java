package com.glia.widgets.internal.engagement.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import androidx.core.util.Consumer;

import com.glia.widgets.internal.engagement.GliaOperatorRepository;
import com.glia.widgets.internal.engagement.data.LocalOperator;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

@SuppressWarnings("unchecked")
public class GetOperatorUseCaseTest {
    private GliaOperatorRepository gliaOperatorRepository;
    private GetOperatorUseCase getOperatorUseCase;
    private LocalOperator operator;

    @Before
    public void setUp() throws Exception {
        gliaOperatorRepository = mock(GliaOperatorRepository.class);
        getOperatorUseCase = new GetOperatorUseCase(gliaOperatorRepository);
        operator = new LocalOperator("id", "name", "imageUrl");
    }

    @Test
    public void execute_returnsOperator_whenOperatorExists() {
        stubGetOperatorResponse(operator);

        getOperatorUseCase.invoke(operator.getId())
            .test()
            .assertResult(Optional.of(operator));
    }

    @Test
    public void execute_returnsEmptyOptional_whenOperatorNotExists() {
        stubGetOperatorResponse(null);

        getOperatorUseCase.invoke(operator.getId())
            .test()
            .assertResult(Optional.empty());
    }

    private void stubGetOperatorResponse(LocalOperator operator) {
        doAnswer(invocation -> {
            Consumer<LocalOperator> callback = invocation.getArgument(1);
            callback.accept(operator);
            return callback;
        }).when(gliaOperatorRepository).getOperatorById(anyString(), any());
    }
}
