package com.glia.widgets.model;

import com.glia.androidsdk.Operator;
import com.glia.androidsdk.comms.OperatorMediaState;

public interface GliaChatHeadControllerRepositoryCallback {

    void operatorDataLoaded(Operator operator);

    void newOperatorMediaState(OperatorMediaState operatorMediaState);
}
