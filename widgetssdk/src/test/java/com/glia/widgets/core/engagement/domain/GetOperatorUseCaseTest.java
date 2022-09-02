package com.glia.widgets.core.engagement.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import androidx.core.util.Consumer;

import com.glia.androidsdk.Operator;
import com.glia.widgets.core.engagement.GliaOperatorRepository;
import com.glia.widgets.core.model.TestOperator;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

@SuppressWarnings("unchecked")
public class GetOperatorUseCaseTest {
    private GliaOperatorRepository gliaOperatorRepository;
    private GetOperatorUseCase getOperatorUseCase;
    private Operator operator;

    @Before
    public void setUp() throws Exception {
        gliaOperatorRepository = mock(GliaOperatorRepository.class);
        getOperatorUseCase = new GetOperatorUseCase(gliaOperatorRepository);
        operator = TestOperator.DEFAULT;
    }

    @Test
    public void execute_returnsOperator_whenOperatorExists() {
        stubGetOperatorResponse(operator);

        getOperatorUseCase.execute(operator.getId())
                .test()
                .assertResult(Optional.of(operator));
    }

    @Test
    public void execute_returnsEmptyOptional_whenOperatorNotExists() {
        stubGetOperatorResponse(null);

        getOperatorUseCase.execute(operator.getId())
                .test()
                .assertResult(Optional.empty());
    }

    private void stubGetOperatorResponse(Operator operator) {
        doAnswer(invocation -> {
            Consumer<Operator> callback = invocation.getArgument(1);
            callback.accept(operator);
            return callback;
        }).when(gliaOperatorRepository).getOperatorById(anyString(), any());
    }
}