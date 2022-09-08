package com.glia.widgets.core.model;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Operator;

public class TestOperator implements Operator {
    public static final Operator DEFAULT = new TestOperator();
    @Override
    public String getId() {
        return "Operator ID";
    }

    @Override
    public String getName() {
        return "Test Name";
    }

    @Override
    public Picture getPicture() {
        return new Picture("https://picture");
    }

    @Override
    public Engagement.MediaType[] getAvailableMedia() {
        return new Engagement.MediaType[0];
    }
}
